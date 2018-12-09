package ops;

import java.util.List;

import dat.ICard;
import ops.dao.Card_Verification;

public class M {

	
	
	protected List<ICard> cards;
	
	protected Card_Verification cv;
	
	public int register_card_operations(Card_Verification cv){
		this.cv=cv;
		return 0;
	}
	

	
	
}
