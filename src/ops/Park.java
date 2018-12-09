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
	
	private ops.EventDriver event_driver;//Park的代码也是由Event Driver调度的。所以event_driver应该是由param传进。
	
	private int left[];
	private int right[];
	private int path[];

	public void init(){}
	
	
}
