package ops.dao;
/*
 * �������������豸�����������жϷ�ʽ���ء�
 * �������Ϸ������仯ʱ������������һ���жϲ�֪ͨPark��
 * �˳����޷�ģ���жϣ������ûص���ʽʵ�֡�
 * Ҳ�������ź���ʵ�֣����жϵķ�ʽ
 * Ϊ��׼ȷ����Ҫ��sensor����ÿ�ֵ������е��ȡ�
 */

public interface Sensor_Operations {
	int registerSensorProc(SensorProc sp,Park_Operations po,int location);
	int unRegisterSensorProc(SensorProc sp);
}

