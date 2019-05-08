package ht;

import java.util.Collection;

import core.CBRConnection;
import core.Connection;
import core.NetworkInterface;
import core.Settings;
/**
 * 
 * @author ht
 * P2P interface ��ҪΪ����The One��ʵ��Wi-Fi Direct����
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
	//������δ����״̬����Ա�������ӣ�������״̬�����ؿ���ʹ�ÿ��������ӿں��������ӣ�δ����״̬����������ʹ��P2P�������������ӡ���֮��ɡ�
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
		}
	}

	@Override
	/**
	 *����P2P �������������⣬ֻ������һ���豸���������ؽڵ㣬��һ��P2P��������GO��Wlan��������LC GO
	 *���ڲ�ͬ��ɫ�Ľڵ��ȡ��ͬ�ĸ��¹���
	 */
	public void update() {
		// TODO Auto-generated method stub
		if (optimizer == null) {
			return; /* nothing to do */
		}
		//�Ͽ��ɵ�����
		optimizer.updateLocation(this);
		if(this.getHost().getIsGO()) {
			for(int i =0; i<this.connections.size();) {
				Connection con = this.connections.get(i);
				NetworkInterface anotherInterface = con.getOtherInterface(this);
				//assert ���� assert [�������ʽ: ������ʽ]�� ����������ʽΪ�棬��������ִ�У������׳�java.lang.AssertionError
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
		// �����µ�����
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
				//���ؽڵ�����
				//connections.size()==1 ��ζ�ű�GW��P2P�ӿ��Ѿ����ӵ�һ��GO�ˣ����ڱ��ӿ����������Ѿ���ɣ�����Ҫ��Wlan�ӿ��Ƿ�
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
			//�����ڵ� �����������Ӹ�����Ϊ�˱�֤����ϵͳ���ӹ�ϵ��ͬ����
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
