package ops;

import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;

/*
 * Ҫ��ʵ�����õ��� ���������ȼ�
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
	 * ����������������ʵ����ͨ���߳̽ӿ�
	 * ��������ֶ�Ϊ��Car
	 * ������Card Reader��Rail ��Sensor����
	 * �����ź���Ҫ�ܴ���
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
		 * 7��״̬��ֱ��ӦΪ
		 * 1.��;����ʧ���볡��Ŀǰֻ��icard��Ч������ͣ����ʧ�ܵ����
		 * 2.�볡�Ŷ�
		 * 3.���г�����Ѱ�ҳ�λ
		 * 4.ͣ����
		 * 5.���г����������뿪ͣ����
		 * 6.�뿪ʧ�ܣ����ڷ���ԭ��λ������ĳ�λ
		 * 7.�������뿪ʧ�ܲ�����ͣ��ͣ��λ֮���״̬��
		 * 
		 * ��6��״̬��Ϊ�����룬���Լ򵥵��������м��ɡ�
		 * 4��5��һ�����֡�����뿪�ɹ��Ļ���
		 * 
		 * ���������ݷ���ֵȷ��������״̬��
		 * 
		 * �������ͣ�����������
		 * ״̬�벻ͨ������ֵ������ͨ��param��������������ֵ��Ȼ�����ǵ�����Ϣ��
		 * 
		 * 0��ʾά�ֵ�ǰ״̬��
		 * 
		 * ���ֶ����в�����֮��
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
		 * ���νṹ����ͬ�ṹ֮��ʹ�ô˽ṹ���д��Ρ�
		 * ÿһ��RCarʵ��Ӧ�ö���һ����Ӧ��param�ṹ��
		 * ���ҸýṹӦ���Ǿ�̬�������������ں����С�
		 * ������һ��param�飬������Ϣ����
		 */
		public class Param{
			int param;
			int car_status;
			EventDriver ed;
			Object obj;
		}
		int locked_location;
		Param p_sig;
		
		public RCar(){this.status=CAR_STATUS_WA;}//���ɵ�������ʼΪ�Ŷӵȴ�״̬��
		
		/*
		 * wa����ִ���������ͣ�����Ĳ���
		 */
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
	 * ʹ�ò�ͬ�Ķ�����Ϊ��ʵ�����ȼ���
	 * ���ȼ�˳��Ϊ��ͣ��>�볡��>�볡��>�Ŷӳ�>��Χ���� ���������������˵�
	 * ����ͣ�ĳ���������κβ�������Ӧ����Ϊ��Ϣ
	 */
	
	private LinkedList<RCar> wa_cars;
	
	private LinkedList<RCar> se_cars;
	private LinkedList<RCar> pa_cars;
	private LinkedList<RCar> le_cars;
	private LinkedList<RCar> re_cars;
	private LinkedList<RCar> de_cars;
	
	/*
	 * ��������ĵ��ȶ��С��ö������ȼ���͡�
	 */
	private LinkedList<Car> cars;
	
	private int op_code;
	private Car op_car;
	
	/*
	 * ��ǰ����������ID
	 */
	
	public static int current_car_id;
	
	/*
	 *�Ѿ������������ID���µ�ID�ɵ�ǰID��1�õ���
	 *�������������ᵼ�¸ñ�����С 
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
			
			/*
			 * һ��5���������ȶ��С�Ϊ�˷��㣬ֻ����wa��le���е�ʱ��ȴ�����
			 * �������е��κ����󶼲���ᡣ
			 * ͨ�ö�����Ҫ������������
			 */
			
			/*
			 * Ϊͣ����������ʱ
			 */
			for(RCar rc:pa_cars)
			{
				rc.time_wait_sec--;
				if(rc.time_wait_sec<0){//���������뿪����
					le_cars.add(rc);
					le_cars.sort(new Comparator<RCar>() {

						@Override
						public int compare(RCar o1, RCar o2) {
							// TODO Auto-generated method stub
							return o1.locked_location-o2.locked_location;
						}
					});//�������Ҫ,��������ͣ��λ�á�������ڵ��ȱ�����
					//��ͣ������ɾȥ
					pa_cars.remove(rc);
				}
			}
			
			/*
			 * �볡��������Ҫ�����ȴ�����iCard�������� �Լ� �ȴ�����
			 */
			
			for(RCar rc:le_cars)
			{
				//le���е��������ȴ�����
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
				//�ж������Ƿ��볡�ɹ�
				//exit��ʾ�Ѿ��˳���ǰ����
				//�ٸ���param�е�car_status�ֶ��ж�
				if(rc.status==STATUS_EXIT){
					if(p.car_status==RCar.CAR_STATUS_RE){
						//�뿪ʧ��,���������뷵�ض���
						this.re_cars.add(rc);
						rc.status=STATUS_READY;
						this.le_cars.remove(rc);

					}else{
						//�뿪�ɹ�,���������뿪�������Ƴ�
						this.le_cars.remove(rc);
					}
				}
				
			}
			
			/*
			 * �볡ʧ�ܵ�������
			 */
			for(RCar rc:re_cars)
			{
				RCar.Param p=rc.new Param();
				p.ed=this;
				if(rc.status==STATUS_WAIT_TIM){
					rc.time_wait_sec--;
					if(rc.time_wait_sec>=0)continue;//�ȴ�����û����� �������ֵ���
				}
				
				
				current_car_id=rc.ID;
				
				rc.status=STATUS_RUNNING;
				rc.status=rc.le(p);
				
				if(rc.status==STATUS_EXIT){
					//����Ӧ�κ��������Բ��ö�param�����ж�
					//�˳��Ž���������������
					this.de_cars.add(rc);
					this.re_cars.remove(rc);
				}else if(rc.status==STATUS_WAIT_TIM){
					rc.time_wait_sec=p.param;
				}
				
				
			}
			
			/*
			 * �볡������
			 */
			for(RCar rc:se_cars)
			{
				RCar.Param p=rc.new Param();
				p.ed=this;
				/*
				 * ռ��λ��Ҫ�����ȴ�����
				 */
				if(rc.status==STATUS_WAIT_TIM){
					rc.time_wait_sec--;
					if(rc.time_wait_sec>=0)continue;//�ȴ�����û����� �������ֵ���
				}
				current_car_id=rc.ID;
				
				rc.status=STATUS_RUNNING;
				rc.status=rc.le(p);
				
				if(rc.status==STATUS_EXIT){
					//�˳�����ζ�Ž���ͣ������
					//ͣ���೤��param����
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
				 *  de ���в����ȣ���������
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
	
	/*
	 * ����ģ��ϵͳ��ֻ������һ��EventDriver
	 * ���Ի�ȡ��ǰCar��ID���þ�̬����
	 * ��ͬ��Thread.currentThread()
	 */
	public static int getCurrentCarID(){
		return current_car_id;
	}
	
	/*
	 * ������������������
	 * ֻ���ڵ�����ֹͣ��ʱ�����Ч
	 * ֻ����ӵ��ŶӶ�����
	 */
	public int addRCar(RCar rc){
		//�ظ����������ô�죿
		//͵����ֻ���ŶӶ����е����������ظ���֤��
		if(this.wa_cars.contains(rc))
			return -1;
		else{
			rc.ID=++allocated_car_id;
			//��Ҫ���뿪ͷ
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
	 * ֹͣ�����������������������񣬰��������߳�
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
		//�������ظ���ʼ
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
	 * ����ȴ������е���������������볡����
	 * �ȴ�����ֻ��Ϊ��ģ�⡣
	 * �������ͣ�������Ŷӣ���û�б�Ҫ��ͣ������
	 */
	public void sigToWaCar(){
		if(!this.wa_cars.isEmpty()){
			RCar c=this.wa_cars.removeFirst();
			c.status=STATUS_READY;
			this.se_cars.add(c);
		}
	}
}
