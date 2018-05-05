package com.creants.creants_2x.mmo;

import java.util.List;

import com.creants.creants_2x.socket.gate.wood.QAntUser;

/**
 * @author LamHM
 *
 */
public interface IMMOUpdateManager {
	void addUserToUpdate(QAntUser user);


	void addBatchToUpdate(List<QAntUser> users);


	void addItemToUpdate(BaseMMOItem mmoItem);


	int getUpdateThreshold();


	void setUpdateThreshold(int id);


	void destroy();
}
