package dat;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
/*
 * 
 * ICard 不能用作凭证。汽车始终持有ICard，但凭证只在park内才有。
 * 
 */
public class ICard {
	private String Name;
	private String ID;
	private RSAPublicKey public_key;
	private RSAPrivateKey private_key;
	
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
	
	
	public RSAPrivateKey getPrivate_key() {
		return private_key;
	}
	public void setPrivate_key(RSAPrivateKey private_key) {
		this.private_key = private_key;
	}
	
	public boolean equals(ICard ICard){
		if(ICard!=null)
			return ICard.ID==this.ID;
		return false;
	}
}
