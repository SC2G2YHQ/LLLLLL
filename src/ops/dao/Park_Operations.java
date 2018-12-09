package ops.dao;

import dat.Token;

public interface Park_Operations {
	/*
	 * ռ�ú��ͷų�λ��Ҫƾ֤��
	 */
	public int lockPark(int location,Token token);
	public int releasePark(int location,Token token);
	
	/*
	 * ռ�ú��ͷų�����������Ҫ��֤��
	 */
	
	public int lockPath(int location,int CarID);
	public int releasePath(int location,int CarID);
	
	public int queryEmptyParkCount();
	
	public Token requestToken();
}
