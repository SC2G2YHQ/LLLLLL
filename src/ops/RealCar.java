package ops;

import dat.ICard;
import dat.Token;
import ops.EventDriver.RCar;
import ops.dao.CardReader_Operations;
import ops.dao.Park_Operations;

public class RealCar extends EventDriver.RCar{
	
	public static final int CURRENT_STATUS_WAITING			= 0;
	public static final int CURRENT_STATUS_GOING_TO_PARK	= 1;
	public static final int CURRENT_STATUS_PARKING			= 2;
	public static final int CURRENT_STATUS_LEAVING			= 3;
	
	/*
	 * ����Ӧ�������Լ�¼�Լ���λ�á��ڴ�Ϊ�˷���û��
	 */
	private ICard icard;
	private int wait_time;
	private int current_status=CURRENT_STATUS_WAITING;
	private Token token;
	
	/*
	 * �����б�������л���������Park_Operations��CardReader_Operations
	 */
	private Park_Operations p_op;
	private CardReader_Operations c_op;
	
	private int wa_opcode=0;//wa�Ĳ�����Ҫ�ֳɶ���׶�
	private boolean se_opcode=false;//se�����ֳ������׶�
	private int le_opcode=0;//le�����ֳɶ���׶�
	private int re_opcode=0;
	
	private int park_line;
	private int park_location;
	
	//�������ؽ׶�Ҫ֪���Լ��ķ���
	private boolean direction_back=false;
	
	/*
	 * ��ǰ��ͷ����λ��
	 */
	private int se_locked_location=-1;
	
	/*
	 * ͣ��λ�ã���Ҫ��������
	 */
	private int wa_park_line=-1;
	private int wa_park_location=-1;
	
	public RealCar(int sleeptime,ICard icard){
		this.wait_time=sleeptime;
		this.icard=icard;
	}
	@Override
	public int wa(Param param) {
		// TODO Auto-generated method stub
		switch(wa_opcode){
		case 0:
		c_op=p_op.getInCardReader();
		c_op.putICard(icard);
		
		 // requestToken������Ҫ�ȴ�1s���ܷ���
		 //ʹ����ͨ�ȴ����˴��ȴ�����Ϊ ��֤���ŵ�λ
		wa_opcode++;
		param.param=1;
		return EventDriver.STATUS_WAIT_TIM;
		
		
		
		case 1:
		this.token=p_op.requestToken();
		c_op.removeICard();
		wa_opcode++;
		
		/*
		 * �ȴ�����̧��
		 */
		param.param=2;
		return EventDriver.STATUS_WAIT_TIM;
		
		
		
		case 2:
		if(token==null)
			
			// * ��֤ʧ�ܣ������뿪���߳��˳�
			 
			return EventDriver.STATUS_EXIT;
		else
			
			// * ����ͣ������
			//������ִ��se����
			param.car_status=CAR_STATUS_SE;
			return EventDriver.STATUS_EXIT;
		default:
			//��������߳��˳�
			
			return EventDriver.STATUS_EXIT;
		}
		
	}
	
