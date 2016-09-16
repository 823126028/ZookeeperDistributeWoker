package zookeeper.nameservice;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.I0Itec.zkclient.IZkChildListener;
import org.I0Itec.zkclient.IZkStateListener;
import org.I0Itec.zkclient.ZkClient;
import org.apache.zookeeper.Watcher.Event.KeeperState;

import zookeeper.log.LogUtil;
/**
 * @author chenjl
 * 一个名字服务
 * 只加孩子监控和状态监控,如果状态出错
 *
 */
public class NameServiceWatcher{
	private CopyOnWriteArrayList<String> serverIpList = new CopyOnWriteArrayList<String>();
	private ZkClient zkClient;
	private boolean start;
	private String path;
	private boolean closed;
	private boolean validZookeeper;
	private static final int DEFAULT_SESSION_TIME_OUT = 3000;
	private static final int DEFAULT_CONNECTION_TIME_OUT = Integer.MAX_VALUE;
	
	public List<String> getIpList(){
		if(!validZookeeper){
			LogUtil.info("ERROR======= disconnect from zookeeper server");
		}
		return serverIpList;
	}

	public boolean start(){
		return start;
	}
	
	public boolean closed(){
		return closed;
	}
	
	public void close(){
		zkClient.close();
		start = false;
		closed = true;
	}
	
	public void fillServerList(List<String> currentChilds){
		serverIpList.clear();
		LogUtil.info("=====更新可用server=====");
		for (String server : currentChilds) {
			String ip = new String((byte[])zkClient.readData(this.path + "/" + server));
			serverIpList.add(ip);
			LogUtil.info("可用server:" + ip);
		}
		LogUtil.info("=====================");
	}
	
	public NameServiceWatcher(String servers,String path){
		this(servers, path, DEFAULT_SESSION_TIME_OUT, DEFAULT_CONNECTION_TIME_OUT);
	}
	
	public NameServiceWatcher(String servers, String path, int sessionTimeOut, int connectionTimeOut){
		zkClient = new ZkClient(servers, sessionTimeOut, connectionTimeOut);
		this.path = path;
		this.start = false;
		this.closed = false;
		this.validZookeeper = false;;
	}
	
	public void unWatch(){
		if(!start)
			return;
		zkClient.unsubscribeAll();
		serverIpList.clear();
		start = false;
	}
	
	private void inValidZookeeper(){
		validZookeeper = false;
	}
	
	private void validZookeeper(){
		validZookeeper = true;
	}
	
	public void initListeners(){
		initChildChanges();
		initStateListener();
	}
	
	private void initStateListener(){
		zkClient.subscribeStateChanges(new IZkStateListener() {
			@Override
			public void handleStateChanged(KeeperState state) throws Exception {
				if(state.equals(KeeperState.Disconnected)){
					inValidZookeeper();
				}else{
					fillServerList(zkClient.getChildren(path));
					validZookeeper();
				}
			}
			@Override
			public void handleNewSession() throws Exception {
				System.out.println("断线啦");
			}
		});
	}
	
	private void initChildChanges(){
		zkClient.subscribeChildChanges(path, new IZkChildListener() {
			@Override
			public void handleChildChange(String parentPath, List<String> currentChilds) throws Exception {
				fillServerList(currentChilds);
			}
		});
	}
	
	public void watch(){
		//创建父节点,因为使用那个的createParents是true,所以重复也不会报错
		zkClient.createPersistent(path,true);
		fillServerList(zkClient.getChildren(path));
		initListeners();
		start = true;
	}
	
	public static void main(String[] args){
		NameServiceWatcher nameServiceWatcher = new NameServiceWatcher("127.0.0.1:2181", "/app");
		nameServiceWatcher.watch();
		try {
			Thread.sleep(100000000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
