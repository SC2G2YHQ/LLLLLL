package ops.dao;

import dat.ICard;

/*
 * 不支持并发访问。
 * 并发访问无效。
 * 能否并发访问取决于对请求方的定义
 */
public interface M_Operations {
	public static final int SUCCESS				= 0x0;
	public static final int ERROR_FULL_LIST		= 0x1;
	public static final int ERROR_INVALID_CARD	= 0x2;
	public static final int ERROR_EXIST_CARD	= 0x4;
	public static final int ERROR_NULL_LIST		= 0x8;
	
	
	boolean exist(String ID);
	boolean Verification(String name);
	boolean Verification(byte[] cipherData);
	
	byte[] getRandomPlainTextData();
	
	public abstract int insert(ICard icard);
	public abstract int delete(ICard icard);
}
