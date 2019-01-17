package ops;

import java.security.interfaces.RSAPublicKey;

import dat.ICard;
import ops.dao.CardReader_Operations;

public class CardReader implements CardReader_Operations{

	/*
	 * 该属性模拟放在读卡器上的ICard。
	 * 将会一直存在到执行remove操作。
	 */
	private ICard icard;
	
	@Override
	public ICard getCard() {
		// TODO Auto-generated method stub
		return this.icard;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		if(this.icard!=null)
			return this.icard.getName();
		return null;
	}

	@Override
	public String getID() {
		// TODO Auto-generated method stub
		if(this.icard!=null)
			return this.icard.getID();
		return null;
	}

	@Override
	public RSAPublicKey getPublic_key() {
		// TODO Auto-generated method stub
		if(this.icard!=null)
			return this.icard.getPublic_key();
		return null;
	}

	@Override
	public byte[] getCipherData(byte[] plainTextData) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void putICard(ICard iCard) {
		// TODO Auto-generated method stub
		if(iCard!=null)
			if(this.icard==null)
				this.icard=iCard;
	}

	@Override
	public void removeICard() {
		// TODO Auto-generated method stub
		this.icard=null;
	}

	@Override
	public boolean control_ICardExist() {
		// TODO Auto-generated method stub
		if(this.icard!=null)
			return true;
		else
			return false;
	}

}
