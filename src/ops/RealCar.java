package ops;

import dat.ICard;
import ops.dao.CardReader_Operations;
import ops.dao.Park_Operations;

public class RealCar extends EventDriver.Car{
	
	public static final int CURRENT_STATUS_WAITING			= 0;
	public static final int CURRENT_STATUS_GOING_TO_PARK	= 1;
	public static final int CURRENT_STATUS_PARKING			= 2;
	public static final int CURRENT_STATUS_LEAVING			= 3;
	
	
	private ICard icard;
	private int wait_time;
	private int current_status=CURRENT_STATUS_WAITING;
	
	private Park_Operations p_op;
	private CardReader_Operations c_op;
	
	@Override
	public int run(Param param) {
		// TODO Auto-generated method stub
		
		switch(current_status){
		case CURRENT_STATUS_WAITING:
			//执行进入停车场操作。
			current_status=CURRENT_STATUS_GOING_TO_PARK;
			return EventDriver.STATUS_READY;
		}
		return 0;
	}
	
	
}
