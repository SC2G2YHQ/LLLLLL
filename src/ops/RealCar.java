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
	 * 汽车应该有属性记录自己的位置。在此为了方便没有
	 */
	private ICard icard;
	private int wait_time;
	private int current_status=CURRENT_STATUS_WAITING;
	private Token token;
	
	/*
	 * 汽车有必须的运行环境，包括Park_Operations和CardReader_Operations
	 */
	private Park_Operations p_op;
	private CardReader_Operations c_op;
	
	private int wa_opcode=0;//wa的操作需要分成多个阶段
	private boolean se_opcode=false;//se操作分成两个阶段
	private int le_opcode=0;//le操作分成多个阶段
	private int re_opcode=0;
	
	private int park_line;
	private int park_location;
	
	//汽车返回阶段要知道自己的方向
	private boolean direction_back=false;
	
	/*
	 * 当前车头锁定位置
	 */
	private int se_locked_location=-1;
	
	/*
	 * 停车位置，需要两个属性
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
		
		 // requestToken至少需要等待1s才能返回
		 //使用普通等待。此处等待可视为 保证卡放到位
		wa_opcode++;
		param.param=1;
		return EventDriver.STATUS_WAIT_TIM;
		
		
		
		case 1:
		this.token=p_op.requestToken();
		c_op.removeICard();
		wa_opcode++;
		
		/*
		 * 等待栏杆抬起
		 */
		param.param=2;
		return EventDriver.STATUS_WAIT_TIM;
		
		
		
		case 2:
		if(token==null)
			
			// * 验证失败，汽车离开，线程退出
			 
			return EventDriver.STATUS_EXIT;
		else
			
			// * 进入停车场。
			//接下来执行se方法
			param.car_status=CAR_STATUS_SE;
			return EventDriver.STATUS_EXIT;
		default:
			//其他情况线程退出
			
			return EventDriver.STATUS_EXIT;
		}
		
	}
	
	/*
	 * (non-Javadoc)
	 * @see ops.EventDriver.RCar#se(ops.EventDriver.RCar.Param)
	 *  寻找停车位操作。
	 *  出现找不到的情况呢？
	 */
	@Override
	public int se(Param param) {
		// TODO Auto-generated method stub
		//需要从进入开始判断
		
		//如果该值为true表示已经完成了停车位占领。释放path并进入停车状态
		if(this.se_opcode){
			this.p_op.releasePath(se_locked_location);
			this.p_op.releasePath(se_locked_location-1);
			param.param=this.time_wait_sec;
			return EventDriver.STATUS_EXIT;
		}
		/*
		 * 位置从1开始 两边有车位
		 * 找到空车位后锁定该车位，睡眠2秒
		 */
		if(se_locked_location>0){
			//查看一侧是否有空位
			if(!this.p_op.getParkStatus(0,se_locked_location)){
				this.p_op.lockPark(0, se_locked_location, this.token);
				//此两句是记下停车位。如果出场失败则要返回此位置
				
				this.park_line=0;
				this.park_location=se_locked_location;
						
				param.param=2;
				this.se_opcode=true;
				return EventDriver.STATUS_WAIT_TIM;
			}
			//查看另一侧是否有空位
			if(!this.p_op.getParkStatus(1,se_locked_location)){
				this.p_op.lockPark(1, se_locked_location, this.token);
				
				//记下停车位。
				
				this.park_line=1;
				this.park_location=se_locked_location;
				
				param.param=2;
				this.se_opcode=true;
				return EventDriver.STATUS_WAIT_TIM;
			}
		}
		
		//path[0]是传感器的位置，两边没有停车位
		/*
		 * se_locked_location从-1开始。需要考虑很多。
		 */
		//需要考虑到一个情况，走到头也没有找到停车位。此处未考虑。
		/*
		 * 判断下一个位置有没有被占
		 */
		if(!this.p_op.get(se_locked_location+1)){
			//没有被占则锁定该位置，并释放上一个位置。
			this.p_op.lockPath(se_locked_location+1);
			//需要判断尾部位置是否在path内。如果不在则不进行锁定操作
			if(this.se_locked_location-1>-1){
				this.p_op.releasePath(se_locked_location-1);
			}
			//自增表示行驶到下一个位置
			se_locked_location++;
		}
		
		
		return EventDriver.STATUS_READY;
	}
	
	
	
	@Override
	public int pa(Param param) {
		// TODO Auto-generated method stub
		//pa方法是多余的并未被使用
		return 0;
	}
	
	
	@Override
	public int le(Param param) {
		// TODO Auto-generated method stub
		//离开分为多个阶段：上车道阶段、驶向出口阶段、离开等待阶段。上车道固定时间2s
		
		//true的时候表示已经在车道上。
		if(this.le_opcode==1){
			//首先需要判断是否走到了出口
			if(se_locked_location>=this.p_op.getMaxPartCount())
			{//表示已经走到了出口
				this.c_op=this.p_op.getOutCardReader();
				//将卡放到读卡器上
				c_op.putICard(icard);
				//等待1s
				param.param=1;
				//修改操作码，进入下一阶段
				this.le_opcode=2;
				return EventDriver.STATUS_WAIT_TIM;
			}
			//没有走到出口则继续前进
			if(!this.p_op.get(se_locked_location+1)){
				//没有被占则锁定该位置，并释放上一个位置。
				//和入场占位操作有些不同，不需要判断尾部是否在path内
				this.p_op.lockPath(se_locked_location+1);
				this.p_op.releasePath(se_locked_location-1);
				return EventDriver.STATUS_READY;
			}else{
				//如果车道被占了则等待下一个调度
				return EventDriver.STATUS_READY;
			}
		}else if(this.le_opcode==0){
			//判断车道有没有被占
			if((!this.p_op.get(se_locked_location))&&(!this.p_op.get(se_locked_location+1))){
				//锁定当前和下一个位置
				this.p_op.lockPath(se_locked_location);
				this.p_op.lockPath(se_locked_location+1);
				//睡眠2s，模拟到车道需要的2s
				param.param=2;
				//将车头置到下一位置。
				se_locked_location++;
				//上车道阶段完成
				le_opcode=1;
				return EventDriver.STATUS_WAIT_TIM;
				}else{
					//如果车道被占了则等待下一个调度
					return EventDriver.STATUS_READY;
				}
			}else if(this.le_opcode==2){
				//已经等待过1s 读卡器已就绪，进入请求离开阶段
				int res=this.p_op.leave();
				if(res==0){
					//请求离开成功
					//等待2s 栏杆抬起
					param.param=2;
					this.le_opcode=3;
					return EventDriver.STATUS_WAIT_TIM;
				}else{
					//请求离开失败
					//进入返回队列
					//退出当前方法
					this.se_locked_location--;//此操作的作用是模拟调头。
					this.direction_back=true;//将汽车的方向设为反方向
					param.car_status=RCar.CAR_STATUS_RE;
					return EventDriver.STATUS_EXIT;
				}
			}else if(this.le_opcode==3){
				//最后的离开阶段
				//需要持续到离开传感器
				//状态较少，所以用if块。最笨的 方法
				//这个阶段没有考虑位置已经被占用的状态
				if(se_locked_location==this.p_op.getMaxPartCount()+2)
				{
					//车尾即将行驶出传感器区域
					//意味着汽车完全离开
					//释放最后一个位置
					this.p_op.releasePath(se_locked_location-2);
					return EventDriver.STATUS_EXIT;
				}
				
				if(se_locked_location==this.p_op.getMaxPartCount()+1)
				{
					//车头即将行驶出传感器区域
					//不再锁定下一个区域
					this.p_op.releasePath(se_locked_location-2);
					se_locked_location++;
					return EventDriver.STATUS_READY;
					
				}
				if(se_locked_location==this.p_op.getMaxPartCount()){
					
					//第一阶段
					//此时的 se_locked_location 在哪  this.p_op.getMaxPartCount() 上
					
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
		//返回的时候要一直扫描原车位有没有被占。一旦被占马上找就近车位
		//入场车不能这样做，就近找车位可能会出现掉头的情况。
		//如果和入场车出现冲突怎么办？
		//阶段分为：0.奔向原来位置	1.占领原车位     2.占领原车位失败后找就近车位
		//3.占领就近车位。
		//没有考虑掉头需要的时间
		switch(this.le_opcode){
		case 0:
			//返回原车位的阶段
			//先看原车位有没有被占
			if(this.p_op.getParkStatus(park_line, park_location)){
				//原车位已经被占
				//进入寻找就近车位状态
				le_opcode=1;
				return EventDriver.STATUS_READY;
			}else{
				//判断是否到了原车位
				if(this.se_locked_location!=this.park_location){
					//没有到原车位，行进一格
					//判断汽车的方向
					if(this.direction_back){
						//反方向
						if(!this.p_op.get(se_locked_location-1)){
							this.p_op.lockPath(se_locked_location-1);
							this.p_op.releasePath(se_locked_location+1);
							se_locked_location--;
							return EventDriver.STATUS_READY;
						}else{
							//如果车道被占了则等待下一个调度
							return EventDriver.STATUS_READY;
						}
					}else{
						//正方向
						if(!this.p_op.get(se_locked_location+1)){
							this.p_op.lockPath(se_locked_location+1);
							this.p_op.releasePath(se_locked_location-1);
							se_locked_location++;
							return EventDriver.STATUS_READY;
						}else{
							//如果车道被占了则等待下一个调度
							return EventDriver.STATUS_READY;
						}
					}
				}else{
					//到了原车位。锁定车位 睡眠2s
					this.p_op.lockPark(park_line, park_location, token);
					le_opcode=2;
					param.param=2;
					return EventDriver.STATUS_WAIT_TIM;
				}
			}
		case 1:
			//寻找就近的车位
			//需要的操作：遍历找到最近的车位。将最近的车位设为原车位，再执行返回原车位的操作即可
			//需要注意可能要重新设置车的方向
			//从se_locked_location开始向两边遍历
			int i=1;
			//最近的查找顺序为：后面的第i个，两边的位子
			//前面第i个，两边的位子
			while(true){
				//注意越界的情况
				//有效车位的范围是 1~max_parts-1
				if(se_locked_location-i>=1){
					if(!this.p_op.getParkStatus(0, se_locked_location-i)){
						//将原车位设为找到的最近车位。
						this.park_line=0;
						this.park_location=se_locked_location-i;
						//设置车方向
						this.direction_back=false;
						//重置操作码，下一轮执行 返回原位置操作
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
					//两边全部越界，说明已经没有空位。
					//这种情况理论上会出现。调度时不会出现。
					//一旦出现则会陷入死循环
					//throw new Throwable();
				}
				
			}
		case 3:
			//成功返回原车位，将进入不死不灭的永生状态
			param.car_status=CAR_STATUS_DE;
			return EventDriver.STATUS_EXIT;
		}
		return 0;
	}
	
	
	@Override
	public int de(Param param) {
		// TODO Auto-generated method stub
		//de阶段的汽车永远不会被调度，但会占用停车位
		return 0;
	}
	
/*	@Override
	public int run(Param param) {
		// TODO Auto-generated method stub
		
		switch(current_status){
		case CURRENT_STATUS_WAITING:
			//执行进入停车场操作。
			current_status=CURRENT_STATUS_GOING_TO_PARK;
			return EventDriver.STATUS_READY;
		case CURRENT_STATUS_GOING_TO_PARK:
			//执行查找车位操作。
			
			 * 
			 
			current_status=CURRENT_STATUS_PARKING;
			return EventDriver.STATUS_READY;
		case CURRENT_STATUS_PARKING:
			
			 * 走到出口。需要避让。
			 
		case CURRENT_STATUS_LEAVING:
			
		}
		return 0;
	}*/
	
	
}
