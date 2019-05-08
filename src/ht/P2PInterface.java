package ht;

import java.util.Collection;

import core.CBRConnection;
import core.Connection;
import core.NetworkInterface;
import core.Settings;
/**
 * 
 * @author ht
 * P2P interface 主要为了在The One中实现Wi-Fi Direct连接
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
	//组主和未连接状态的组员可以连接，单连接状态的网关可以使用空闲网卡接口和组主连接，未连接状态的网关优先使用P2P网卡与组主连接。反之亦可。
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
					connections.remove(i);
				}
				else {
					i++;
				}
			}
		}else if (this.getHost().getIsGW()) {
			Connection connection = this.connections.get(0);
			NetworkInterface aNetworkInterface = connection.getOtherInterface(this);
			assert connection.isUp() : "Connection " + connection + " was down!";
			if (!isWithinRange(aNetworkInterface)) {
				disconnect(connection,aNetworkInterface);
				connections.remove(0);
			}
		}else {
			Connection connection = this.connections.get(0);
			NetworkInterface aNetworkInterface = connection.getOtherInterface(this);
			assert connection.isUp() : "Connection " + connection + " was down!";
			if (!isWithinRange(aNetworkInterface)) {
				disconnect(connection,aNetworkInterface);
				connections.remove(0);
			}
		}
		// 建立新的连接
		Collection<NetworkInterface> interfaces =
			optimizer.getNearInterfaces(this);
		if(!this.getHost().getIsGO()) {
			if(!this.getHost().getIsGW()) {
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
				//connections.size()==1 意味着本GW的P2P接口已经连接到一个GO了，对于本接口来讲任务已经完成，不需要管Wlan接口是否
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
			}
		}else {
			//组主节点 不能限制连接个数，为了保证整个系统连接关系的同步性
			for (NetworkInterface i : interfaces) {
				if(i.getHost().getIsGO()) {
					continue;
				}else 	if(i.getConnections().size()==0) {
						connect(i);
					}
				}
			}
	}

	@Override
	public void createConnection(NetworkInterface anotherInterface) {
		// TODO Auto-generated method stub
	}

}
