package interfaces;

import java.util.Collection;

import core.CBRConnection;
import core.Connection;
import core.NetworkInterface;
import core.Settings;

/**
 * 
 * @author ht
 * ���нڵ�Ҳ��ӵ��WlanInterface�����ǽ�GW�ڵ����ʹ��WlanInterface
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
	//���ؽڵ�ʹ��Wlan�����������ǰWlan����������Ϊ0�����Ҹ������������ڣ�����Խ������ӣ�
	//����Ҫע�����GW�ڵ���һ��GO�����ӵģ���ô�½������Ӳ����Ǳ�����
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
			//CBRConnection �㶨��������
			Connection con = new CBRConnection(this.host, this,
					anotherInterface.getHost(), anotherInterface, conSpeed);
			connect(con,anotherInterface);
			System.out.println(this.getHost().getNodeType()+this.getHost().getAddress()+this.getInterfaceType()+"ʹ��Wlan�ӿ�����"+
			anotherInterface.getHost().getNodeType()+anotherInterface.getHost().getAddress()+anotherInterface.getInterfaceType()+"�ɹ�����");
		}
	}

	@Override
	/**
	 *����WlanInterface������GW�ڵ���Ч
	 */
	public void update() {
		// TODO Auto-generated method stub
		if (optimizer == null) {
			return; /* nothing to do */
		}
		//�Ͽ��ɵ����ӣ�����Wlan�ӿڽ�����Ҫ����GW�ڵ�
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
		
		//�����µ�����, �����һ����������LC����
		//ע�⣬��ǰGW�ڵ�����Ѿ�ͨ��p2p�ӿ���ĳGO�ڵ㽨�������ӣ�������ʹ��Wlan�ӿڽ�������
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
