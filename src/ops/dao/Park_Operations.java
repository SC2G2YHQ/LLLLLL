package ops.dao;

import dat.Token;

/*
 *ͣ���������� 
 * ���ˡ��������Ͷ�����������
 */

public interface Park_Operations {
	/*
	 * ռ�ú��ͷų�λ��Ҫƾ֤��
	 */
	public int lockPark(int location,Token token);
	public int releasePark(int location,Token token);
	
	/*
	 * ռ�ú��ͷų�����������Ҫ��֤��
	 */
	
	public int lockPath(int location);
	public int releasePath(int location);
	
	public int queryEmptyParkCount();
	
	/*
	 * ����ƾ֤����Ҫ��ICard�ŵ��������ϡ�����Ӧ��ģ�����
	 */
	
	public Token requestToken();
	
	public int leave();
	
	/*
	 * ��ȡ�������Ĳ����ӿ� ��ģ�⽫ICard�ŵ���������֮���̡�
	 */
	
	public CardReader_Operations getInCardReader();
	public CardReader_Operations getOutCardReader();
	
}
