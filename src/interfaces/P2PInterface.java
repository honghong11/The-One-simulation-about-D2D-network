package interfaces;

import java.util.Collection;

import core.CBRConnection;
import core.Connection;
import core.NetworkInterface;
import core.Settings;
/**
 * 
 * @author ht
 * P2P interface 主要为了在The One中实现Wi-Fi Direct连接
 * 只负责p2p网卡的逻辑，与wlan网卡的逻辑分开来
 */
public class P2PInterface extends NetworkInterface{
	public P2PInterface(Settings s) {
		super(s);
	}
	
	public P2PInterface(P2PInterface p2pInterface) {
		super(p2pInterface);
	}
	
	@Override
	public NetworkInterface replicate() {
		return new P2PInterface(this);
	}

	@Override
	//组主和未连接状态的组员可以连接，未连接状态的网关优先使用P2P网卡与组主P2P网卡建立连接
	//不需要对接口连接类型进行判断，因为THEONE中只有相同接口才能通信
	//isConnecteds函数不能判断该节点与对应的接口是否已经存在连接，而是只判断当前节点的当前接口是否与对应接口是否存在连接
	public void connect(NetworkInterface anotherInterface) {
		if (isScanning()
				&& anotherInterface.getHost().isRadioActive()
				&& isWithinRange(anotherInterface)
				&& !isConnected(anotherInterface)
				&& (this != anotherInterface)) {
			// new contact within range
			// connection speed is the lower one of the two speeds
			int conSpeed = anotherInterface.getTransmitSpeed(this);
			if (conSpeed > this.transmitSpeed) {
				conSpeed = this.transmitSpeed;
			}
			//CBRConnection 恒定传输速率
			Connection con = new CBRConnection(this.host, this,
					anotherInterface.getHost(), anotherInterface, conSpeed);
			connect(con,anotherInterface);
			System.out.println(this.getHost().getNodeType()+this.getHost().getAddress()+this.getInterfaceType()+"使用p2p接口连接"+
			anotherInterface.getHost().getNodeType()+anotherInterface.getHost().getAddress()+anotherInterface.getInterfaceType()+"成功！！");
			//TODO 将节点的资源信息交付给所连接的GO节点
			
			anotherInterface.addRN().this.getHost().getResourceId()
		}
	}

	@Override
	/**
	 *对于P2P 网卡，除组主外，只能连接一个设备。对于网关节点，是一个P2P网卡连接GO，Wlan网卡连接LC GO
	 *对于不同角色的节点采取不同的更新过程
	 */
	public void update() {
		// TODO Auto-generated method stub
		if (optimizer == null) {
			return; /* nothing to do */
		}
		//断开旧的连接
		optimizer.updateLocation(this);
		if(this.getHost().getIsGO()) {
			for(int i =0; i<this.connections.size();) {
				Connection con = this.connections.get(i);
				NetworkInterface anotherInterface = con.getOtherInterface(this);
				//assert 断言 assert [布尔表达式: 错误表达式]。 如果布尔表达式为真，则程序继续执行，否则抛出java.lang.AssertionError
				assert con.isUp() : "Connection " + con + " was down!";
				if (!isWithinRange(anotherInterface)) {
					disconnect(con,anotherInterface);
					//TODO 更新RN表，更新GWT表
					connections.remove(i);
				}
				else {
					i++;
				}
			}
		}else if (this.getHost().getIsGW()) {
			if(this.connections.size()>0) {
				Connection connection = this.connections.get(0);
				NetworkInterface aNetworkInterface = connection.getOtherInterface(this);
				assert connection.isUp() : "Connection " + connection + " was down!";
				if (!isWithinRange(aNetworkInterface)) {
					disconnect(connection,aNetworkInterface);
					connections.remove(0);
				}
			}
		}else {
			if(this.connections.size()>0) {
				Connection connection = this.connections.get(0);
				NetworkInterface aNetworkInterface = connection.getOtherInterface(this);
				assert connection.isUp() : "Connection " + connection + " was down!";
				if (!isWithinRange(aNetworkInterface)) {
					disconnect(connection,aNetworkInterface);
					connections.remove(0);
				}
			}
		} 
		// 建立新的连接
		Collection<NetworkInterface> interfaces =
			optimizer.getNearInterfaces(this);
			if(!this.getHost().getIsGO()) {
				if(!this.getHost().getIsGW()) {
					//组员与组主建立连接
					if(this.connections.size()==0) {
						for(NetworkInterface i: interfaces) {
							if(i.getHost().getIsGO()) {
								double probabilityConGO = Math.random();
								if(probabilityConGO>0.5) {
									connect(i);
									break;
								}
							}
						}
					}
				}else {
					//网关节点连接
					//connections.size()==1 意味着本GW的P2P接口已经连接到一个GO了，
					//同时要保证GW使用p2p网卡连接时，GW节点的WLan接口没有连接到相同的GO节点
					boolean isConnected = false;      
					if(this.connections.size()==0) {
						for(NetworkInterface i: interfaces) {
							if(i.getHost().getIsGO()) {
								double probabilityConGO = Math.random();
								if(probabilityConGO>0.5) {
									for(NetworkInterface networkInterface:this.getHost().getInterfaces()) {
										if(networkInterface.getConnections().size()>0) {
											for(Connection connection: networkInterface.getConnections()) {
												if(connection.getOtherInterface(networkInterface).equals(i)) { 
													isConnected = true;
													break;
												}
											}
										}
										if(isConnected) {
											break;
										}
									}
									if(!isConnected) {
										connect(i);
										break;
									}else {
										isConnected = false;
									}
								}
							}
						}
					}
				}
			}
			//为了与真机实验保持一致，GO节点不允许主动与组员节点建立连接
//			else {
//				//组主节点 不能限制连接个数，为了保证整个系统连接关系的同步性。
//				//除了GO节点其他节点不论使用哪个网卡接口都只能存在一个连接，因此，对于GW节点不需要考虑i.getConnections().size()=1的情况
//				for (NetworkInterface i : interfaces) {   
//					if(i.getHost().getIsGO()) {
//						continue;
//					}else  if(i.getConnections().size()==0) {
//							connect(i);
//						}
//					}
//				}
	}

	@Override
	public void createConnection(NetworkInterface anotherInterface) {
		// TODO Auto-generated method stub
	}

}