package com.creants.creants_2x.mmo;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

import com.creants.creants_2x.core.util.QAntTracer;
import com.creants.creants_2x.socket.gate.wood.QAntUser;

/**
 * @author LamHM
 *
 */
public class ProximityManager implements IProximityManager {
	private final Vec3D aoi;
	private final Vec3D sectorSize;
	private final ConcurrentMap<P3D, ConcurrentLinkedQueue<QAntUser>> map;


	public ProximityManager(final Vec3D aoi) {
		this.aoi = aoi;
		if (aoi.isFloat())
			sectorSize = new Vec3D(aoi.floatX() * 1.0f, aoi.floatY() * 1.0f, aoi.floatZ() * 1.0f);
		else
			sectorSize = new Vec3D(aoi.intX() * 1, aoi.intY() * 1, aoi.intZ() * 1);
		map = new ConcurrentHashMap<P3D, ConcurrentLinkedQueue<QAntUser>>();
	}


	@Override
	public Vec3D getDefaultAOI() {
		return aoi;
	}


	@Override
	public void addUser(QAntUser user) {
		P3D pos = findSector(user);
		user.setProperty("_uPos", pos);
		moveUser(user, pos, null);
	}


	private void moveUser(QAntUser user, P3D newPos, P3D oldPos) {
		ConcurrentLinkedQueue<QAntUser> uList = null;
		synchronized (map) {
			uList = map.get(newPos);
			if (uList == null) {
				uList = new ConcurrentLinkedQueue<QAntUser>();
				map.put(newPos, uList);
			}
		}
		// monitorexit(this.map)
		uList.add(user);
		if (oldPos != null) {
			uList = map.get(oldPos);
			if (uList != null) {
				uList.remove(user);
			}
		}
	}


	@Override
	public void updateUser(QAntUser user) {
		P3D newPos = findSector(user);
		P3D oldPos = (P3D) user.getProperty("_uPos");
		user.setProperty("_uPos", newPos);
		if (!newPos.equals(oldPos)) {
			moveUser(user, newPos, oldPos);
		}
	}


	@Override
	public List<QAntUser> getProximityList(QAntUser target, Vec3D aoi) {
		P3D targetPos = (P3D) target.getProperty("_uPos");
		Vec3D targetLocation = (Vec3D) target.getProperty("_uLoc");
		if (targetLocation == null) {
			return null;
		}
		List<P3D> queryBlocks = getQueryBlocks(targetPos);
		List<QAntUser> proximityList = new LinkedList<QAntUser>();
		// TODO lamda
		for (P3D pos : queryBlocks) {
			ConcurrentLinkedQueue<QAntUser> users = map.get(pos);
			if (users == null) {
				continue;
			}

			proximityList.addAll(users.stream().filter(u -> u != target && userFallsWithinAOI(u, targetLocation, aoi))
					.collect(Collectors.toList()));
		}
		return proximityList;
	}


	@Override
	public List<QAntUser> getProximityList(QAntUser target) {
		return getProximityList(target, aoi);
	}


	@Override
	public List<QAntUser> getProximityList(Vec3D pos) {
		return getProximityList(pos, aoi);
	}


	Collection<QAntUser> getProximitySector(P3D pos) {
		return map.get(pos);
	}


	List<QAntUser> getProximityList(P3D pos, Vec3D targetLocation) {
		List<P3D> queryBlocks = getQueryBlocks(pos);
		return findLocalUsersWithinAOI(queryBlocks, targetLocation, aoi);
	}


	@Override
	public List<QAntUser> getProximityList(Vec3D targetLocation, Vec3D aoi) {
		List<P3D> queryBlocks = getQueryBlocks(findSector(targetLocation));
		return findLocalUsersWithinAOI(queryBlocks, targetLocation, aoi);
	}


	private boolean userFallsWithinAOI(QAntUser userToCheck, Vec3D targetLocation, Vec3D aoi) {
		Vec3D checkLocation = (Vec3D) userToCheck.getProperty("_uLoc");
		if (checkLocation == null) {
			QAntTracer.debug(this.getClass(), "User: " + userToCheck + " has no location in the map.");
			return false;
		}
		boolean checkX;
		boolean checkY;
		boolean checkZ;
		if (targetLocation.isFloat()) {
			checkX = (Math.abs(targetLocation.floatX() - checkLocation.floatX()) < aoi.floatX());
			checkY = (Math.abs(targetLocation.floatY() - checkLocation.floatY()) < aoi.floatY());
			checkZ = (aoi.floatZ() == 0.0f
					|| Math.abs(targetLocation.floatZ() - checkLocation.floatZ()) < aoi.floatZ());
		} else {
			checkX = (Math.abs(targetLocation.intX() - checkLocation.intX()) < aoi.intX());
			checkY = (Math.abs(targetLocation.intY() - checkLocation.intY()) < aoi.intY());
			checkZ = (aoi.intZ() == 0 || Math.abs(targetLocation.intZ() - checkLocation.intZ()) < aoi.intZ());
		}
		return checkX && checkY && checkZ;
	}