	/*
	 * (non-Javadoc)
	 * @see ops.EventDriver.RCar#se(ops.EventDriver.RCar.Param)
	 *  Ѱ��ͣ��λ������
	 *  �����Ҳ���������أ�
	 */
	@Override
	public int se(Param param) {
		// TODO Auto-generated method stub
		//��Ҫ�ӽ��뿪ʼ�ж�
		
		//�����ֵΪtrue��ʾ�Ѿ������ͣ��λռ�졣�ͷ�path������ͣ��״̬
		if(this.se_opcode){
			this.p_op.releasePath(se_locked_location);
			this.p_op.releasePath(se_locked_location-1);
			param.param=this.time_wait_sec;
			return EventDriver.STATUS_EXIT;
		}
		/*
		 * λ�ô�1��ʼ �����г�λ
		 * �ҵ��ճ�λ�������ó�λ��˯��2��
		 */
		if(se_locked_location>0){
			//�鿴һ���Ƿ��п�λ
			if(!this.p_op.getParkStatus(0,se_locked_location)){
				this.p_op.lockPark(0, se_locked_location, this.token);
				//�������Ǽ���ͣ��λ���������ʧ����Ҫ���ش�λ��
				
				this.park_line=0;
				this.park_location=se_locked_location;
						
				param.param=2;
				this.se_opcode=true;
				return EventDriver.STATUS_WAIT_TIM;
			}
			//�鿴��һ���Ƿ��п�λ
			if(!this.p_op.getParkStatus(1,se_locked_location)){
				this.p_op.lockPark(1, se_locked_location, this.token);
				
				//����ͣ��λ��
				
				this.park_line=1;
				this.park_location=se_locked_location;
				
				param.param=2;
				this.se_opcode=true;
				return EventDriver.STATUS_WAIT_TIM;
			}
		}
		
		//path[0]�Ǵ�������λ�ã�����û��ͣ��λ
		/*
		 * se_locked_location��-1��ʼ����Ҫ���Ǻܶࡣ
		 */
		//��Ҫ���ǵ�һ��������ߵ�ͷҲû���ҵ�ͣ��λ���˴�δ���ǡ�
		/*
		 * �ж���һ��λ����û�б�ռ
		 */
		if(!this.p_op.get(se_locked_location+1)){
			//û�б�ռ��������λ�ã����ͷ���һ��λ�á�
			this.p_op.lockPath(se_locked_location+1);
			//��Ҫ�ж�β��λ���Ƿ���path�ڡ���������򲻽�����������
			if(this.se_locked_location-1>-1){
				this.p_op.releasePath(se_locked_location-1);
			}
			//������ʾ��ʻ����һ��λ��
			se_locked_location++;
		}
		
		
		return EventDriver.STATUS_READY;
	}
	
	
	
