package zookeeper.distributewoker.worker.task;

import zookeeper.distributewoker.ResultHandler.Result;
import zookeeper.distributewoker.worker.Worker;

public interface IWorkerTaskFactory {
	Runnable getRunnerTask(String taskName, Result result, Worker worker);
}
