package com.creants.creants_2x.mmo;

import java.util.List;

import com.creants.creants_2x.socket.gate.wood.QAntUser;

/**
 * @author LamHM
 *
 */
public interface IProximityManager {
	void addUser(QAntUser user);


	void updateUser(QAntUser user);


	void removeUser(QAntUser user);


	List<QAntUser> getProximityList(QAntUser user);


	List<QAntUser> getProximityList(QAntUser user, Vec3D vec3d);


	List<QAntUser> getProximityList(Vec3D vec3d);


	List<QAntUser> getProximityList(Vec3D vec3d1, Vec3D vec3d2);


	int getSize();


	Vec3D getSectorSize();


	Vec3D getDefaultAOI();
}
