package ops.dao;

import dat.ICard;

/*
 * ��֧�ֲ������ʡ�
 * ����������Ч��
 * �ܷ񲢷�����ȡ���ڶ����󷽵Ķ���
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
