package ops;

import java.util.Iterator;
import java.util.LinkedList;

/*
 * 要想实现争用调度 必须用优先级
 */

public abstract class EventDriver {
	
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
	
	/*
	 * 该类名字是汽车，实际是通用线程接口
	 * 最初将名字定为了Car
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
		 * 6种状态码分别对应为
		 * 0.入场排队
		 * 1.在行车道上寻找车位
		 * 2.停车中
		 * 3.在行车道上正在离开停车场
		 * 4.离开失败，正在返回原车位或最近的车位
		 * 5.僵死，离开失败并重新停在停车位之后的状态。
		 * 
		 * 这6种状态码为互斥码，所以简单的整数序列即可。
		 * 4和5不一定出现。如果离开成功的话。
		 * 
		 * 调度器根据返回值确定汽车的状态。
		 */
		public static final int CAR_STATUS_WA	= 0;
		public static final int CAR_STATUS_SE	= 1;
		public static final int CAR_STATUS_PA	= 2;
		public static final int CAR_STATUS_LE	= 3;
		public static final int CAR_STATUS_RE	= 4;
		public static final int CAR_STATUS_DE	= 5;
		
		int ID;
		int status;
		int time_wait_sec;

		
		public class Param{
			int param;
			EventDriver ed;
		}
		
		public RCar(){this.status=CAR_STATUS_WA;}//生成的汽车初始为排队等待状态。
		
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
	 */
	
	private LinkedList<Car> se_cars;
	private LinkedList<Car> pa_cars;
	private LinkedList<Car> le_cars;
	private LinkedList<Car> re_cars;
	private LinkedList<Car> de_cars;
	
	private LinkedList<Car> cars;
	
	private int op_code;
	private Car op_car;
	
	/*
	 * 当前调度汽车的ID
	 */
	
	private int current_car_id;
	
	/*
	 *已经分配的汽车的ID，新的ID由当前ID加1得到。
	 *撤销汽车并不会导致该变量减小 
	 */
	
	private int allocated_car_id;
	
	public void init(){
		this.cars = new LinkedList<Car>();
		this.op_code=OPCODE_NULL;
		this.allocated_car_id=0;
	}
	
	private void tick(Car c){
		int return_status;
		Car.Param param=c.new Param();
		param.ed=this;
		
		c.status=STATUS_RUNNING;
		this.current_car_id=c.ID;
		
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
		
		while(!cars.isEmpty()){
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
	
	public int getCurrentCarID(){
		return this.current_car_id;
	}
}
