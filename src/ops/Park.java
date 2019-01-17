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
	
	private ops.EventDriver event_driver;//Park的代码也是由Event Driver调度的。所以event_driver应该是由param传进。
	
	private int max_parts;//每行车位数量限制。


	/*
	 * token仅用于鉴权，并不区分icard
	 */
	private LinkedHashMap<Integer,Token> tokens;
	
	/*
	 * 场内车，也是卡号集合
	 */
	private HashSet<String> icards;
	
	/*
	 * 车位的定义，包括通道
	 */
	private class solt{
		//拼写错误 应该是slot
		boolean locked;	//
		int locked_by;	//
	}
	
	/*
	 * 停车位和行车道也可以单独成类。
	 * Path比停车位多了2个车位宽度。
	 * 使停车位从1开始。0是无效停车位
	 * 这两个车位宽度如何处理。
	 */
	private solt park[][];//停车位定义，分为两列。0和1，在path两边。并未实现多列调度
	private solt path[];//path的长度定义 := max_parts+2 max_parts+1是停车场外传感器的位置

	private int total_empty_park;//剩余空停车位
	
	/*
	 * 构造停车场需要提供以下“设备”及参数
	 * 停车场名、ID
	 * 出入口的 传感器、读卡器及栏杆
	 * 卡管理系统M的接口
	 * 调度器的引用
	 * 最大车位限制 （单行）
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
		//理论上需要进行参数有效性的验证
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
		//初始化停车位和传感器
		this.park=new solt[2][max_parts];
		this.path=new solt[max_parts+2];
		
		this.s_op_in.registerSensorProc(this, this, 0);
		this.s_op_in.registerSensorProc(this, this, max_parts+1);
		
		
	}

	/*
	 * (non-Javadoc)
	 * @see ops.dao.Park_Operations#lockPark(int, int, dat.Token)
	 * 条件要求
	 * line=0|1 location < max_parts location!=0
	 */
	@Override
	public int lockPark(int line,int location, Token token) {
		// TODO Auto-generated method stub
		//输入数据合法性校验
		
		if(token==null)return -1;
		if(line<0||line>1)return -1;
		if(location>=max_parts)return -1;
		if(location<1)return -1;
		
		
		int car_id=EventDriver.getCurrentCarID();
		//鉴权操作
		if(this.tokens.get(new Integer(car_id)).equals(token)){
			//鉴权通过
			if(park[line][location].locked)return -1;//车位已被锁定
			else {park[line][location].locked=true;park[line][location].locked_by=car_id;}
			return 0;
		}else
			return -1;
	}

	@Override
	public int releasePark(int line,int location, Token token) {
		// TODO Auto-generated method stub
		//输入数据合法性校验
		
		if(token==null)return -1;
		if(line<0||line>1)return -1;
		if(location>=max_parts)return -1;
		if(location<1)return -1;
		
		
		int car_id=EventDriver.getCurrentCarID();
		//鉴权操作
		if(this.tokens.get(new Integer(car_id)).equals(token)){
			//鉴权通过
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
	 * location范围 为：0~max_parts
	 * 锁定车道不鉴权
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
	 * requestToken和leave需要哪些鉴定？
	 * 
	 * 
	 */
	
	@Override
	public Token requestToken() {
		// TODO Auto-generated method stub
		
		int car_id=EventDriver.getCurrentCarID();
		if(this.tokens.get(new Integer(car_id))==null){
			//读卡器为空怎么办
			String E=this.c_op_in.getID();
			//查询工号，建立会话
			if(this.m_op.exist(E)){
				//用户名作为口令，没有使用RSA验证
				if(this.m_op.Verification(this.c_op_in.getName())){
					//验证通过
					if(!icards.contains(this.c_op_in.getName())){
						//handle是一个随机数安全性更高
						icards.add(E);
						Token t=new Token(car_id,car_id);
						tokens.put(new Integer(car_id), t);
						//假设汽车请求凭证后就会进入停车场，不考虑离开的操作
						total_empty_park--;
						/*
						 * 升起栏杆
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
	 * 0表示验证通过，Park将会升起栏杆。
	 */
	@Override
	public int leave() {
		// TODO Auto-generated method stub
		int car_id=EventDriver.getCurrentCarID();
		if(this.tokens.get(new Integer(car_id))==null){
			String E=this.c_op_out.getID();
			if(icards.contains(E)){
				//验证通过
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
	 * 用于处理带延时的请求。requestToken等。
	 */
	@Override
	public int run(Param param) {
		// TODO Auto-generated method stub
		
		//等待下一个请求
		return EventDriver.STATUS_WAIT_SIG;
	}
	
	public boolean get(int location){
		//注意下标越界
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
		//传感器回调函数
		//判断消息发生的位置。
		//status为true表示上方有车，false表示无车
		//由于传感器的协议定义，此处不用判断状态的变化
		if(location==0){
			//入场传感器
			//放下栏杆。
			//栏杆再未完全放下的情况下辆汽车就可进入
			if(status==false){
				this.r_op_in.down();
				if(this.total_empty_park>0){
					//表明还有空位。
					//通知调度器 入场下一个汽车
					this.event_driver.sigToWaCar();
				}
			}
		}
		
		if(location==this.max_parts+1){
			//出场传感器
			if(status==false){
				this.r_op_out.down();
				this.total_empty_park++;
			}
		}
	}
	
	
}
