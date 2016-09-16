package zookeeper;
import java.util.List;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.exception.ZkNodeExistsException;

public class Animal{
	private ZkClient zkClient;
	private String key;
	
	private final static int DEFAULT_SESSION_TIME_OUT = 3000;
	private final static int DEFAULT_CONNECTION_TIME_OUT = Integer.MAX_VALUE;
	
	public String getData(String path){
		 byte[] data = zkClient.readData(path, true);
		 if(data == null){
			 return null;
		 }else{
			 return new String(data);
		 }
	}
	
	public void setData(String path,String value){
		zkClient.writeData(path, value.getBytes());
	}
	
	public List<String> getChildren(String path){
		 List<String> children = zkClient.getChildren(path);
		 return children;
	}
	
	public ZkClient getClient(){
		return zkClient;
	}
	
	public String key(){
		return key;
	}
	
	
	public Animal(String host,String key){
		this(host, key, DEFAULT_SESSION_TIME_OUT, DEFAULT_CONNECTION_TIME_OUT);
	}
	
	public Animal(String host, String key, int sessionTimeOut,int connectionTimeOut){
		zkClient = new ZkClient(host,sessionTimeOut,connectionTimeOut);
		this.key = key;
	}
	
	public boolean hasParent(String path){
		return path.lastIndexOf("/") != -1 && path.lastIndexOf("/") != 0;
	}
	
	public String parseParent(String path){
		return path.substring(0,path.lastIndexOf("/"));
	}
	
	public boolean createPersistent(String path,String key){
		boolean created = true;
		String parent = null;
		if(hasParent(path)){
			parent = parseParent(path);
			zkClient.createPersistent(parent, true);
		}
		try{
			zkClient.createPersistent(path,key.getBytes());
		}catch(ZkNodeExistsException e){
			created = false;
		}
		return created;
	}
	
	public boolean createEphemeral(
			String path,
			String key,
			boolean race
			){
		
		boolean grabbed = true;
		String parent = null;
		if(hasParent(path)){
			parent = parseParent(path);
			zkClient.createPersistent(parent, true);
		}
		try{
			zkClient.createEphemeral(path,key.getBytes());
		}catch(ZkNodeExistsException e){
			grabbed = false;
			System.out.println("already exists");
			if(!race){
				throw e;
			}
		}
		return grabbed;
	}
	
	public void unRegister(String path){
		byte[] result = zkClient.readData(path);
		if(new String(result).equals(key())){
			zkClient.unsubscribeAll();
			zkClient.delete(path);
		}
	}	
}