package zookeeper.distributewoker.comm;

public class CoolDownContainer {
	private long lastAccessTime;
	private final long COOL_DOWN_TIME;
	private Runnable runnable;
	
	public CoolDownContainer(Runnable runnable,int coolDownTime){
		this.runnable = runnable;
		this.COOL_DOWN_TIME = coolDownTime;
		lastAccessTime = Utils.getNowTimeStramp();
	}
	
	public void tick(){
		if(Utils.getNowTimeStramp() - lastAccessTime > COOL_DOWN_TIME){
			runnable.run();
			lastAccessTime = Utils.getNowTimeStramp();
		}
	}
	
}
