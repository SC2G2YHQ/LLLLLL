package dat;

import java.security.interfaces.RSAPublicKey;
/*
 * 
 * ICard ��������ƾ֤������ʼ�ճ���ICard����ƾֻ֤��park�ڲ��С�
 * 
 */
public class ICard {
	private String Name;
	private String ID;
	private RSAPublicKey public_key;
	
	public String getName() {
		return Name;
	}
	public void setName(String name) {
		Name = name;
	}
	public String getID() {
		return ID;
	}
	public void setID(String iD) {
		ID = iD;
	}
	public RSAPublicKey getPublic_key() {
		return public_key;
	}
	public void setPublic_key(RSAPublicKey public_key) {
		this.public_key = public_key;
	}
	
	public boolean equals(ICard ICard){
		if(ICard!=null)
			return ICard.ID==this.ID;
		return false;
	}
}
