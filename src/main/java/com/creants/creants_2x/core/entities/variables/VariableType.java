package com.creants.creants_2x.core.entities.variables;

/**
 * @author LamHM
 *
 */
public enum VariableType {
	NULL("NULL", 0, 0),
	BOOL("BOOL", 1, 1),
	INT("INT", 2, 2),
	DOUBLE("DOUBLE", 3, 3),
	STRING("STRING", 4, 4),
	OBJECT("OBJECT", 5, 5),
	ARRAY("ARRAY", 6, 6);

	private int id;


	private VariableType(String s, int n, int id) {
		this.id = id;
	}


	public int getId() {
		return this.id;
	}


	public static VariableType fromString(String id) {
		return valueOf(id.toUpperCase());
	}


	public static VariableType fromId(int id) {
		VariableType[] values;
		for (int length = (values = values()).length, i = 0; i < length; ++i) {
			VariableType type = values[i];
			if (type.id == id) {
				return type;
			}
		}
		return null;
	}
}
