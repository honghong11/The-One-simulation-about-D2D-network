package routing;

/**
 * 
 * @author ht
 *该路由过程包括两个具体的路由子过程：资源发现路由和资源回溯路由。通过传递的数据包类型进行判断采用哪种路由方式。
 *
 */
public class ICNAssistedRouter  extends ActiveRouter{

	protected ICNAssistedRouter(ActiveRouter r) {
		super(r);
		// TODO Auto-generated constructor stub
	}

	@Override
	public MessageRouter replicate() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void update() {
		super.update();
		
	}
}
