package ops;


import ops.dao.Park_Operations;
import ops.dao.SensorProc;
import ops.dao.Sensor_Operations;

public class Sensor extends EventDriver.Car implements Sensor_Operations{

	private SensorProc sensor_proc;//�ص�������
	private Park_Operations po;
	//����λ�õ����
	//����path����������
	boolean current_staus=false;
	int location;
	/*
	 * ��ʼ������
	 * ��Ҫ�ṩ���ӵ�λ�á�
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
		 * ѭ���ж��Ϸ��Ƿ���������
		 */
		boolean st=po.get(location);
		//ֻ��״̬�����仯ʱ�Żص�
		if(st!=this.current_staus){
			this.current_staus=st;
			
			//ע���ָ��
			this.sensor_proc.lpfnSensorProc(this.location,st);
		}
		return EventDriver.STATUS_READY;
	}

}
