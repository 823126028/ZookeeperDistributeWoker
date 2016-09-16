package zookeeper.distributewoker.handler.taskmanager;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import zookeeper.distributewoker.handler.comm.TaskStructure;

public class TaskChooser {
	private List<TaskStructure> taskList;
	private Map<String, Integer> indexMap;
	private String curTaskName = null;
	
	public TaskChooser(List<TaskStructure> taskList){
		this.taskList = sorted(taskList);
		refreshMap();
	}
	
	public void recover(List<String> allExistsTaskResult){
		for (String task : allExistsTaskResult) {
			if(indexMap.containsKey(task)){
				curTaskName = task;
			}else{
				break;
			}
		}
	}
	
	public String curTaskName(){
		return curTaskName;
	}
	
	
	public boolean isEnd(){
		if(curTaskName != null){
			return indexMap.get(curTaskName) >= taskList.size() - 1;
		}
		return false;
	}
	
	public String nextTaskName(){
		if(curTaskName == null){
			return taskList.get(0).taskName;
		}
		if(indexMap.get(curTaskName) >= taskList.size() - 1){
			return null;
		}
		return taskList.get(indexMap.get(curTaskName) + 1).taskName;
	}
	
	public TaskStructure getStructureByName(String taskName){
		return taskList.get(indexMap.get(taskName));
	}
	
	public void setCurTaskName(String curTaskName){
		this.curTaskName = curTaskName;
	}
	
	private void refreshMap(){
		indexMap = new HashMap<>();
		for(int i = 0; i < taskList.size(); i++){
			indexMap.put(taskList.get(i).taskName, i);
		}
	}
	
	private List<TaskStructure> sorted(List<TaskStructure> taskList){
		Collections.sort(taskList, new Comparator<TaskStructure>() {
			@Override
			public int compare(TaskStructure o1, TaskStructure o2) {
				if(o1.privilege > o2.privilege || ((o1.privilege == o2.privilege) && o1.taskName.compareTo(o1.taskName) == 1)){
					return 1;
				}
				if(o1.privilege < o2.privilege || ((o1.privilege == o2.privilege) && o1.taskName.compareTo(o1.taskName) == -1)){
					return -1;
				}
				return 0;
			}
		});
		return taskList;
	}
	
	
	
}
