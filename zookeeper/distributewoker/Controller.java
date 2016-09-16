package zookeeper.distributewoker;

import zookeeper.Animal;
import zookeeper.distributewoker.config.PathConfig;
import zookeeper.distributewoker.handler.HandlerRunner;
import zookeeper.election.MasterSlaveSwitch;

public class Controller {
	private MasterSlaveSwitch masterSlaveSwitch;
	private HandlerRunner handlerRunner;
	private ResultHandler resultHandler;
	private WorkerHandler workerHandler;
	
	public Animal getAnimal(){
		return masterSlaveSwitch.getAnimal();
	}
	
	public void doClearAllResult(){
		masterSlaveSwitch.getAnimal().getClient().deleteRecursive(PathConfig.RESULT_PATH);
	}
	
	public Controller(String host, String masterPath, String ip,HandlerRunner handdlerRunner) throws InterruptedException{
		this.handlerRunner = handdlerRunner;
		this.handlerRunner.setController(this);
		Runnable doAfterOwn = new Runnable() {
			@Override
			public void run() {
				handlerRunner.valid();
			}
		};
		Runnable doAfterRelease = new Runnable() {
			@Override
			public void run() {
				handlerRunner.inValid();
			}
		};
	
		this.masterSlaveSwitch = new MasterSlaveSwitch(host, masterPath, ip, doAfterOwn, doAfterRelease);
		this.resultHandler = new ResultHandler(this.getAnimal());
		this.workerHandler = new WorkerHandler(this.getAnimal(), PathConfig.WORK_PATH);
	}
	
	public boolean isMaster(){
		return masterSlaveSwitch.activeOwner();
	}
	
	public void start(){
		masterSlaveSwitch.start();
	}

	public WorkerHandler getWorkerHandler() {
		return workerHandler;
	}
	
	public ResultHandler getResultHandler(){
		return resultHandler;
	}
}
