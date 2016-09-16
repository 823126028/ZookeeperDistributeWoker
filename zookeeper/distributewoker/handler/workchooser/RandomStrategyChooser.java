package zookeeper.distributewoker.handler.workchooser;

import java.util.List;
import java.util.Random;

public class RandomStrategyChooser implements IWorkerChooser{
	private Random r = new Random();
	
	@Override
	public String getWorkerIndex(List<String> allWorkers) {
		if(allWorkers.size() == 0){
			return null;
		}
		return allWorkers.get(r.nextInt(allWorkers.size()));
	}

}
