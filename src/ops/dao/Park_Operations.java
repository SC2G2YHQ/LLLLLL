package ops.dao;

import dat.Token;

public interface Park_Operations {
	/*
	 * 占用和释放车位需要凭证。
	 */
	public int lockPark(int location,Token token);
	public int releasePark(int location,Token token);
	
	/*
	 * 占用和释放车道理论上需要验证。
	 */
	
	public int lockPath(int location,int CarID);
	public int releasePath(int location,int CarID);
	
	public int queryEmptyParkCount();
	
	public Token requestToken();
}
