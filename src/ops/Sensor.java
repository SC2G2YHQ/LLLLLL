package ops;

import java.util.LinkedList;

import ops.dao.SensorProc;
import ops.dao.Sensor_Operations;

public class Sensor extends EventDriver.Car implements Sensor_Operations{

	private LinkedList<SensorProc> sensor_proc;//回调函数集。事件产生后依次回调所有的函数。
	
	/*
	 * 初始化函数
	 * 需要提供监视的位置。
	 */
	
	public void init(){
		
	}
	
	@Override
	public int registerSensorProc(SensorProc sp) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int unRegisterSensorProc(SensorProc sp) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int run(Param param) {
		// TODO Auto-generated method stub
		
		/*
		 * 循环判断上方是否有汽车。
		 */
		
		return EventDriver.STATUS_READY;
	}

}
