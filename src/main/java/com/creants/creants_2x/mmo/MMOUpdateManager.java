package com.creants.creants_2x.mmo;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.creants.creants_2x.QAntServer;
import com.creants.creants_2x.core.api.response.IQAntMMOResponseApi;
import com.creants.creants_2x.core.exception.ExceptionMessageComposer;
import com.creants.creants_2x.core.util.QAntTracer;
import com.creants.creants_2x.socket.gate.wood.QAntUser;

/**
 * @author LamHM
 *
 */
public final class MMOUpdateManager implements IMMOUpdateManager {
	private final MMORoom mmoRoom;
	private final List<QAntUser> usersToUpdate;
	private final List<BaseMMOItem> itemsToUpdate;
	private final ScheduledFuture<?> updateTask;
	private final IQAntMMOResponseApi responseAPI;
	private volatile int threshold;


	public MMOUpdateManager(MMORoom room, int thresholdMillis) {
		this.mmoRoom = room;
		this.threshold = thresholdMillis;
		this.usersToUpdate = new LinkedList<QAntUser>();
		this.itemsToUpdate = new LinkedList<BaseMMOItem>();
		this.responseAPI = QAntServer.getInstance().getAPIManager().getMMOApi().getResponseAPI();
		this.updateTask = QAntServer.getInstance().getTaskScheduler().scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				MMOUpdateManager.this.executeUpdate();
			}
		}, 0, thresholdMillis, TimeUnit.MILLISECONDS);
	}


	@Override
	public void addUserToUpdate(QAntUser user) {
		synchronized (usersToUpdate) {
			if (!usersToUpdate.contains(user))
				usersToUpdate.add(user);
		}
		// monitorexit(this.usersToUpdate)
	}


	@Override
	public void addBatchToUpdate(List<QAntUser> users) {
		synchronized (usersToUpdate) {
			usersToUpdate
					.addAll(users.stream().filter(user -> usersToUpdate.contains(user)).collect(Collectors.toList()));
		}
		// monitorexit(this.usersToUpdate)
	}


	@Override
	public void addItemToUpdate(BaseMMOItem item) {
		synchronized (itemsToUpdate) {
			if (!itemsToUpdate.contains(item))
				itemsToUpdate.add(item);
		}
		// monitorexit(this.itemsToUpdate)
	}


	@Override
	public int getUpdateThreshold() {
		return this.threshold;
	}


	@Override
	public void setUpdateThreshold(final int millis) {
		this.threshold = millis;
	}


	@Override
	public void destroy() {
		if (this.updateTask != null) {
			this.updateTask.cancel(true);
		}
	}


	private void executeUpdate() {
		try {
			if (usersToUpdate.size() == 0 && itemsToUpdate.size() == 0)
				return;

			Set<QAntUser> allAffectedUsers = new HashSet<QAntUser>();
			List<QAntUser> usersToUpdateCopy = null;
			synchronized (usersToUpdate) {
				usersToUpdateCopy = new LinkedList<QAntUser>(usersToUpdate);
				usersToUpdate.clear();
			}

			// monitorexit(this.usersToUpdate)
			usersToUpdateCopy.addAll(findUsersAffectedByItemsUpdate());
			for (QAntUser user : usersToUpdateCopy) {
				computeAndUpdateUsers(user, allAffectedUsers);
			}

			for (QAntUser affectedUser : allAffectedUsers) {
				boolean userWasNotAlreadyUpdated = !usersToUpdateCopy.contains(affectedUser);
				if (userWasNotAlreadyUpdated) {
					computeAndUpdateUsers(affectedUser);
				}
			}
		} catch (Exception e) {
			ExceptionMessageComposer emc = new ExceptionMessageComposer(e);
			emc.setDescription("Unexpected error in update task!");
			emc.addInfo("Room: " + mmoRoom.toString());
			QAntTracer.warn(this.getClass(), emc.toString());
		}
	}


	private void computeAndUpdateUsers(QAntUser user) {
		this.computeAndUpdateUsers(user, null);
	}


	private void computeAndUpdateUsers(QAntUser user, Set<QAntUser> allAffectedUsers) {
		if (user.getCurrentMMORoom() != mmoRoom) {
			return;
		}

		List<QAntUser> newProxyList = mmoRoom.getProximityManager().getProximityList(user);
		List<BaseMMOItem> newItemsList = mmoRoom.getItemsManager().getItemList(user);
		if (newProxyList == null || newItemsList == null)
			return;

		List<QAntUser> plusUserList = new LinkedList<>(newProxyList);
		List<QAntUser> lastProxyList = user.getLastProxyList();
		List<QAntUser> minusUserList = lastProxyList == null ? null : new LinkedList<>(lastProxyList);
		List<BaseMMOItem> plusItemList = new LinkedList<BaseMMOItem>(newItemsList);
		List<BaseMMOItem> lastMMOItemsList = user.getLastMMOItemsList();
		List<BaseMMOItem> minusItemList = lastMMOItemsList == null ? null : new LinkedList<>(lastMMOItemsList);

		user.setLastProxyList(newProxyList);
		user.setLastMMOItemsList(newItemsList);
		final boolean previousUserListExists = minusUserList != null;
		final boolean previousItemListExists = minusItemList != null;
		if (allAffectedUsers != null) {
			allAffectedUsers.addAll(plusUserList);
			if (previousUserListExists) {
				allAffectedUsers.addAll(minusUserList);
			}
		}
		if (previousUserListExists) {
			Iterator<QAntUser> it = minusUserList.iterator();
			while (it.hasNext()) {
				final QAntUser item = it.next();
				if (plusUserList.contains(item)) {
					it.remove();
					plusUserList.remove(item);
				}
			}
		}
		if (previousItemListExists) {
			final Iterator<BaseMMOItem> it2 = minusItemList.iterator();
			while (it2.hasNext()) {
				final BaseMMOItem item2 = it2.next();
				if (plusItemList.contains(item2)) {
					it2.remove();
					plusItemList.remove(item2);
				}
			}
		}
		boolean needsUpdate = plusUserList != null && plusUserList.size() > 0;
		needsUpdate |= (minusUserList != null && minusUserList.size() > 0);
		needsUpdate |= (plusItemList != null && plusItemList.size() > 0);
		needsUpdate |= (minusItemList != null && minusItemList.size() > 0);
		if (needsUpdate) {
			this.responseAPI.notifyProximityListUpdate(this.mmoRoom,
					new MMOUpdateDelta(user, plusUserList, minusUserList, plusItemList, minusItemList));
		}
	}


	private Set<QAntUser> findUsersAffectedByItemsUpdate() {
		Set<QAntUser> affectedUsers = new HashSet<QAntUser>();
		List<BaseMMOItem> itemsToUpdateCopy = null;
		synchronized (this.itemsToUpdate) {
			itemsToUpdateCopy = new LinkedList<BaseMMOItem>(this.itemsToUpdate);
			this.itemsToUpdate.clear();
		}
		// monitorexit(this.itemsToUpdate)
		for (final BaseMMOItem item : itemsToUpdateCopy) {
			affectedUsers.addAll(this.findUsersAffectedByThisItem(item));
		}
		return affectedUsers;
	}


	private Collection<QAntUser> findUsersAffectedByThisItem(BaseMMOItem item) {
		Set<QAntUser> userList = new HashSet<QAntUser>();
		List<QAntUser> oldUsers = item.getLastProxyList();
		if (oldUsers != null) {
			userList.addAll(oldUsers);
		}

		List<QAntUser> newUsers = this.mmoRoom.getProximityManager().getProximityList(item.getLastLocation());
		userList.addAll(newUsers);
		return userList;
	}
}
