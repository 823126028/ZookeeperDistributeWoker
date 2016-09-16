package zookeeper.distributewoker.handler.workchooser;

import java.util.List;

public interface IWorkerChooser {
	public String getWorkerIndex(List<String> allWorkers);
}
