package com.creants.creants_2x.core.entities;

/**
 * @author LamHM
 *
 */
public interface UserVariable {
	void setHidden(boolean isHidden);


	boolean isHidden();


	boolean isPrivate();


	void setPrivate(boolean isPrivate);


	void setNull();
}
