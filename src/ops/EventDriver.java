package ops;

import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;

/*
 * 要想实现争用调度 必须用优先级
 */

public /*abstract*/ class EventDriver {
	public static final int STATUS_EXIT		= -1;
	public static final int STATUS_READY	= 0;
	public static final int STATUS_RUNNING	= 1;
	public static final int STATUS_WAIT_SIG	= 2;
	public static final int STATUS_WAIT_TIM	= 3;
	public static final int STATUS_SUSPEND	= 4;
	
	public static final int CARS_LIMIT		= 100;
	
	public static final int OPCODE_NULL		= 0;
	public static final int OPCODE_SENDSIG	= 1;
	public static final int OPCODE_SUSPEND	= 2;
	public static final int OPCODE_ADD		= 3;
	public static final int OPCODE_REMOVE	= 4;
	
	public EventDriver(){}
	
	private boolean running = false;
	private Thread tc;
	/*
	 * 该类名字是汽车，实际是通用线程接口
	 * 最初将名字定为了Car
	 * 包含了Card Reader，Rail 及Sensor任务。
	 * 发送信号需要能传参
	 */
	
	public static abstract class Car{
		int ID;
		int status;
		int time_wait_sec;

		
		public class Param{
			int param;
			EventDriver ed;
		}
		
		public Car(){this.status=STATUS_READY;}//所有线程需要有初始状态。初始为 就绪
		
		public abstract int run(Param param);
	}
	
	
	/*
	 * 汽车不再是通用线程，有自己特定的回调函数集。
	 */
	public static abstract class RCar{
		/*
		 * 7种状态码分别对应为
		 * 1.中途操作失败离场。目前只有icard无效，进入停车场失败的情况
		 * 2.入场排队
		 * 3.在行车道上寻找车位
		 * 4.停车中
		 * 5.在行车道上正在离开停车场
		 * 6.离开失败，正在返回原车位或最近的车位
		 * 7.僵死，离开失败并重新停在停车位之后的状态。
		 * 
		 * 这6种状态码为互斥码，所以简单的整数序列即可。
		 * 4和5不一定出现。如果离开成功的话。
		 * 
		 * 调度器根据返回值确定汽车的状态。
		 * 
		 * 请求进入停车场操作最复杂
		 * 状态码不通过返回值来传，通过param参数来传。返回值仍然传的是调度信息。
		 * 
		 * 0表示维持当前状态。
		 * 
		 * 这种定义有不完善之处
		 */
		public static final int CAR_STATUS_FA	= 1;
		public static final int CAR_STATUS_WA	= 2;
		public static final int CAR_STATUS_SE	= 3;
		public static final int CAR_STATUS_PA	= 4;
		public static final int CAR_STATUS_LE	= 5;
		public static final int CAR_STATUS_RE	= 6;
		public static final int CAR_STATUS_DE	= 7;
		
		int ID;
		int status;
		int time_wait_sec;

		/*
		 * 传参结构，不同结构之间使用此结构进行传参。
		 * 每一个RCar实例应该都有一个对应的param结构。
		 * 并且该结构应该是静态变量，不声明在函数中。
		 * 可以有一个param组，构成消息队列
		 */
		public class Param{
			int param;
			int car_status;
			EventDriver ed;
			Object obj;
		}
		int locked_location;
		Param p_sig;
		
		public RCar(){this.status=CAR_STATUS_WA;}//生成的汽车初始为排队等待状态。
		
		/*
		 * wa函数执行请求进入停车场的操作
		 */
		public abstract int wa(Param param);
		
		public abstract int se(Param param);
		public abstract int pa(Param param);
		public abstract int le(Param param);
		public abstract int re(Param param);
		public abstract int de(Param param);
		
	}


	/*
	 * 不同的队列。 包括：就绪、睡眠、时间等待。
	 * 同一队列下也要有顺序。
	 * 使用不同的队列是为了实现优先级。
	 * 优先级顺序为：停车>离场车>入场车>排队车>外围任务 包括传感器，栏杆等
	 * 正在停的车不会进行任何操作。对应代码为休息
	 */
	
	private LinkedList<RCar> wa_cars;
	
	private LinkedList<RCar> se_cars;
	private LinkedList<RCar> pa_cars;
	private LinkedList<RCar> le_cars;
	private LinkedList<RCar> re_cars;
	private LinkedList<RCar> de_cars;
	
	/*
	 * 其他任务的调度队列。该队列优先级最低。
	 */
	private LinkedList<Car> cars;
	
	private int op_code;
	private Car op_car;
	
	/*
	 * 当前调度汽车的ID
	 */
	
	public static int current_car_id;
	
	/*
	 *已经分配的汽车的ID，新的ID由当前ID加1得到。
	 *撤销汽车并不会导致该变量减小 
	 */
	
	private int allocated_car_id;
	
	public void init(){
		this.cars = new LinkedList<Car>();
		this.wa_cars = new LinkedList<RCar>();
		this.se_cars = new LinkedList<RCar>();
		this.pa_cars = new LinkedList<RCar>();
		this.le_cars = new LinkedList<RCar>();
		this.re_cars = new LinkedList<RCar>();
		this.de_cars = new LinkedList<RCar>();
		
		this.op_code=OPCODE_NULL;
		this.allocated_car_id=0;
	}
	
	private void tick(Car c){
		int return_status;
		Car.Param param=c.new Param();
		param.ed=this;
		
		c.status=STATUS_RUNNING;
		current_car_id=c.ID;
		
		return_status=c.run(param);
		switch(return_status){
		case STATUS_WAIT_SIG:
			c.status=STATUS_WAIT_SIG;
			break;
		case STATUS_WAIT_TIM:
			c.time_wait_sec=param.param;
			c.status=STATUS_WAIT_TIM;
			break;
		default:
			c.status=STATUS_READY;
		}
		
	}
	
	private Car getCarByID(int CarID){
		if(cars.isEmpty())
			return null;
		else{
			Iterator<Car> it=cars.iterator();
			while(it.hasNext()){
				Car c=it.next();
				if(c.ID==CarID)
					return c;
			}
		}
		return null;
	}
	
	public void launch(){
		System.out.println("ticks");
		while(running){
			/*
			 * 每次调度之前先进行车辆操作。
			 */
			switch(this.op_code){
			case OPCODE_NULL:
				break;
			case OPCODE_SENDSIG:
				if(cars.contains(this.op_car)){
					this.op_car.status=STATUS_READY;
				}
				break;
			case OPCODE_SUSPEND:
				if(cars.contains(this.op_car)){
					this.op_car.status=STATUS_SUSPEND;
				}
				break;
			case OPCODE_ADD:
				if(!cars.contains(this.op_car)){
					this.cars.add(op_car);
				}
				break;
			case OPCODE_REMOVE:
				if(cars.contains(this.op_car)){
					this.cars.remove(op_car);
				}
				break;
			default:
				//无效操作码
				break;
			}
			
			//将操作码置为空操作，不然下一轮会重复操作
			this.op_code=OPCODE_NULL;
			
			/*
			 * 开始一轮调度
			 */
			
			/*
			 * 一共5个汽车调度队列。为了方便，只处理wa和le队列的时间等待请求。
			 * 其他队列的任何请求都不理会。
			 * 通用队列则要处理所有请求
			 */
			
			/*
			 * 为停车的汽车计时
			 */
			for(RCar rc:pa_cars)
			{
				rc.time_wait_sec--;
				if(rc.time_wait_sec<0){//将车放入离开队列
					le_cars.add(rc);
					le_cars.sort(new Comparator<RCar>() {

						@Override
						public int compare(RCar o1, RCar o2) {
							// TODO Auto-generated method stub
							return o1.locked_location-o2.locked_location;
						}
					});//排序很重要,排序依据停车位置。靠近入口的先被调度
					//从停车队列删去
					pa_cars.remove(rc);
				}
			}
			
			/*
			 * 离场汽车。需要两个等待：放iCard到读卡器 以及 等待栏杆
			 */
			
			for(RCar rc:le_cars)
			{
				//le队列的有两个等待操作
				RCar.Param p=rc.new Param();
				switch(rc.status){
				case STATUS_READY:
					p.ed=this;
					
					current_car_id=rc.ID;
					
					rc.status=STATUS_RUNNING;
					rc.status=rc.le(p);
					break;
				case STATUS_WAIT_TIM:
					rc.time_wait_sec--;
					if(rc.time_wait_sec<0){
						p.ed=this;
						
						current_car_id=rc.ID;
						
						rc.status=STATUS_RUNNING;
						rc.status=rc.le(p);
					}
				}
				//判断汽车是否离场成功
				//exit表示已经退出当前操作
				//再根据param中的car_status字段判断
				if(rc.status==STATUS_EXIT){
					if(p.car_status==RCar.CAR_STATUS_RE){
						//离开失败,将汽车放入返回队列
						this.re_cars.add(rc);
						rc.status=STATUS_READY;
						this.le_cars.remove(rc);

					}else{
						//离开成功,将汽车从离开队列中移除
						this.le_cars.remove(rc);
					}
				}
				
			}
			
			/*
			 * 离场失败的汽车。
			 */
			for(RCar rc:re_cars)
			{
				RCar.Param p=rc.new Param();
				p.ed=this;
				if(rc.status==STATUS_WAIT_TIM){
					rc.time_wait_sec--;
					if(rc.time_wait_sec>=0)continue;//等待周期没有完成 放弃本轮调度
				}
				
				
				current_car_id=rc.ID;
				
				rc.status=STATUS_RUNNING;
				rc.status=rc.le(p);
				
				if(rc.status==STATUS_EXIT){
					//不响应任何请求，所以不用对param进行判断
					//退出着进入永生汽车队列
					this.de_cars.add(rc);
					this.re_cars.remove(rc);
				}else if(rc.status==STATUS_WAIT_TIM){
					rc.time_wait_sec=p.param;
				}
				
				
			}
			
			/*
			 * 入场的汽车
			 */
			for(RCar rc:se_cars)
			{
				RCar.Param p=rc.new Param();
				p.ed=this;
				/*
				 * 占车位需要两个等待周期
				 */
				if(rc.status==STATUS_WAIT_TIM){
					rc.time_wait_sec--;
					if(rc.time_wait_sec>=0)continue;//等待周期没有完成 放弃本轮调度
				}
				current_car_id=rc.ID;
				
				rc.status=STATUS_RUNNING;
				rc.status=rc.le(p);
				
				if(rc.status==STATUS_EXIT){
					//退出则意味着进入停车队列
					//停车多长由param传入
					rc.status=STATUS_WAIT_TIM;
					rc.time_wait_sec=p.param;
					this.de_cars.add(rc);
					this.re_cars.remove(rc);
				}else if(rc.status==STATUS_WAIT_TIM){
					rc.time_wait_sec=p.param;
				}
			}
			

			for(RCar rc:de_cars)
			{
				/*
				 *  de 队列不调度，永生汽车
				 */
				;
			}
			Iterator<Car> it=cars.iterator();
			while(it.hasNext()){
				Car c=it.next();
				switch(c.status){
				case STATUS_READY:
					tick(c);
					break;
				case STATUS_WAIT_TIM:
					if(c.time_wait_sec>0)
						c.time_wait_sec--;
					else
					{
						tick(c);
					}
					break;
				case STATUS_WAIT_SIG:
					break;
				case STATUS_SUSPEND:
					break;
				default:
					break;
				}
			}
			
			//
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	/*
	 * 以下为原子操作函数，需要特殊处理。
	 */
	
	public void sendSIG(int CarID){
		Car car=getCarByID(CarID);
		this.op_car=car;
		this.op_code=OPCODE_SENDSIG;
	}
	
	/*
	 * 挂起操作不能马上上生效，需要等当前汽车调度完成。
	 */
	public void suspend(int CarID){
		Car car=getCarByID(CarID);
		this.op_car=car;
		this.op_code=OPCODE_SUSPEND;
	}
	
	/*
	 *添加操作不能异步
	 *但此处是异步，可能会存在漏洞 
	 */
	public int addCar(Car car){
		if(this.cars.size()>CARS_LIMIT)
			return -1;
		if(this.cars.contains(car))
			return -1;
		this.op_car=car;
		this.op_code=OPCODE_ADD;
		return ++this.allocated_car_id;
	}
	
	public int removeCar(int CarID){
		Car car=getCarByID(CarID);
		
		if(car==null)
			return -1;
		this.op_car=car;
		this.op_code=OPCODE_REMOVE;
		return 0;
		
	}
	
	/*
	 * 整个模拟系统中只可能有一个EventDriver
	 * 所以获取当前Car的ID可用静态方法
	 * 等同于Thread.currentThread()
	 */
	public static int getCurrentCarID(){
		return current_car_id;
	}
	
	/*
	 * 立即添加汽车和任务块
	 * 只有在调度器停止的时候才有效
	 * 只能添加到排队队列中
	 */
	public int addRCar(RCar rc){
		//重复添加汽车怎么办？
		//偷懒。只对排队队列中的汽车进行重复验证。
		if(this.wa_cars.contains(rc))
			return -1;
		else{
			rc.ID=++allocated_car_id;
			//需要插入开头
			this.wa_cars.add(0, rc);
			return allocated_car_id;
		}
	}
	
	public int addCar_IMMEDIATELY(Car c){
		if(tc!=null)
			if(this.cars.contains(c))
				return -1;
			else{
				c.ID=++allocated_car_id;
				this.cars.add(c);
				return allocated_car_id;
			}
		else
			return -1;
	}
	
	/*
	 * 
	 */
	public void pause(){
		this.running=false;
	}
	
	public void __continue(){
		this.running=true;
	}
	
	/*
	 * stop will clean all resources
	 * 
	 * 停止操作会清理所有汽车和任务，包括调度线程
	 */
	public void stop(){
		if(this.tc!=null){
			tc.stop();
			tc.destroy();
			this.cars.clear();
			this.se_cars.clear();
			this.pa_cars.clear();
			this.le_cars.clear();
			this.re_cars.clear();
			this.de_cars.clear();
			this.wa_cars.clear();
			this.tc=null;
		}
	}
	
	public void start(){
		//不允许重复开始
		if(this.tc==null){
			Thread td=new Thread(){
				@Override
				public void run(){
					running = true;
					launch();
				}
			};
			this.tc=td;
			this.tc.start();
		}
		
	}
	
	/*
	 * 激活等待队列中的汽车。将其放入入场队列
	 * 等待队列只是为了模拟。
	 * 如果能在停车场外排队，就没有必要建停车厂了
	 */
	public void sigToWaCar(){
		if(!this.wa_cars.isEmpty()){
			RCar c=this.wa_cars.removeFirst();
			c.status=STATUS_READY;
			this.se_cars.add(c);
		}
	}
}
