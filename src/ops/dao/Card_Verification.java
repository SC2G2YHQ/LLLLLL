package ops.dao;

import dat.ICard;

/** 
 * IC卡操作集，有两种验证方式
 *  
 */ 

public interface Card_Verification {
    /** 
     * 名字加id的验证方式
     *  
     * @param ICard 
     *            IC卡 
     * @param name 
     *            员工姓名
     * @param id 
     *            工号
     * @return 验证成功与否 
     */ 
	public boolean verify(ICard icard,String name,int id);
	
    /** 
     * 公钥验证方式
     *  
     * @param ICard 
     *            IC卡 
     * @param plainTextData 
     *            明文
     * @param cipherData 
     *            密文
     * @return 验证成功与否 
     * 
     * RSA公钥验证方式安全性教高
     */ 
	public boolean verify(ICard icard,byte[] plainTextData,byte[] cipherData);
	
}
