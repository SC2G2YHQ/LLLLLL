package bo;

import dat.ICard;
import ops.CardReader;
import ops.EventDriver;
import ops.M;
import ops.Park;
import ops.Rail;
import ops.RealCar;
import ops.Sensor;
import ops.dao.CardReader_Operations;
import ops.dao.M_Operations;
import ops.dao.Park_Operations;
import ops.dao.Rail_Operations;
import ops.dao.Sensor_Operations;

/*
 * һ���򵥵��������롣
 * ��һ����Ҳ���мܹ���
 * 
 * �������ϵͳ�ļ��ع��̡�
 * ���뻷����ͳһ����
 * 
 * ��Ҫһ����I/Oģ��
 * ģʽ �ַ�
 */
public class Launcher {
	//����
	//ͬһ������������ͬ���ͣ����߼��ȼۡ�
	//
	public static final int MAX_PARK_LIMIT = 50;
	
	public static void main(String[] args) {
		//��ʼ����˳��
		/*
		 * 1.������
		 * 2.{
		 * M
		 * Rail
		 * CardReader
		 * Sensor
		 * }
		 * 3.Park
		 */
		EventDriver ed=new EventDriver();
		ed.init();
		
		M_Operations mo=new M();
		/*
		 * �������һЩICard
		 * ʹ��GUID��ΪID
		 */
		ICard ic1=new ICard();
		ic1.setID("abea80e30f4347ffb528ae113b950496");
		ic1.setName("�ٺٺ�");
		ICard ic2=new ICard();
		ic2.setID("d34318e70cd845558ed8e0e62f4095d2");
		ic2.setName("������");
		mo.insert(ic1);
		mo.insert(ic2);
		
		Rail_Operations r_in = new Rail();
		
		Rail_Operations r_out = new Rail();
		
		CardReader_Operations c_in = new CardReader();
		
		CardReader_Operations c_out = new CardReader();
		
		Sensor_Operations s_in = new Sensor();
		
		Sensor_Operations s_out = new Sensor();
		//��ʼ��ͣ����
		
		Park_Operations po = new Park("�޷�ʹ�õ�ͣ����",0,mo,c_in,c_out,r_in,r_out,s_in,s_out,MAX_PARK_LIMIT);
		/*
		 * ��ʼ��һ������
		 * ʹ�á��ٺٺ١� ��ICard
		 */
		RealCar rc1=new RealCar(3600,ic1);
		
		ed.addRCar(rc1);
		//��Ҫ���ô˷���
		//��Ȼ��������
		ed.start();
		//��һ���ź�
		ed.sigToWaCar();
	}
	
}
