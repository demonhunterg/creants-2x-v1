package com.creants.creants_2x.mmo;

import java.util.List;

import com.creants.creants_2x.socket.gate.wood.QAntUser;

/**
 * @author LamHM
 *
 */
public abstract class BaseMMOItem  implements IMMOItem{
	abstract P3D getLastPos();


	abstract void setLastPos(P3D p0);


	abstract Vec3D getLastLocation();


	abstract void setLastLocation(Vec3D vec3d);


	abstract List<QAntUser> getLastProxyList();


	abstract void setLastProxyList(List<QAntUser> p0);


	public abstract MMORoom getRoom();


	abstract void setRoom(MMORoom p0);
}
