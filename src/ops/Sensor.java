package ops;

import java.util.LinkedList;

import ops.dao.SensorProc;
import ops.dao.Sensor_Operations;

public class Sensor extends EventDriver.Car implements Sensor_Operations{

	private LinkedList<SensorProc> sensor_proc;//�ص����������¼����������λص����еĺ�����
	
	/*
	 * ��ʼ������
	 * ��Ҫ�ṩ���ӵ�λ�á�
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
		 * ѭ���ж��Ϸ��Ƿ���������
		 */
		
		return EventDriver.STATUS_READY;
	}

}
