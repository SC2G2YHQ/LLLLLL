package ops.dao;

import java.security.interfaces.RSAPublicKey;

import dat.ICard;
/*
 * 
 * ֻ���Ͷ���������֧��д��������
 * ����ǿɶ�д�ж�������������һ�������Ķ��������壬��Ҫ���� read write �� control ������
 * 
 */
public interface CardReader_Operations {
	ICard getCard();
	String getName();
	String getID();
	public RSAPublicKey getPublic_key();
	
	byte[] getCipherData(byte[] plainTextData);
	
	/*
	 * �˶����������ⷽ��������ģ�⽫���ŵ����������Լ����߿���
	 * һ��������һ��ֻ�ܶ�һ�������������봫�� �����á�
	 */
	void putICard(ICard iCard);
	void removeICard();
	
	/*
	 * ���� ����������û�п����˷�������control�෽���������� prefix control
	 */
	boolean control_ICardExist();
	
}
