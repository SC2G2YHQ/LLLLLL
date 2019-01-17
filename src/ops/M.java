package ops;

import java.util.LinkedList;
import java.util.List;

import dat.ICard;
import ops.dao.Card_Verification;
import ops.dao.M_Operations;

public class M implements M_Operations{

	
	
	protected List<ICard> cards;
	
	/*
	 *当前 icard 起到会话的作用。调用exist后会保存查询过的ICard，后面验证步骤会用到
	 *为了安全一般这个变量会有一个有效期。此处无法设置有效期，直到下一次调用exist。
	 *为什么不用ID作为口令？因为名字可能重复，不能用作唯一标识。
	 */
	private ICard current_icard;
	
	/*
	 * Card_Verification是最初的验证接口。更改架构后不再使用
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
	 * 使用ID查询ICard并建立会话
	 */

	@Override
	public boolean exist(String ID) {
		// TODO Auto-generated method stub
		/*
		 * 使用map效率会更高。ID到ICard映射
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
			if(this.current_icard.getName().equals(name))//注意此处有一个bug。current_icard的name为null时会出错
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
		if(true);//这个判断是icard数量判断，M所保存icard的数量有上限。
		if(icard==null)
			return ERROR_INVALID_CARD;
		/*
		 * ID和name都不能为空
		 * ID或name为空串的情况未被考虑
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
