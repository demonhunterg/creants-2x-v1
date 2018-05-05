package com.creants.creants_2x.mmo;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

import com.creants.creants_2x.core.entities.QAntRoom;

/**
 * @author LamHM
 *
 */
public class QAntMMORoom extends QAntRoom {
	private final IProximityManager proximityManager;
	private Vec3D lowLimit;
	private Vec3D highLimit;
	private int userLimboMaxSeconds;
	private ScheduledFuture<?> limboCleanerTask;
	private boolean sendAOIEntryPoint;


	public QAntMMORoom(String name, Vec3D aoi, int updateMillis) {
		super(name);
		this.userLimboMaxSeconds = 0;
		this.sendAOIEntryPoint = false;
		this.proximityManager = new ProximityManager(aoi);
		this.itemsManager = new MMOItemManager(aoi, this);
		this.updateManager = new MMOUpdateManager(this, updateMillis);
		this.itemsById = new ConcurrentHashMap<Integer, BaseMMOItem>();
	}
}
