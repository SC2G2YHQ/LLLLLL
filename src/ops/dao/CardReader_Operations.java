package ops.dao;

import java.security.interfaces.RSAPublicKey;

import dat.ICard;
/*
 * 
 * 只读型读卡器，不支持写卡操作。
 * 如果是可读写行读卡器，或者是一个完整的读卡器定义，需要包括 read write 及 control 方法。
 * 
 */
public interface CardReader_Operations {
	ICard getCard();
	String getName();
	String getID();
	public RSAPublicKey getPublic_key();
	
	byte[] getCipherData(byte[] plainTextData);
	
	/*
	 * 此二方法是虚拟方法，用来模拟将卡放到读卡器上以及拿走卡。
	 * 一个读卡器一次只能读一个卡，所以无须传卡 的引用。
	 */
	void putICard(ICard iCard);
	void removeICard();
	
	/*
	 * 返回 读卡器上有没有卡。此方法属于control类方法，所以有 prefix control
	 */
	boolean control_ICardExist();
	
}
