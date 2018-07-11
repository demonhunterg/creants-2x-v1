package com.creants.creants_2x.mmo;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import com.creants.creants_2x.QAntServer;
import com.creants.creants_2x.core.entities.QAntRoom;
import com.creants.creants_2x.core.exception.QAntJoinRoomException;
import com.creants.creants_2x.core.util.IPlayerIdGenerator;
import com.creants.creants_2x.socket.gate.entities.IQAntArray;
import com.creants.creants_2x.socket.gate.wood.QAntUser;

import io.netty.util.AttributeKey;

/**
 * @author LamHM
 *
 */
public class MMORoom extends QAntRoom {
	private final IProximityManager proximityManager;
	private final IMMOItemManager itemsManager;
	private final IMMOUpdateManager updateManager;
	private final ConcurrentMap<Integer, BaseMMOItem> itemsById;
	private Vec3D lowLimit;
	private Vec3D highLimit;
	private int userLimboMaxSeconds;
	private ScheduledFuture<?> limboCleanerTask;
	private boolean sendAOIEntryPoint;
	private static final AttributeKey<PreviousMMORoomState> PREVIOUS_MMO_ROOM_STATE = AttributeKey
			.valueOf("PreviousMMORoomState");


	public MMORoom(String name, Vec3D aoi, int updateMillis) {
		super(name);
		this.userLimboMaxSeconds = 0;
		this.sendAOIEntryPoint = false;
		this.proximityManager = new ProximityManager(aoi);
		this.itemsManager = new MMOItemManager(aoi, this);
		this.updateManager = new MMOUpdateManager(this, updateMillis);
		this.itemsById = new ConcurrentHashMap<Integer, BaseMMOItem>();
	}


	@Override
	public void addUser(QAntUser user, final boolean asSpectator) throws QAntJoinRoomException {
		super.addUser(user, false);
		user.setLastProxyList(null);
	}


	@Override
	public void removeUser(QAntUser user) {
		super.removeUser(user);
		try {
			proximityManager.removeUser(user);
			user.setLastMMOItemsList(null);
			if (user.getLastProxyList() != null) {
				updateManager.addBatchToUpdate(user.getLastProxyList());
			} else {
				PreviousMMORoomState prevState = user.getChannel().attr(PREVIOUS_MMO_ROOM_STATE).get();
				if (prevState != null && prevState.getRoomId() == this.getId()) {
					updateManager.addBatchToUpdate(prevState.getProxyList());
				}
			}
		} catch (IllegalStateException err) {
			throw new IllegalStateException(
					"Remove failed. Requested user " + user + " was not found in this room: " + this);
		} finally {
			user.removeProperty("_uLoc");
		}
		user.removeProperty("_uLoc");
	}


	public void removeMMOItem(BaseMMOItem item) {
		this.itemsManager.removeItem(item);
		this.updateManager.addItemToUpdate(item);
		this.itemsById.remove(item.getId());
	}


	public Vec3D getDefaultAOI() {
		return this.proximityManager.getDefaultAOI();
	}


	public List<QAntUser> getProximityList(QAntUser target) {
		return this.proximityManager.getProximityList(target);
	}


	public List<QAntUser> getProximityList(QAntUser target, final Vec3D aoi) {
		return this.proximityManager.getProximityList(target, aoi);
	}


	public List<QAntUser> getProximityList(final Vec3D position) {
		return this.proximityManager.getProximityList(position);
	}


	public List<QAntUser> getProximityList(final Vec3D position, final Vec3D aoi) {
		return this.proximityManager.getProximityList(position, aoi);
	}


	public BaseMMOItem getMMOItemById(final int itemId) {
		return this.itemsById.get(itemId);
	}


	public List<BaseMMOItem> getAllMMOItems() {
		return new LinkedList<BaseMMOItem>(this.itemsById.values());
	}


	public boolean containsMMOItem(final int id) {
		return this.itemsById.containsKey(id);
	}


	public boolean containsMMOItem(final BaseMMOItem item) {
		return this.itemsById.containsValue(item);
	}


	public List<BaseMMOItem> getProximityItems(QAntUser target) {
		return this.itemsManager.getItemList(target);
	}


	public List<BaseMMOItem> getProximityItems(QAntUser target, final Vec3D aoi) {
		return this.itemsManager.getItemList(target, aoi);
	}


	public List<BaseMMOItem> getProximityItems(final Vec3D pos) {
		return this.itemsManager.getItemList(pos);
	}


	public List<BaseMMOItem> getProximityItems(final Vec3D pos, final Vec3D aoi) {
		return this.itemsManager.getItemList(pos, aoi);
	}


	public Vec3D getSectorSize() {
		return this.proximityManager.getSectorSize();
	}


	public IProximityManager getProximityManager() {
		return this.proximityManager;
	}


	public IMMOItemManager getItemsManager() {
		return this.itemsManager;
	}


	public P3D findUserLocation(QAntUser user) {
		return ((ProximityManager) this.proximityManager).findUserLocation(user);
	}


