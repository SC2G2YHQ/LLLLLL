package ops;

import java.util.HashSet;

import dat.Token;

public class Park {
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
	
	private int left[];
	private int right[];
	private int path[];

	public void init(){}
	
	
}
