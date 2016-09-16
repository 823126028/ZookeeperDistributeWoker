package zookeeper.distributewoker.handler;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import zookeeper.distributewoker.Controller;
import zookeeper.distributewoker.ResultHandler.Result;
import zookeeper.distributewoker.comm.Utils;
import zookeeper.distributewoker.handler.comm.TaskStructure;
import zookeeper.distributewoker.handler.taskmanager.TaskChooser;
import zookeeper.distributewoker.handler.workchooser.IWorkerChooser;

public abstract class HandlerRunner extends Thread{
	protected volatile boolean valid;
	protected ReentrantLock lock = new ReentrantLock();
	protected Condition condition = lock.newCondition();
	protected Controller controller;
	protected TaskChooser taskChooser;
	protected IWorkerChooser workerChooser;
	
	public void setController(Controller controller){
		this.controller = controller;
		this.start();
	}
	
	public HandlerRunner(List<TaskStructure> taskList,IWorkerChooser workerChooser){
		this.valid = false;
		this.taskChooser = new TaskChooser(taskList);
		this.workerChooser = workerChooser;
	}
	
	public void valid(){
		this.valid = true;
		onValid();
	}
	
	public void inValid(){
		this.valid = false;
	}
	
	private void onValid(){
		taskChooser.recover(this.controller.getResultHandler().getChildren());
		try{
			lock.lock();
			condition.signal();
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			lock.unlock();
		}
	}
	
	private void waitBeMaster(){
		while(!this.valid){
			try{
				lock.lock();
				condition.await(3, TimeUnit.SECONDS);
			}catch(Exception e){
				e.printStackTrace();
			}finally{
				lock.unlock();
			}
		}
	}
	
	protected abstract boolean handleTask();
	
	protected abstract void doCheck();
	
	protected void createNewResultKey(String taskName, String workerIndex){
		this.controller.getResultHandler().createResultKey(taskName, Utils.getNowTimeStramp(), workerIndex, Result.RUNNING_STATE);
		this.controller.getWorkerHandler().notify(workerIndex);
		this.taskChooser.setCurTaskName(taskName);
	}
	
	protected void 	reNewResultKey(String taskName,long privilege,String workerIndex){
		this.controller.getResultHandler().renewResultKey(taskName, privilege, workerIndex, Result.RUNNING_STATE); 
		this.controller.getWorkerHandler().notify(workerIndex);
	}
	
	
	private void invalidRest(){
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public void run() {
		for(;;){
			waitBeMaster();
			if(!handleTask()){
				invalidRest();
			}
			doCheck();
		}
	}
}
