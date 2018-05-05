package com.creants.creants_2x.core.api.response;

import java.util.List;

import com.creants.creants_2x.core.entities.QAntRoom;
import com.creants.creants_2x.mmo.BaseMMOItem;
import com.creants.creants_2x.mmo.IMMOItemVariable;
import com.creants.creants_2x.mmo.MMOUpdateDelta;

/**
 * @author LamHM
 *
 */
public interface IQAntMMOResponseApi {
	void notifyProximityListUpdate(QAntRoom room, MMOUpdateDelta p1);


	void notifyItemVariablesUpdate(BaseMMOItem mmoItem, List<IMMOItemVariable> itemVariables);
}
