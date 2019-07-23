package interfaces;

import java.util.Collection;

import core.CBRConnection;
import core.Connection;
import core.NetworkInterface;
import core.Settings;
/**
 * 
 * @author ht
 * P2P interface ��ҪΪ����The One��ʵ��Wi-Fi Direct����
 * ֻ����p2p�������߼�����wlan�������߼��ֿ���
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
	//������δ����״̬����Ա�������ӣ�δ����״̬����������ʹ��P2P����������P2P������������
	//����Ҫ�Խӿ��������ͽ����жϣ���ΪTHEONE��ֻ����ͬ�ӿڲ���ͨ��
	//isConnecteds���������жϸýڵ����Ӧ�Ľӿ��Ƿ��Ѿ��������ӣ�����ֻ�жϵ�ǰ�ڵ�ĵ�ǰ�ӿ��Ƿ����Ӧ�ӿ��Ƿ��������
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
			System.out.println(this.getHost().getNodeType()+this.getHost().getAddress()+this.getInterfaceType()+"ʹ��p2p�ӿ�����"+
			anotherInterface.getHost().getNodeType()+anotherInterface.getHost().getAddress()+anotherInterface.getInterfaceType()+"�ɹ�����");
			//TODO ���ڵ����Դ��Ϣ�����������ӵ�GO�ڵ�
			
			anotherInterface.addRN().this.getHost().getResourceId()
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
					//TODO ����RN������GWT��
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
		// �����µ�����
		Collection<NetworkInterface> interfaces =
			optimizer.getNearInterfaces(this);
			if(!this.getHost().getIsGO()) {
				if(!this.getHost().getIsGW()) {
					//��Ա��������������
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
					//connections.size()==1 ��ζ�ű�GW��P2P�ӿ��Ѿ����ӵ�һ��GO�ˣ�
					//ͬʱҪ��֤GWʹ��p2p��������ʱ��GW�ڵ��WLan�ӿ�û�����ӵ���ͬ��GO�ڵ�
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
			//Ϊ�������ʵ�鱣��һ�£�GO�ڵ㲻������������Ա�ڵ㽨������
//			else {
//				//�����ڵ� �����������Ӹ�����Ϊ�˱�֤����ϵͳ���ӹ�ϵ��ͬ���ԡ�
//				//����GO�ڵ������ڵ㲻��ʹ���ĸ������ӿڶ�ֻ�ܴ���һ�����ӣ���ˣ�����GW�ڵ㲻��Ҫ����i.getConnections().size()=1�����
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