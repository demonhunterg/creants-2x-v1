package com.creants.creants_2x.core.entities.variables;

import com.creants.creants_2x.socket.gate.entities.IQAntArray;
import com.creants.creants_2x.socket.gate.entities.IQAntObject;

/**
 * @author LamHM
 *
 */
public interface Variable extends Cloneable {
	String getName();


	VariableType getType();


	Object getValue();


	Boolean getBoolValue();


	Integer getIntValue();


	Double getDoubleValue();


	String getStringValue();


	IQAntObject getQAntObjectValue();


	IQAntArray getQAntArrayValue();


	boolean isNull();


	IQAntArray toQAntArray();
}
