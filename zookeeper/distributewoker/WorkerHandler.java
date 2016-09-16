package zookeeper.distributewoker;
import java.util.List;

import zookeeper.Animal;
import zookeeper.distributewoker.config.PathConfig;
public class WorkerHandler {
	private Animal animal;
	private boolean inited = false;
	
	public WorkerHandler( Animal animal,String workPath){
		this.animal = animal;
	}
	
	public List<String> getWorkers(){
		if(!inited){
			animal.createPersistent(PathConfig.WORK_PATH, PathConfig.WORK_PATH);
			inited = true;
		}
		return animal.getChildren(PathConfig.WORK_PATH);
	}
	
	public void notify(String workIndex){
		this.animal.setData(PathConfig.buildWorkPath(workIndex), workIndex); ;
	}
}
