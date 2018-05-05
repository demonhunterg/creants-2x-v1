package com.creants.creants_2x.mmo;

import java.util.List;

import com.creants.creants_2x.socket.gate.entities.IQAntArray;

/**
 * @author LamHM
 *
 */
public interface IMMOItem {
	int getId();


	IMMOItemVariable getVariable(String variableKey);


	List<IMMOItemVariable> getVariables();


	void setVariable(IMMOItemVariable itemVariable);


	void setVariables(List<IMMOItemVariable> itemValiables);


	void removeVariable(String variableKey);


	IQAntArray toSFSArray();
}
