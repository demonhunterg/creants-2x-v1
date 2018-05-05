package com.creants.creants_2x.mmo;

import java.util.List;

import com.creants.creants_2x.socket.gate.wood.QAntUser;

/**
 * @author LamHM
 *
 */
public interface IMMOItemManager {
	void setItem(BaseMMOItem p0, Vec3D vec3d);


	void removeItem(BaseMMOItem p0);


	List<BaseMMOItem> getItemList(QAntUser user);


	List<BaseMMOItem> getItemList(QAntUser user, Vec3D vec3d);


	List<BaseMMOItem> getItemList(Vec3D vec3d);


	List<BaseMMOItem> getItemList(Vec3D vec3d1, Vec3D vec3d2);


	int getSize();
}
