package ops;

import java.util.Iterator;
import java.util.LinkedList;

/*
 * Ҫ��ʵ�����õ��� ���������ȼ�
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
	 * ����������������ʵ����ͨ���߳̽ӿ�
	 * ��������ֶ�Ϊ��Car
	 */
	
	public static abstract class Car{
		int ID;
		int status;
		int time_wait_sec;

		
		public class Param{
			int param;
			EventDriver ed;
		}
		
		public Car(){this.status=STATUS_READY;}//�����߳���Ҫ�г�ʼ״̬����ʼΪ ����
		
		public abstract int run(Param param);
	}
	
	
	/*
	 * ����������ͨ���̣߳����Լ��ض��Ļص���������
	 */
	public static abstract class RCar{
		/*
		 * 6��״̬��ֱ��ӦΪ
		 * 0.�볡�Ŷ�
		 * 1.���г�����Ѱ�ҳ�λ
		 * 2.ͣ����
		 * 3.���г����������뿪ͣ����
		 * 4.�뿪ʧ�ܣ����ڷ���ԭ��λ������ĳ�λ
		 * 5.�������뿪ʧ�ܲ�����ͣ��ͣ��λ֮���״̬��
		 * 
		 * ��6��״̬��Ϊ�����룬���Լ򵥵��������м��ɡ�
		 * 4��5��һ�����֡�����뿪�ɹ��Ļ���
		 * 
		 * ���������ݷ���ֵȷ��������״̬��
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
		
		public RCar(){this.status=CAR_STATUS_WA;}//���ɵ�������ʼΪ�Ŷӵȴ�״̬��
		
		public abstract int wa(Param param);
		public abstract int se(Param param);
		public abstract int pa(Param param);
		public abstract int le(Param param);
		public abstract int re(Param param);
		public abstract int de(Param param);
		
	}


	/*
	 * ��ͬ�Ķ��С� ������������˯�ߡ�ʱ��ȴ���
	 * ͬһ������ҲҪ��˳��
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
	 * ��ǰ����������ID
	 */
	
	private int current_car_id;
	
	/*
	 *�Ѿ������������ID���µ�ID�ɵ�ǰID��1�õ���
	 *�������������ᵼ�¸ñ�����С 
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
			 * ÿ�ε���֮ǰ�Ƚ��г���������
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
				//��Ч������
				break;
			}
			
			//����������Ϊ�ղ�������Ȼ��һ�ֻ��ظ�����
			this.op_code=OPCODE_NULL;
			
			/*
			 * ��ʼһ�ֵ���
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
	 * ����Ϊԭ�Ӳ�����������Ҫ���⴦��
	 */
	
	public void sendSIG(int CarID){
		Car car=getCarByID(CarID);
		this.op_car=car;
		this.op_code=OPCODE_SENDSIG;
	}
	
	/*
	 * �������������������Ч����Ҫ�ȵ�ǰ����������ɡ�
	 */
	public void suspend(int CarID){
		Car car=getCarByID(CarID);
		this.op_car=car;
		this.op_code=OPCODE_SUSPEND;
	}
	
	/*
	 *��Ӳ��������첽
	 *���˴����첽�����ܻ����©�� 
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