	private List<P3D> getQueryBlocks(P3D center) {
		final List<P3D> queryBlocks = new LinkedList<P3D>();
		for (int z = -1; z <= 1; ++z) {
			for (int y = -1; y <= 1; ++y) {
				for (int x = -1; x <= 1; ++x) {
					queryBlocks.add(new P3D(center.px + x, center.py + y, center.pz + z));
				}
			}
		}
		return queryBlocks;
	}


	@Override
	public Vec3D getSectorSize() {
		return this.sectorSize;
	}


	@Override
	public int getSize() {
		return this.map.size();
	}


	@Override
	public void removeUser(QAntUser user) {
		P3D lastPos = (P3D) user.getProperty("_uPos");
		if (lastPos == null) {
			return;
		}

		ConcurrentLinkedQueue<QAntUser> q = map.get(lastPos);
		if (q != null && !q.contains(user)) {
			lastPos = findUserLocation(user);
			q = map.get(lastPos);
		}

		if (q != null) {
			q.remove(user);
			user.removeProperty("_uPos");
			return;
		}

		throw new IllegalStateException();
	}


	public P3D findUserLocation(QAntUser user) {
		for (Map.Entry<P3D, ConcurrentLinkedQueue<QAntUser>> entry : map.entrySet()) {
			if (entry.getValue().contains(user)) {
				return entry.getKey();
			}
		}

		return null;
	}


	public List<QAntUser> dumpAllUsers() {
		List<QAntUser> allUsers = new LinkedList<QAntUser>();
		for (ConcurrentLinkedQueue<QAntUser> q : map.values()) {
			allUsers.addAll(q);
		}

		return allUsers;
	}


	private P3D findSector(QAntUser user) {
		Vec3D pos = (Vec3D) user.getProperty("_uLoc");
		return findSector(pos);
	}


	private P3D findSector(Vec3D pos) {
		if (pos == null) {
			throw new IllegalArgumentException("User does not have a position assigned!");
		}

		if (pos.isFloat() != this.sectorSize.isFloat()) {
			throw new IllegalArgumentException(
					"User coordinates don't match numeric type of the Room's Area Of Interest (AOI)");
		}

		if (pos.isFloat()) {
			return findFloatSector(pos);
		}
		return findIntSector(pos);
	}


	private P3D findFloatSector(Vec3D pos) {
		int xx = (int) (pos.floatX() / sectorSize.floatX());
		xx = ((pos.floatX() < 0.0f) ? (xx - 1) : xx);
		int yy = (int) (pos.floatY() / sectorSize.floatY());
		yy = ((pos.floatY() < 0.0f) ? (yy - 1) : yy);
		int zz = 0;
		if (sectorSize.floatZ() != 0.0f) {
			zz = (int) (pos.floatZ() / sectorSize.floatZ());
			zz = ((pos.floatZ() < 0.0f) ? (zz - 1) : zz);
		}
		return new P3D(xx, yy, zz);
	}


	private P3D findIntSector(Vec3D pos) {
		int xx = pos.intX() / sectorSize.intX();
		xx = ((pos.intX() < 0) ? (xx - 1) : xx);
		int yy = pos.intY() / sectorSize.intY();
		yy = ((pos.intY() < 0) ? (yy - 1) : yy);
		int zz = 0;
		if (sectorSize.intZ() != 0) {
			zz = pos.intZ() / sectorSize.intZ();
			zz = ((pos.intZ() < 0) ? (zz - 1) : zz);
		}
		return new P3D(xx, yy, zz);
	}


	// TODO lamda
	private List<QAntUser> findLocalUsersWithinAOI(List<P3D> queryBlocks, Vec3D targetLocation, Vec3D aoi) {
		List<QAntUser> userList = new LinkedList<QAntUser>();
		for (P3D pos : queryBlocks) {
			ConcurrentLinkedQueue<QAntUser> users = map.get(pos);
			if (users == null)
				continue;

			for (QAntUser user : users) {
				if (userFallsWithinAOI(user, targetLocation, aoi)) {
					userList.add(user);
				}
			}
		}
		return userList;
	}


	public void dumpState() {
		for (P3D pos : map.keySet()) {
			System.out.println(pos + " --> " + map.get(pos));
		}
	}
}
