package ops.dao;
/*
 * �������������豸�����������жϷ�ʽ���ء�
 * �������Ϸ������仯ʱ������������һ���жϲ�֪ͨPark��
 * �˳����޷�ģ���жϣ������ûص���ʽʵ�֡�
 * Ҳ�������ź���ʵ�֣����жϵķ�ʽ
 */

public interface Sensor_Operations {
	int registerSensorProc(SensorProc sp);
	int unRegisterSensorProc(SensorProc sp);
}

