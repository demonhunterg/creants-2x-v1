package com.creants.creants_2x.mmo;

import java.util.Collection;

import com.creants.creants_2x.QAntServer;
import com.creants.creants_2x.core.api.IQAntApi;
import com.creants.creants_2x.core.exception.ExceptionMessageComposer;
import com.creants.creants_2x.core.util.QAntTracer;
import com.creants.creants_2x.socket.gate.wood.QAntUser;

/**
 * @author LamHM
 *
 */
class MMORoomCleaner implements Runnable {
	private final MMORoom targetRoom;
	private final IQAntApi qantApi;
	private final long allowedTime;


	public MMORoomCleaner(final MMORoom targetRoom) {
		this.targetRoom = targetRoom;
		this.qantApi = QAntServer.getInstance().getAPIManager().getQAntApi();
		this.allowedTime = targetRoom.getUserLimboMaxSeconds() * 1000;
	}


	@Override
	public void run() {
		try {
			Collection<QAntUser> allUsers = targetRoom.getUserManager().getDirectUserList();
			for (QAntUser u : allUsers) {
				if (u.containsProperty("_uLoc"))
					continue;

				long joinTime = (long) u.getProperty("_uJoinTime");
				if (System.currentTimeMillis() <= joinTime + this.allowedTime)
					continue;

				kickUserOut(u);
			}

		} catch (Exception e) {
			final ExceptionMessageComposer emc = new ExceptionMessageComposer(e);
			emc.setDescription("Limbo cleaner encountered an error!");
			emc.addInfo("Room: " + this.targetRoom.toString());
			QAntTracer.warn(this.getClass(), emc.toString());
		}
	}


	private void kickUserOut(QAntUser u) {
		QAntTracer.debug(this.getClass(), "User: " + u + " kicked out of " + this.targetRoom);
		qantApi.leaveRoom(u, targetRoom);
	}
}
