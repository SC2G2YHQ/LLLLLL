package ops;

import java.util.HashSet;
import java.util.LinkedHashMap;

import dat.Token;
import ops.dao.CardReader_Operations;
import ops.dao.Park_Operations;
import ops.dao.SensorProc;

public class Park extends EventDriver.Car implements Park_Operations,SensorProc{
	
	private String Name;
	private long id;
	
	private ops.dao.M_Operations m_op;
	
	private ops.dao.CardReader_Operations c_op_in;
	private ops.dao.CardReader_Operations c_op_out;
	
	private ops.dao.Rail_Operations r_op_in;
	private ops.dao.Rail_Operations r_op_out;
	
	private ops.dao.Sensor_Operations s_op_in;
	private ops.dao.Sensor_Operations s_op_out;
	
	private ops.EventDriver event_driver;//Park�Ĵ���Ҳ����Event Driver���ȵġ�����event_driverӦ������param������
	
	private int max_parts;//ÿ�г�λ�������ơ�


	/*
	 * token�����ڼ�Ȩ����������icard
	 */
	private LinkedHashMap<Integer,Token> tokens;
	
	/*
	 * ���ڳ���Ҳ�ǿ��ż���
	 */
	private HashSet<String> icards;
	
	/*
	 * ��λ�Ķ��壬����ͨ��
	 */
	private class solt{
		//ƴд���� Ӧ����slot
		boolean locked;	//
		int locked_by;	//
	}
	
	/*
	 * ͣ��λ���г���Ҳ���Ե������ࡣ
	 * Path��ͣ��λ����2����λ��ȡ�
	 * ʹͣ��λ��1��ʼ��0����Чͣ��λ
	 * ��������λ�����δ���
	 */
	private solt park[][];//ͣ��λ���壬��Ϊ���С�0��1����path���ߡ���δʵ�ֶ��е���
	private solt path[];//path�ĳ��ȶ��� := max_parts+2 max_parts+1��ͣ�����⴫������λ��

	private int total_empty_park;//ʣ���ͣ��λ
	
	/*
	 * ����ͣ������Ҫ�ṩ���¡��豸��������
	 * ͣ��������ID
	 * ����ڵ� ��������������������
	 * ������ϵͳM�Ľӿ�
	 * ������������
	 * ���λ���� �����У�
	 */
	public Park(String Name,
			int id,
			ops.dao.M_Operations m_op,
			CardReader_Operations c_op_in,
			CardReader_Operations c_op_out,
			ops.dao.Rail_Operations r_op_in,
			ops.dao.Rail_Operations r_op_out,
			ops.dao.Sensor_Operations s_op_in,
			ops.dao.Sensor_Operations s_op_out,
			int max_parts){
		//��������Ҫ���в�����Ч�Ե���֤
		this.Name = Name;
		this.id=id;
		this.m_op=m_op;
		this.c_op_in=c_op_in;
		this.c_op_out=c_op_out;
		this.r_op_in=r_op_in;
		this.r_op_out=r_op_out;
		this.s_op_in=s_op_in;
		this.s_op_out=s_op_out;
		this.max_parts=max_parts;
	}
	public void init(){
		//��ʼ��ͣ��λ�ʹ�����
		this.park=new solt[2][max_parts];
		this.path=new solt[max_parts+2];
		
		this.s_op_in.registerSensorProc(this, this, 0);
		this.s_op_in.registerSensorProc(this, this, max_parts+1);
		
		
	}

	/*
	 * (non-Javadoc)
	 * @see ops.dao.Park_Operations#lockPark(int, int, dat.Token)
	 * ����Ҫ��
	 * line=0|1 location < max_parts location!=0
	 */
	@Override
	public int lockPark(int line,int location, Token token) {
		// TODO Auto-generated method stub
		//�������ݺϷ���У��
		
		if(token==null)return -1;
		if(line<0||line>1)return -1;
		if(location>=max_parts)return -1;
		if(location<1)return -1;
		
		
		int car_id=EventDriver.getCurrentCarID();
		//��Ȩ����
		if(this.tokens.get(new Integer(car_id)).equals(token)){
			//��Ȩͨ��
			if(park[line][location].locked)return -1;//��λ�ѱ�����
			else {park[line][location].locked=true;park[line][location].locked_by=car_id;}
			return 0;
		}else
			return -1;
	}

