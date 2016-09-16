package zookeeper.distributewoker.worker.task;

import zookeeper.distributewoker.ResultHandler.Result;
import zookeeper.distributewoker.worker.Worker;

public class RandomSayHelloFactory implements IWorkerTaskFactory{

	@Override
	public Runnable getRunnerTask(String taskName, Result result, Worker worker) {
		return new RandomSayHelloTask(taskName, result, worker);
	}

}
