package zookeeper.distributewoker.worker.task;

import zookeeper.distributewoker.ResultHandler.Result;
import zookeeper.distributewoker.worker.Worker;

public class DefaultWorkerTaskFactory implements IWorkerTaskFactory{
	@Override
	public AbstractWorkerTask getRunnerTask(String taskName, Result result, Worker worker) {
		//TODO
		return null;
	}
}
