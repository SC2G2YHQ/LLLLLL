package dat;

/*
 * ��������Ϳ�����ƾ֤�������ڱ�������������������Ϊ�˵��ȵ�λ
 * ���Ե���������һ��ƾ֤�� Token
 * Parkʹ��Token��ʶ������
 */
public class Token {
	int CarID;
	int Handle;
	String ID;
	
	public boolean equals(Token t){
		return t.CarID==this.CarID&&t.Handle==this.Handle;
	}
	
	public Token(int CarID,int Handle){
		this.CarID=CarID;this.Handle=Handle;
	}
}
