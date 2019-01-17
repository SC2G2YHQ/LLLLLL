package bo;

import dat.ICard;
import ops.CardReader;
import ops.EventDriver;
import ops.M;
import ops.Park;
import ops.Rail;
import ops.RealCar;
import ops.Sensor;
import ops.dao.CardReader_Operations;
import ops.dao.M_Operations;
import ops.dao.Park_Operations;
import ops.dao.Rail_Operations;
import ops.dao.Sensor_Operations;

/*
 * 一个简单的启动代码。
 * 这一部分也可有架构。
 * 
 * 想想操作系统的加载过程。
 * 必须环境。统一描述
 * 
 * 需要一个简单I/O模型
 * 模式 字符
 */
public class Launcher {
	//参数
	//同一个操作集，不同类型，但逻辑等价。
	//
	public static final int MAX_PARK_LIMIT = 50;
	
	public static void main(String[] args) {
		//初始化的顺序
		/*
		 * 1.调度器
		 * 2.{
		 * M
		 * Rail
		 * CardReader
		 * Sensor
		 * }
		 * 3.Park
		 */
		EventDriver ed=new EventDriver();
		ed.init();
		
		M_Operations mo=new M();
		/*
		 * 随机生成一些ICard
		 * 使用GUID作为ID
		 */
		ICard ic1=new ICard();
		ic1.setID("abea80e30f4347ffb528ae113b950496");
		ic1.setName("嘿嘿嘿");
		ICard ic2=new ICard();
		ic2.setID("d34318e70cd845558ed8e0e62f4095d2");
		ic2.setName("哈哈哈");
		mo.insert(ic1);
		mo.insert(ic2);
		
		Rail_Operations r_in = new Rail();
		
		Rail_Operations r_out = new Rail();
		
		CardReader_Operations c_in = new CardReader();
		
		CardReader_Operations c_out = new CardReader();
		
		Sensor_Operations s_in = new Sensor();
		
		Sensor_Operations s_out = new Sensor();
		//初始化停车场
		
		Park_Operations po = new Park("无法使用的停车场",0,mo,c_in,c_out,r_in,r_out,s_in,s_out,MAX_PARK_LIMIT);
		/*
		 * 初始化一辆汽车
		 * 使用“嘿嘿嘿“ 的ICard
		 */
		RealCar rc1=new RealCar(3600,ic1);
		
		ed.addRCar(rc1);
		//不要调用此方法
		//不然程序会崩溃
		ed.start();
		//第一个信号
		ed.sigToWaCar();
	}
	
}
