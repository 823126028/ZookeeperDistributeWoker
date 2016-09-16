package zookeeper.distributewoker.worker.task;

import java.util.Random;

import zookeeper.distributewoker.ResultHandler.Result;
import zookeeper.distributewoker.worker.Worker;
import zookeeper.log.LogUtil;

public class RandomSayHelloTask extends AbstractWorkerTask{
	private Random random = new Random();
	public RandomSayHelloTask(String taskName, Result result, Worker worker) {
		super(taskName, result, worker);
	}

	@Override
	protected boolean doRealTask(){
		int value = random.nextInt(5);
		if(value < 1){
			LogUtil.info("task sucess:" + this.taskName);
			return true;
		}
		if(value < 3){
			try{
				Thread.sleep(10000);
			}catch(Exception e){
				e.printStackTrace();
			}
			return false;
		}
		return false;
	}

	@Override
	protected boolean callBack() {
		LogUtil.info("call back :" + this.taskName);
		return false;
	}

}
