package ops.dao;

/*
 * 处理传感器“中断”的函数
 */
interface SensorProc{
	void lpfnSensorProc(int SensorID,int data);
}
