package com.creants.creants_2x.mmo;

import java.util.List;

import com.creants.creants_2x.socket.gate.wood.QAntUser;

/**
 * @author LamHM
 *
 */
public class MMOUpdateDelta {
	private final List<QAntUser> plusUserList;
	private final List<QAntUser> minusUserList;
	private final List<BaseMMOItem> plusItemList;
	private final List<BaseMMOItem> minusItemList;
	private final QAntUser recipient;


	public MMOUpdateDelta(final QAntUser recipient, final List<QAntUser> plusUserList,
			final List<QAntUser> minusUserList, final List<BaseMMOItem> plusItemList,
			final List<BaseMMOItem> minusItemList) {
		this.recipient = recipient;
		this.plusUserList = plusUserList;
		this.minusUserList = minusUserList;
		this.plusItemList = plusItemList;
		this.minusItemList = minusItemList;
	}


	public List<QAntUser> getPlusUserList() {
		return this.plusUserList;
	}


	public List<QAntUser> getMinusUserList() {
		return this.minusUserList;
	}


	public List<BaseMMOItem> getPlusItemList() {
		return this.plusItemList;
	}


	public List<BaseMMOItem> getMinusItemList() {
		return this.minusItemList;
	}


	public QAntUser getRecipient() {
		return this.recipient;
	}


	@Override
	public String toString() {
		return String.format("{\n  Update: %s\n  -U: %s\n  +U: %s\n -I: %s\n +I: %s\n}\n", this.recipient.getName(),
				this.minusUserList, this.plusUserList, this.minusItemList, this.plusItemList);
	}
}
