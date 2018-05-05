package com.creants.creants_2x.core.api;

import java.util.List;

import com.creants.creants_2x.core.api.response.IQAntMMOResponseApi;
import com.creants.creants_2x.core.entities.QAntRoom;
import com.creants.creants_2x.core.entities.Room;
import com.creants.creants_2x.mmo.BaseMMOItem;
import com.creants.creants_2x.mmo.IMMOItemVariable;
import com.creants.creants_2x.mmo.Vec3D;
import com.creants.creants_2x.socket.gate.entities.IQAntObject;
import com.creants.creants_2x.socket.gate.wood.QAntUser;

/**
 * @author LamHM
 *
 */
public interface IQAntMMOApi {
	IQAntMMOResponseApi getResponseAPI();


	void sendObjectMessage(QAntRoom room, QAntUser user, IQAntObject params, Vec3D vec3d);


	void sendPublicMessage(QAntRoom room, QAntUser user, String message, IQAntObject params, Vec3D vec3d);


	void setUserPosition(QAntUser user, Vec3D vec3d, Room room);


	void setMMOItemPosition(BaseMMOItem mmoItem, Vec3D vec3d, Room room);


	void removeMMOItem(BaseMMOItem mmoItem);


	void setMMOItemVariables(BaseMMOItem mmoItem, List<IMMOItemVariable> itemVariables);


	void setMMOItemVariables(BaseMMOItem mmoItem, List<IMMOItemVariable> variables, boolean p2);
}
