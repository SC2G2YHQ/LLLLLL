package ops.dao;

import dat.Token;

/*
 *停车场操作集 
 * 栏杆、传感器和读卡器有两套
 */

public interface Park_Operations {
	/*
	 * 占用和释放车位需要凭证。
	 */
	public int lockPark(int location,Token token);
	public int releasePark(int location,Token token);
	
	/*
	 * 占用和释放车道理论上需要验证。
	 */
	
	public int lockPath(int location);
	public int releasePath(int location);
	
	public int queryEmptyParkCount();
	
	/*
	 * 请求凭证，需要把ICard放到读卡器上。有相应的模拟操作
	 */
	
	public Token requestToken();
	
	public int leave();
	
	/*
	 * 获取读卡器的操作接口 以模拟将ICard放到读卡器上之过程。
	 */
	
	public CardReader_Operations getInCardReader();
	public CardReader_Operations getOutCardReader();
	
}