	public P3D findItemLocation(final BaseMMOItem item) {
		return ((MMOItemManager) itemsManager).findItemLocation(item);
	}


	public void updateUser(QAntUser user) {
		if (!this.containsUser(user)) {
			throw new IllegalArgumentException("Invalid User, not joined in this MMORoom: " + this.toString());
		}
		this.proximityManager.updateUser(user);
		this.updateManager.addUserToUpdate(user);
	}


	public void updateItem(final BaseMMOItem item, final Vec3D pos) {
		if (item.getRoom() == null) {
			item.setRoom(this);
		}
		if (item.getRoom() != this) {
			throw new IllegalArgumentException(String.format(
					"Item: %s is already assigned to %s, and can't be re-assigned to %s", item.getRoom(), this));
		}
		this.itemsManager.setItem(item, pos);
		this.updateManager.addItemToUpdate(item);
		this.itemsById.putIfAbsent(item.getId(), item);
	}


	public Vec3D getMapLowerLimit() {
		return this.lowLimit;
	}


	public Vec3D getMapHigherLimit() {
		return this.highLimit;
	}


	public void setMapLimits(final Vec3D lowLimit, final Vec3D highLimit) {
		if (this.lowLimit != null || this.highLimit != null) {
			throw new IllegalStateException("Map Limits cannot be reset");
		}
		this.lowLimit = lowLimit;
		this.highLimit = highLimit;
	}


	public int getUserLimboMaxSeconds() {
		return this.userLimboMaxSeconds;
	}


	public void setUserLimboMaxSeconds(final int userLimboMaxSeconds) {
		if (this.userLimboMaxSeconds > 0) {
			throw new IllegalStateException("UserLimboMaxSeconds cannot be reset");
		}
		this.userLimboMaxSeconds = userLimboMaxSeconds;
		this.limboCleanerTask = QAntServer.getInstance().getTaskScheduler()
				.scheduleAtFixedRate(new MMORoomCleaner(this), 0, 60, TimeUnit.SECONDS);
	}


	@Override
	public void destroy() {
		if (this.limboCleanerTask != null) {
			this.limboCleanerTask.cancel(true);
		}
		this.updateManager.destroy();
	}


	public boolean isSendAOIEntryPoint() {
		return this.sendAOIEntryPoint;
	}


	public void setSendAOIEntryPoint(final boolean sendAOIEntryPoint) {
		this.sendAOIEntryPoint = sendAOIEntryPoint;
	}


	public int getProximityListUpdateMillis() {
		return this.updateManager.getUpdateThreshold();
	}


	@Override
	public QAntUser getUserByPlayerId(final int playerId) {
		return null;
	}


	@Override
	public boolean isGame() {
		return false;
	}


	@Override
	public String toString() {
		return String.format("[ MMORoom: %s, Id: %s, Group: %s, AOI: %s ]", this.getName(), this.getId(),
				this.getGroupId(), this.getDefaultAOI());
	}


	@Override
	public List<QAntUser> getPlayersList() {
		throw new UnsupportedOperationException("MMO Room don't support players");
	}


	@Override
	public List<QAntUser> getSpectatorsList() {
		throw new UnsupportedOperationException("MMO Rooms don't support spectators");
	}


	@Override
	public void setGame(final boolean game) {
		super.setGame(false);
	}


	@Override
	public void setGame(final boolean game, final Class<? extends IPlayerIdGenerator> customPlayerIdGeneratorClass) {
		super.setGame(false, customPlayerIdGeneratorClass);
	}


	@Override
	public IQAntArray toQAntArray(boolean globalRoomVarsOnly) {
		final IQAntArray roomObj = super.toQAntArray(globalRoomVarsOnly);
		final boolean isFloat = this.getDefaultAOI().isFloat();
		roomObj.addNull();
		roomObj.addNull();
		if (isFloat) {
			roomObj.addFloatArray(this.getDefaultAOI().toFloatArray());
			if (this.lowLimit != null) {
				roomObj.addFloatArray(this.lowLimit.toFloatArray());
				roomObj.addFloatArray(this.highLimit.toFloatArray());
			} else {
				roomObj.addNull();
				roomObj.addNull();
			}
		} else {
			roomObj.addIntArray(this.getDefaultAOI().toIntArray());
			if (this.lowLimit != null) {
				roomObj.addIntArray(this.lowLimit.toIntArray());
				roomObj.addIntArray(this.highLimit.toIntArray());
			} else {
				roomObj.addNull();
				roomObj.addNull();
			}
		}
		return roomObj;
	}

	public static class PreviousMMORoomState {
		int roomId;
		List<QAntUser> proxyList;


		public PreviousMMORoomState(int roomId, List<QAntUser> proxyList) {
			this.roomId = roomId;
			this.proxyList = proxyList;
		}


		public List<QAntUser> getProxyList() {
			return this.proxyList;
		}


		public int getRoomId() {
			return this.roomId;
		}
	}

}