	@Override
	public int releasePark(int line,int location, Token token) {
		// TODO Auto-generated method stub
		//�������ݺϷ���У��
		
		if(token==null)return -1;
		if(line<0||line>1)return -1;
		if(location>=max_parts)return -1;
		if(location<1)return -1;
		
		
		int car_id=EventDriver.getCurrentCarID();
		//��Ȩ����
		if(this.tokens.get(new Integer(car_id)).equals(token)){
			//��Ȩͨ��
			if(park[line][location].locked_by==car_id){
				park[line][location].locked=false;
				park[line][location].locked_by=0;
				return 0;
			}else
				return -1;
				
		}else
			return -1;
	}

	/*
	 * (non-Javadoc)
	 * @see ops.dao.Park_Operations#lockPath(int)
	 * location��Χ Ϊ��0~max_parts
	 * ������������Ȩ
	 */
	@Override
	public int lockPath(int location) {
		// TODO Auto-generated method stub
		if(location<0||location>max_parts)return -1;
		if(path[location].locked){return -1;}
		else {path[location].locked=true;return 0;}
	}

	@Override
	public int releasePath(int location) {
		// TODO Auto-generated method stub
		if(location<0||location>max_parts)return -1;
		if(path[location].locked){path[location].locked=false;return 0;}
		else return -1;
	}

	@Override
	public int queryEmptyParkCount() {
		// TODO Auto-generated method stub
		return this.total_empty_park;
	}

	/*
	 * (non-Javadoc)
	 * @see ops.dao.Park_Operations#requestToken()
	 * requestToken��leave��Ҫ��Щ������
	 * 
	 * 
	 */
	
	@Override
	public Token requestToken() {
		// TODO Auto-generated method stub
		
		int car_id=EventDriver.getCurrentCarID();
		if(this.tokens.get(new Integer(car_id))==null){
			//������Ϊ����ô��
			String E=this.c_op_in.getID();
			//��ѯ���ţ������Ự
			if(this.m_op.exist(E)){
				//�û�����Ϊ���û��ʹ��RSA��֤
				if(this.m_op.Verification(this.c_op_in.getName())){
					//��֤ͨ��
					if(!icards.contains(this.c_op_in.getName())){
						//handle��һ���������ȫ�Ը���
						icards.add(E);
						Token t=new Token(car_id,car_id);
						tokens.put(new Integer(car_id), t);
						//������������ƾ֤��ͻ����ͣ�������������뿪�Ĳ���
						total_empty_park--;
						/*
						 * ��������
						 */
						r_op_in.raise();
						return t;
					}
				}
			}
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see ops.dao.Park_Operations#leave()
	 * 0��ʾ��֤ͨ����Park�����������ˡ�
	 */
	@Override
	public int leave() {
		// TODO Auto-generated method stub
		int car_id=EventDriver.getCurrentCarID();
		if(this.tokens.get(new Integer(car_id))==null){
			String E=this.c_op_out.getID();
			if(icards.contains(E)){
				//��֤ͨ��
				r_op_out.raise();
				return 0;
			}
		}
		return -1;
	}

	
	
	
	
	@Override
	public CardReader_Operations getInCardReader() {
		// TODO Auto-generated method stub
		return this.c_op_in;
	}

	@Override
	public CardReader_Operations getOutCardReader() {
		// TODO Auto-generated method stub
		return this.c_op_out;
	}

	/*
	 * (non-Javadoc)
	 * @see ops.EventDriver.Car#run(ops.EventDriver.Car.Param)
	 * ���ڴ������ʱ������requestToken�ȡ�
	 */
	@Override
	public int run(Param param) {
		// TODO Auto-generated method stub
		
		//�ȴ���һ������
		return EventDriver.STATUS_WAIT_SIG;
	}
	
	public boolean get(int location){
		//ע���±�Խ��
		return this.path[location].locked;
	}

	@Override
	public boolean getParkStatus(int line, int location) {
		// TODO Auto-generated method stub
		return this.park[line][location].locked;
	}

	@Override
	public int getMaxPartCount() {
		// TODO Auto-generated method stub
		return this.max_parts;
	}

	@Override
	public void lpfnSensorProc(int location, boolean status) {
		// TODO Auto-generated method stub
		//�������ص�����
		//�ж���Ϣ������λ�á�
		//statusΪtrue��ʾ�Ϸ��г���false��ʾ�޳�
		//���ڴ�������Э�鶨�壬�˴������ж�״̬�ı仯
		if(location==0){
			//�볡������
			//�������ˡ�
			//������δ��ȫ���µ�������������Ϳɽ���
			if(status==false){
				this.r_op_in.down();
				if(this.total_empty_park>0){
					//�������п�λ��
					//֪ͨ������ �볡��һ������
					this.event_driver.sigToWaCar();
				}
			}
		}
		
		if(location==this.max_parts+1){
			//����������
			if(status==false){
				this.r_op_out.down();
				this.total_empty_park++;
			}
		}
	}
	
	
}
