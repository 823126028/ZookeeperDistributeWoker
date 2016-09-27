package zookeeper.election;

import org.I0Itec.zkclient.IZkDataListener;
import org.I0Itec.zkclient.IZkStateListener;
import org.apache.zookeeper.Watcher.Event.KeeperState;

import zookeeper.Animal;
import zookeeper.log.LogUtil;

public class MasterSlaveSwitch {
	private Animal animal;
	private String activeIP;
	private IZkDataListener zkDataListener;
	private IZkStateListener zkStateListener;
	private Runnable doAfterOwn;
	private Runnable doAfterRelease;
	private String path;
	
	public Animal getAnimal(){
		return animal;
	}
	
	public boolean activeOwner(){
		return this.animal.key().equals(activeIP);
	}
	
	private boolean sameKey(String ip){
		return this.animal.key().equals(ip);
	}

	void initDataListener(){
		zkStateListener = new IZkStateListener() {
			@Override
			public void handleStateChanged(KeeperState state) throws Exception {
				if(state == KeeperState.Disconnected){
					return;
				}
				//重新连上
				String nowActiveIp = animal.getData(path);
				if(activeOwner()){
					if(!sameKey(nowActiveIp)){
						doAfterRelease.run();
					}
				}
				activeIP = nowActiveIp;
			}
			
			@Override
			public void handleNewSession() throws Exception {
			}
		};
		
		zkDataListener = new IZkDataListener() {
			@Override
			public void handleDataDeleted(String dataPath) throws Exception {
				if(activeOwner()){
					doAfterRelease.run();
				}
				activeIP = null;
				animal.createEphemeral(path, animal.key(), true);
			}
			
			@Override
			public void handleDataChange(String dataPath, Object data) throws Exception {
					String nowActiveIP = new String((byte[])data);
					if(sameKey(nowActiveIP)){
						doAfterOwn.run();
					}
					activeIP = nowActiveIP;
			}
		};
	}
	
	public void start(){
		animal.getClient().subscribeDataChanges(path, zkDataListener);
		animal.getClient().subscribeStateChanges(zkStateListener);
		boolean grabbed = animal.createEphemeral(path, animal.key(), true);
		if(grabbed){
			System.out.println("grabbed");
			activeIP = animal.key();
			doAfterOwn.run();
		}
		else{
			System.out.println("not grabbed");
			activeIP = animal.getData(this.path);
		}
		LogUtil.info("activeIp:" + activeIP);
	}
	
	public MasterSlaveSwitch(String host,String path,String ip,Runnable doAfterOwn, Runnable doAfterRelease){
		animal = new Animal(host,ip);
		this.doAfterOwn = doAfterOwn;
		this.doAfterRelease = doAfterRelease;
		this.path = path;
		initDataListener();
	}
}
