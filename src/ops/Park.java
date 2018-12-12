package ops;

import java.util.HashSet;

import dat.Token;
import ops.dao.CardReader_Operations;
import ops.dao.Park_Operations;

public class Park implements Park_Operations{
	private String Name;
	private long id;
	
	private ops.dao.M_Operations m_op;
	
	private ops.dao.CardReader_Operations c_op_in;
	private ops.dao.CardReader_Operations c_op_out;
	
	private ops.dao.Rail_Operations r_op_in;
	private ops.dao.Rail_Operations r_op_out;
	
	private ops.dao.Sensor_Operations s_op_in;
	private ops.dao.Sensor_Operations s_op_out;
	
	private ops.EventDriver event_driver;//Park的代码也是由Event Driver调度的。所以event_driver应该是由param传进。
	
	/*
	 * 停车位和行车道也可以单独成类。
	 * Path比停车位多了2个车位宽度。
	 * 
	 */
	
	private boolean left[];
	private boolean right[];
	private boolean path[];

	public void init(){
		
		
	}

	@Override
	public int lockPark(int location, Token token) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int releasePark(int location, Token token) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int lockPath(int location) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int releasePath(int location) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int queryEmptyParkCount() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Token requestToken() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int leave() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public CardReader_Operations getInCardReader() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CardReader_Operations getOutCardReader() {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	
}