	@Override
	public int pa(Param param) {
		// TODO Auto-generated method stub
		//pa�����Ƕ���Ĳ�δ��ʹ��
		return 0;
	}
	
	
	@Override
	public int le(Param param) {
		// TODO Auto-generated method stub
		//�뿪��Ϊ����׶Σ��ϳ����׶Ρ�ʻ����ڽ׶Ρ��뿪�ȴ��׶Ρ��ϳ����̶�ʱ��2s
		
		//true��ʱ���ʾ�Ѿ��ڳ����ϡ�
		if(this.le_opcode==1){
			//������Ҫ�ж��Ƿ��ߵ��˳���
			if(se_locked_location>=this.p_op.getMaxPartCount())
			{//��ʾ�Ѿ��ߵ��˳���
				this.c_op=this.p_op.getOutCardReader();
				//�����ŵ���������
				c_op.putICard(icard);
				//�ȴ�1s
				param.param=1;
				//�޸Ĳ����룬������һ�׶�
				this.le_opcode=2;
				return EventDriver.STATUS_WAIT_TIM;
			}
			//û���ߵ����������ǰ��
			if(!this.p_op.get(se_locked_location+1)){
				//û�б�ռ��������λ�ã����ͷ���һ��λ�á�
				//���볡ռλ������Щ��ͬ������Ҫ�ж�β���Ƿ���path��
				this.p_op.lockPath(se_locked_location+1);
				this.p_op.releasePath(se_locked_location-1);
				return EventDriver.STATUS_READY;
			}else{
				//���������ռ����ȴ���һ������
				return EventDriver.STATUS_READY;
			}
		}else if(this.le_opcode==0){
			//�жϳ�����û�б�ռ
			if((!this.p_op.get(se_locked_location))&&(!this.p_op.get(se_locked_location+1))){
				//������ǰ����һ��λ��
				this.p_op.lockPath(se_locked_location);
				this.p_op.lockPath(se_locked_location+1);
				//˯��2s��ģ�⵽������Ҫ��2s
				param.param=2;
				//����ͷ�õ���һλ�á�
				se_locked_location++;
				//�ϳ����׶����
				le_opcode=1;
				return EventDriver.STATUS_WAIT_TIM;
				}else{
					//���������ռ����ȴ���һ������
					return EventDriver.STATUS_READY;
				}
			}else if(this.le_opcode==2){
				//�Ѿ��ȴ���1s �������Ѿ��������������뿪�׶�
				int res=this.p_op.leave();
				if(res==0){
					//�����뿪�ɹ�
					//�ȴ�2s ����̧��
					param.param=2;
					this.le_opcode=3;
					return EventDriver.STATUS_WAIT_TIM;
				}else{
					//�����뿪ʧ��
					//���뷵�ض���
					//�˳���ǰ����
					this.se_locked_location--;//�˲�����������ģ���ͷ��
					this.direction_back=true;//�������ķ�����Ϊ������
					param.car_status=RCar.CAR_STATUS_RE;
					return EventDriver.STATUS_EXIT;
				}
			}else if(this.le_opcode==3){
				//�����뿪�׶�
				//��Ҫ�������뿪������
				//״̬���٣�������if�顣��� ����
				//����׶�û�п���λ���Ѿ���ռ�õ�״̬
				if(se_locked_location==this.p_op.getMaxPartCount()+2)
				{
					//��β������ʻ������������
					//��ζ��������ȫ�뿪
					//�ͷ����һ��λ��
					this.p_op.releasePath(se_locked_location-2);
					return EventDriver.STATUS_EXIT;
				}
				
				if(se_locked_location==this.p_op.getMaxPartCount()+1)
				{
					//��ͷ������ʻ������������
					//����������һ������
					this.p_op.releasePath(se_locked_location-2);
					se_locked_location++;
					return EventDriver.STATUS_READY;
					
				}
				if(se_locked_location==this.p_op.getMaxPartCount()){
					
					//��һ�׶�
					//��ʱ�� se_locked_location ����  this.p_op.getMaxPartCount() ��
					
					this.p_op.lockPath(se_locked_location+1);
					this.p_op.releasePath(se_locked_location-1);
					se_locked_location++;
					return EventDriver.STATUS_READY;
				}
			}
			return EventDriver.STATUS_READY; 
	}
	
	
	@Override
	public int re(Param param) {
		// TODO Auto-generated method stub
		//���ص�ʱ��Ҫһֱɨ��ԭ��λ��û�б�ռ��һ����ռ�����Ҿͽ���λ
		//�볡���������������ͽ��ҳ�λ���ܻ���ֵ�ͷ�������
		//������볡�����ֳ�ͻ��ô�죿
		//�׶η�Ϊ��0.����ԭ��λ��	1.ռ��ԭ��λ     2.ռ��ԭ��λʧ�ܺ��Ҿͽ���λ
		//3.ռ��ͽ���λ��
		//û�п��ǵ�ͷ��Ҫ��ʱ��
		switch(this.le_opcode){
		case 0:
			//����ԭ��λ�Ľ׶�
			//�ȿ�ԭ��λ��û�б�ռ
			if(this.p_op.getParkStatus(park_line, park_location)){
				//ԭ��λ�Ѿ���ռ
				//����Ѱ�Ҿͽ���λ״̬
				le_opcode=1;
				return EventDriver.STATUS_READY;
			}else{
				//�ж��Ƿ���ԭ��λ
				if(this.se_locked_location!=this.park_location){
					//û�е�ԭ��λ���н�һ��
					//�ж������ķ���
					if(this.direction_back){
						//������
						if(!this.p_op.get(se_locked_location-1)){
							this.p_op.lockPath(se_locked_location-1);
							this.p_op.releasePath(se_locked_location+1);
							se_locked_location--;
							return EventDriver.STATUS_READY;
						}else{
							//���������ռ����ȴ���һ������
							return EventDriver.STATUS_READY;
						}
					}else{
						//������
						if(!this.p_op.get(se_locked_location+1)){
							this.p_op.lockPath(se_locked_location+1);
							this.p_op.releasePath(se_locked_location-1);
							se_locked_location++;
							return EventDriver.STATUS_READY;
						}else{
							//���������ռ����ȴ���һ������
							return EventDriver.STATUS_READY;
						}
					}
				}else{
					//����ԭ��λ��������λ ˯��2s
					this.p_op.lockPark(park_line, park_location, token);
					le_opcode=2;
					param.param=2;
					return EventDriver.STATUS_WAIT_TIM;
				}
			}
		case 1:
			//Ѱ�Ҿͽ��ĳ�λ
			//��Ҫ�Ĳ����������ҵ�����ĳ�λ��������ĳ�λ��Ϊԭ��λ����ִ�з���ԭ��λ�Ĳ�������
			//��Ҫע�����Ҫ�������ó��ķ���
			//��se_locked_location��ʼ�����߱���
			int i=1;
			//����Ĳ���˳��Ϊ������ĵ�i�������ߵ�λ��
			//ǰ���i�������ߵ�λ��
			while(true){
				//ע��Խ������
				//��Ч��λ�ķ�Χ�� 1~max_parts-1
				if(se_locked_location-i>=1){
					if(!this.p_op.getParkStatus(0, se_locked_location-i)){
						//��ԭ��λ��Ϊ�ҵ��������λ��
						this.park_line=0;
						this.park_location=se_locked_location-i;
						//���ó�����
						this.direction_back=false;
						//���ò����룬��һ��ִ�� ����ԭλ�ò���
						this.re_opcode=0;
						return EventDriver.STATUS_READY;
					}
					if(!this.p_op.getParkStatus(1, se_locked_location-i)){
						this.park_line=1;
						this.park_location=se_locked_location-i;
						this.direction_back=false;
						this.re_opcode=0;
						return EventDriver.STATUS_READY;
					}
				}
				
				if(se_locked_location+i<this.p_op.getMaxPartCount()){
					if(!this.p_op.getParkStatus(0, se_locked_location+i)){
						this.park_line=0;
						this.park_location=se_locked_location+i;
						this.direction_back=true;
						this.re_opcode=0;
						return EventDriver.STATUS_READY;
					}
					if(!this.p_op.getParkStatus(1, se_locked_location+i)){
						this.park_line=1;
						this.park_location=se_locked_location+i;
						this.direction_back=true;
						this.re_opcode=0;
						return EventDriver.STATUS_READY;
					}
				}
				i++;
				if(i>this.p_op.getMaxPartCount()){
					//����ȫ��Խ�磬˵���Ѿ�û�п�λ��
					//������������ϻ���֡�����ʱ������֡�
					//һ���������������ѭ��
					//throw new Throwable();
				}
				
			}
		case 3:
			//�ɹ�����ԭ��λ�������벻�����������״̬
			param.car_status=CAR_STATUS_DE;
			return EventDriver.STATUS_EXIT;
		}
		return 0;
	}
	
	
	@Override
	public int de(Param param) {
		// TODO Auto-generated method stub
		//de�׶ε�������Զ���ᱻ���ȣ�����ռ��ͣ��λ
		return 0;
	}
	
/*	@Override
	public int run(Param param) {
		// TODO Auto-generated method stub
		
		switch(current_status){
		case CURRENT_STATUS_WAITING:
			//ִ�н���ͣ����������
			current_status=CURRENT_STATUS_GOING_TO_PARK;
			return EventDriver.STATUS_READY;
		case CURRENT_STATUS_GOING_TO_PARK:
			//ִ�в��ҳ�λ������
			
			 * 
			 
			current_status=CURRENT_STATUS_PARKING;
			return EventDriver.STATUS_READY;
		case CURRENT_STATUS_PARKING:
			
			 * �ߵ����ڡ���Ҫ���á�
			 
		case CURRENT_STATUS_LEAVING:
			
		}
		return 0;
	}*/
	
	
}
