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
	 *对于WlanInterface，仅对GW节点有效
	 */
	public void update() {
		// TODO Auto-generated method stub
		if (optimizer == null) {
			return; /* nothing to do */
		}
		//断开旧的连接
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
		Collection<NetworkInterface> interfaces = optimizer.getNearInterfaces(this);
		if(this.getHost().getIsGW()&&this.connections.size()==0) {
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

	@Override
	public void createConnection(NetworkInterface anotherInterface) {
		// TODO Auto-generated method stub
	}
}
