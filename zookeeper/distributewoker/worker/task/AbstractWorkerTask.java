package zookeeper.distributewoker.worker.task;

import zookeeper.distributewoker.ResultHandler.Result;
import zookeeper.distributewoker.worker.Worker;

public abstract class AbstractWorkerTask implements Runnable{
	protected Worker worker;
	protected Result result;
	protected String taskName;
	
	public AbstractWorkerTask(String taskName, Result result, Worker worker){
		this.worker = worker;
		this.taskName = taskName;
		this.result = result;
	}
	
	protected abstract boolean doRealTask();
	protected abstract boolean callBack();
	
	public void run() {
		boolean suc = doRealTask();
		boolean valid = worker.checkTaskIsValid(taskName, result);
		if(!valid){
			callBack();
		}else{
			if(suc){
				worker.doAfterSucess(taskName, result);
			}else{
				worker.doAfterFailue(taskName, result);
			}
		}
	}
}
