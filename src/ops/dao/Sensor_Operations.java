package ops.dao;
/*
 * 传感器是特殊设备，数据是以中断方式返回。
 * 传感器上方发生变化时，传感器产生一个中断并通知Park。
 * 此程序无法模拟中断，所以用回调方式实现。
 * 也可以用信号量实现，软中断的方式
 * 为了准确，需要把sensor放在每轮的最后进行调度。
 */

public interface Sensor_Operations {
	int registerSensorProc(SensorProc sp,Park_Operations po,int location);
	int unRegisterSensorProc(SensorProc sp);
}

