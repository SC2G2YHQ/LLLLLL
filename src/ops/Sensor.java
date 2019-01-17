package ops;


import ops.dao.Park_Operations;
import ops.dao.SensorProc;
import ops.dao.Sensor_Operations;

public class Sensor extends EventDriver.Car implements Sensor_Operations{

	private SensorProc sensor_proc;//回调函数。
	private Park_Operations po;
	//监视位置的情况
	//此与path锁定情况相关
	boolean current_staus=false;
	int location;
	/*
	 * 初始化函数
	 * 需要提供监视的位置。
	 */
	
	public void init(){
		
	}
	
	@Override
	public int registerSensorProc(SensorProc sp,Park_Operations po,int location) {
		// TODO Auto-generated method stub
		if(sp!=null)
		{
			this.sensor_proc=sp;
			this.po=po;
		}
		return 0;
	}

	@Override
	public int unRegisterSensorProc(SensorProc sp) {
		// TODO Auto-generated method stub
		this.sensor_proc=null;
		return 0;
	}

	@Override
	public int run(Param param) {
		// TODO Auto-generated method stub
		
		/*
		 * 循环判断上方是否有汽车。
		 */
		boolean st=po.get(location);
		//只有状态发生变化时才回调
		if(st!=this.current_staus){
			this.current_staus=st;
			
			//注意空指针
			this.sensor_proc.lpfnSensorProc(this.location,st);
		}
		return EventDriver.STATUS_READY;
	}

}
