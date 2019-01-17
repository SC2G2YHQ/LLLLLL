package dat;

/*
 * 汽车本身就可用作凭证。但是在本程序中汽车基类又作为了调度单位
 * 所以单独构建了一个凭证类 Token
 * Park使用Token标识汽车。
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
