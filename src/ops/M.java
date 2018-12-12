package ops;

import java.util.List;

import dat.ICard;
import ops.dao.Card_Verification;
import ops.dao.M_Operations;

public class M implements M_Operations{

	
	
	protected List<ICard> cards;
	
	protected Card_Verification cv;
	
	public int register_card_operations(Card_Verification cv){
		this.cv=cv;
		return 0;
	}

	@Override
	public boolean exist(String ID) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean Verification(String name) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean Verification(byte[] cipherData) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public byte[] getRandomPlainTextData() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int insert(ICard icard) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int delete(ICard icard) {
		// TODO Auto-generated method stub
		return 0;
	}
	

	
	
}
