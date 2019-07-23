package interfaces;

import java.util.Collection;

import core.CBRConnection;
import core.Connection;
import core.NetworkInterface;
import core.Settings;

/**
 * 
 * @author ht
 * 所有节点也都拥有WlanInterface，但是仅GW节点可以使用WlanInterface
 *
 */
public class WlanInterface extends NetworkInterface{
	public WlanInterface(Settings s) {
		super(s);
	}
	
	public WlanInterface(WlanInterface wlanInterface) {
		super(wlanInterface);
	}
	
	@Override
	public NetworkInterface replicate() {
		return new WlanInterface(this);
	}

	@Override
	//网关节点使用Wlan网卡，如果当前Wlan网卡连接数为0，并且附近有组主存在，则可以进行连接，
	//但是要注意如果GW节点与一个GO是连接的，那么新建的连接不能是本组主
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
			System.out.println(this.getHost().getNodeType()+this.getHost().getAddress()+this.getInterfaceType()+"使用Wlan接口连接"+
			anotherInterface.getHost().getNodeType()+anotherInterface.getHost().getAddress()+anotherInterface.getInterfaceType()+"成功！！");
		}
	}

	@Override
	/**
	 *对于WlanInterface，仅对GW节点有效
	 */
	public void update() {
		// TODO Auto-generated method stub
		if (optimizer == null) {
			return; /* nothing to do */
		}
		//断开旧的连接，对于Wlan接口仅仅需要处理GW节点
		optimizer.updateLocation(this);
		if(this.getHost().getIsGW()) {
			if(this.connections.size()>0) {
				Connection connection = this.connections.get(0);
				NetworkInterface networkInterface = connection.getOtherInterface(this);
				assert connection.isUp() : "Connection " + connection + " was down!";
				if(!isWithinRange(networkInterface)) {
					disconnect(connection, networkInterface);
					connections.remove(connection);
				}
			}
		}
		
		//建立新的连接, 随机和一个组主建立LC连接
		//注意，当前GW节点如果已经通过p2p接口与某GO节点建立的连接，则不能再使用Wlan接口建立连接
		Collection<NetworkInterface> interfaces = optimizer.getNearInterfaces(this);
		boolean isConnected = false;
		if(this.getHost().getIsGW()&&this.connections.size()==0) {
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

	@Override
	public void createConnection(NetworkInterface anotherInterface) {
		// TODO Auto-generated method stub
	}
}
