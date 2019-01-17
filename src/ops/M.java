package ops;

import java.util.LinkedList;
import java.util.List;

import dat.ICard;
import ops.dao.Card_Verification;
import ops.dao.M_Operations;

public class M implements M_Operations{

	
	
	protected List<ICard> cards;
	
	/*
	 *��ǰ icard �𵽻Ự�����á�����exist��ᱣ���ѯ����ICard��������֤������õ�
	 *Ϊ�˰�ȫһ�������������һ����Ч�ڡ��˴��޷�������Ч�ڣ�ֱ����һ�ε���exist��
	 *Ϊʲô����ID��Ϊ�����Ϊ���ֿ����ظ�����������Ψһ��ʶ��
	 */
	private ICard current_icard;
	
	/*
	 * Card_Verification���������֤�ӿڡ����ļܹ�����ʹ��
	 */
	protected Card_Verification cv;
	
	public M(){
		this.cards=new LinkedList<ICard>();
	}
	
	public int register_card_operations(Card_Verification cv){
		this.cv=cv;
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * @see ops.dao.M_Operations#exist(java.lang.String)
	 * ʹ��ID��ѯICard�������Ự
	 */

	@Override
	public boolean exist(String ID) {
		// TODO Auto-generated method stub
		/*
		 * ʹ��mapЧ�ʻ���ߡ�ID��ICardӳ��
		 */
		if(this.cards==null)
			return false;
		for(ICard ic:cards){
			if(ic.getID()==ID){
				this.current_icard=ic;
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean Verification(String name) {
		// TODO Auto-generated method stub
		if(this.current_icard==null)
			return false;
		else
			if(this.current_icard.getName().equals(name))//ע��˴���һ��bug��current_icard��nameΪnullʱ�����
				return true;
			else
				return false;
	}

	@Override
	public boolean Verification(byte[] cipherData) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public byte[] getRandomPlainTextData() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int insert(ICard icard) {
		// TODO Auto-generated method stub
		if(this.cards==null)
			return ERROR_NULL_LIST;
		if(true);//����ж���icard�����жϣ�M������icard�����������ޡ�
		if(icard==null)
			return ERROR_INVALID_CARD;
		/*
		 * ID��name������Ϊ��
		 * ID��nameΪ�մ������δ������
		 */
		if(icard.getID()==null||icard.getName()==null)
			return ERROR_INVALID_CARD;
		
		for(ICard ic:cards){
			if(ic.equals(icard)){
				return ERROR_EXIST_CARD;
			}
		}
		cards.add(icard);
		return SUCCESS;
	}

	@Override
	public int delete(ICard icard) {
		// TODO Auto-generated method stub
		if(this.cards==null)
			return ERROR_NULL_LIST;
		if(icard==null)
			return ERROR_INVALID_CARD;
		for(ICard ic:cards){
			if(ic.equals(icard)){
				cards.remove(ic);
				return SUCCESS;
			}
		}
		return SUCCESS;
	}

}
