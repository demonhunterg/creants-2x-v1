package com.creants.creants_2x.mmo;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;

import com.creants.creants_2x.socket.gate.wood.QAntUser;

/**
 * @author LamHM
 *
 */
public class MMOItemManager implements IMMOItemManager {
	private final Vec3D aoi;
	private final Vec3D sectorSize;
	private final ConcurrentMap<P3D, ConcurrentLinkedQueue<BaseMMOItem>> map;
	private final MMORoom owner;


	public MMOItemManager(Vec3D aoi, MMORoom owner) {
		this.owner = owner;
		this.aoi = aoi;
		if (aoi.isFloat()) {
			this.sectorSize = new Vec3D(aoi.floatX() * 1.0f, aoi.floatY() * 1.0f, aoi.floatZ() * 1.0f);
		} else {
			this.sectorSize = new Vec3D(aoi.intX() * 1, aoi.intY() * 1, aoi.intZ() * 1);
		}
		this.map = new ConcurrentHashMap<P3D, ConcurrentLinkedQueue<BaseMMOItem>>();
	}


	@Override
	public void setItem(final BaseMMOItem item, final Vec3D location) {
		final P3D oldPos = item.getLastPos();
		final Vec3D oldLocation = item.getLastLocation();
		final boolean isNewItem = oldPos == null;
		final P3D newPos = this.findSector(location);
		final boolean isSamePosition = newPos.equals(item.getLastPos());
		item.setLastPos(newPos);
		item.setLastLocation(location);
		final boolean needsUpdate = isNewItem || !isSamePosition;
		if (needsUpdate) {
			if (!isNewItem) {
				final List<QAntUser> usersAffectedInPreviousPos = ((ProximityManager) this.owner.getProximityManager())
						.getProximityList(oldPos, oldLocation);
				if (usersAffectedInPreviousPos != null) {
					item.setLastProxyList(usersAffectedInPreviousPos);
				}
			}
			this.moveItem(item, newPos, oldPos);
		}
	}


	@Override
	public List<BaseMMOItem> getItemList(QAntUser target, final Vec3D aoi) {
		final P3D targetPos = (P3D) target.getProperty("_uPos");
		final Vec3D targetLocation = (Vec3D) target.getProperty("_uLoc");
		if (targetLocation == null) {
			return null;
		}
		final List<P3D> queryBlocks = this.getQueryBlocks(targetPos);
		return this.findLocalItemsWithinAOI(queryBlocks, targetLocation, aoi);
	}


	@Override
	public List<BaseMMOItem> getItemList(QAntUser target) {
		return this.getItemList(target, this.aoi);
	}


	@Override
	public List<BaseMMOItem> getItemList(final Vec3D location, final Vec3D aoi) {
		final P3D targetPos = this.findSector(location);
		final List<P3D> queryBlocks = this.getQueryBlocks(targetPos);
		return this.findLocalItemsWithinAOI(queryBlocks, location, aoi);
	}


	@Override
	public List<BaseMMOItem> getItemList(final Vec3D pos) {
		return this.getItemList(pos, this.aoi);
	}


	@Override
	public int getSize() {
		return this.map.size();
	}


	@Override
	public void removeItem(final BaseMMOItem item) {
		P3D lastPos = item.getLastPos();
		if (lastPos == null) {
			return;
		}
		ConcurrentLinkedQueue<BaseMMOItem> q = this.map.get(lastPos);
		if (q != null && !q.contains(item)) {
			lastPos = this.findItemLocation(item);
			q = this.map.get(lastPos);
		}
		if (q != null) {
			q.remove(item);
			return;
		}
		throw new IllegalStateException();
	}


	public P3D findItemLocation(final BaseMMOItem item) {
		for (final Map.Entry<P3D, ConcurrentLinkedQueue<BaseMMOItem>> entry : this.map.entrySet()) {
			if (entry.getValue().contains(item)) {
				return entry.getKey();
			}
		}
		return null;
	}


	private void moveItem(final BaseMMOItem item, final P3D newPos, final P3D oldPos) {
		ConcurrentLinkedQueue<BaseMMOItem> itemList = null;
		synchronized (this.map) {
			itemList = this.map.get(newPos);
			if (itemList == null) {
				itemList = new ConcurrentLinkedQueue<BaseMMOItem>();
				this.map.put(newPos, itemList);
			}
		}
		// monitorexit(this.map)
		itemList.add(item);
		if (oldPos != null) {
			itemList = this.map.get(oldPos);
			if (itemList != null) {
				itemList.remove(item);
			}
		}
	}


	private P3D findSector(final Vec3D pos) {
		if (pos.isFloat()) {
			return this.findFloatSector(pos);
		}
		return this.findIntSector(pos);
	}


	private P3D findFloatSector(final Vec3D pos) {
		int xx = (int) (pos.floatX() / this.sectorSize.floatX());
		xx = ((pos.floatX() < 0.0f) ? (xx - 1) : xx);
		int yy = (int) (pos.floatY() / this.sectorSize.floatY());
		yy = ((pos.floatY() < 0.0f) ? (yy - 1) : yy);
		int zz = 0;
		if (this.sectorSize.floatZ() != 0.0f) {
			zz = (int) (pos.floatZ() / this.sectorSize.floatZ());
			zz = ((pos.floatZ() < 0.0f) ? (zz - 1) : zz);
		}
		return new P3D(xx, yy, zz);
	}


	private P3D findIntSector(final Vec3D pos) {
		int xx = pos.intX() / this.sectorSize.intX();
		xx = ((pos.intX() < 0) ? (xx - 1) : xx);
		int yy = pos.intY() / this.sectorSize.intY();
		yy = ((pos.intY() < 0) ? (yy - 1) : yy);
		int zz = 0;
		if (this.sectorSize.intZ() != 0) {
			zz = pos.intZ() / this.sectorSize.intZ();
			zz = ((pos.intZ() < 0) ? (zz - 1) : zz);
		}
		return new P3D(xx, yy, zz);
	}


	private boolean itemFallsWithinAOI(final BaseMMOItem item, final Vec3D targetLocation, final Vec3D aoi) {
		final Vec3D checkLocation = item.getLastLocation();
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


	private List<P3D> getQueryBlocks(final P3D center) {
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


	private List<BaseMMOItem> findLocalItemsWithinAOI(final List<P3D> queryBlocks, final Vec3D targetLocation,
			final Vec3D aoi) {
		final List<BaseMMOItem> itemList = new LinkedList<BaseMMOItem>();
		for (final P3D pos : queryBlocks) {
			final ConcurrentLinkedQueue<BaseMMOItem> items = this.map.get(pos);
			if (items == null) {
				continue;
			}
			for (final BaseMMOItem it : items) {
				if (this.itemFallsWithinAOI(it, targetLocation, aoi)) {
					itemList.add(it);
				}
			}
		}
		return itemList;
	}


	public void dumpState() {
		for (final P3D pos : this.map.keySet()) {
			System.out.println(pos + " --> " + this.map.get(pos));
		}
	}

}
