package ops.dao;

import dat.ICard;

/** 
 * IC������������������֤��ʽ
 *  
 */ 

public interface Card_Verification {
    /** 
     * ���ּ�id����֤��ʽ
     *  
     * @param ICard 
     *            IC�� 
     * @param name 
     *            Ա������
     * @param id 
     *            ����
     * @return ��֤�ɹ���� 
     */ 
	public boolean verify(ICard icard,String name,int id);
	
    /** 
     * ��Կ��֤��ʽ
     *  
     * @param ICard 
     *            IC�� 
     * @param plainTextData 
     *            ����
     * @param cipherData 
     *            ����
     * @return ��֤�ɹ���� 
     * 
     * RSA��Կ��֤��ʽ��ȫ�Խ̸�
     */ 
	public boolean verify(ICard icard,byte[] plainTextData,byte[] cipherData);
	
}
