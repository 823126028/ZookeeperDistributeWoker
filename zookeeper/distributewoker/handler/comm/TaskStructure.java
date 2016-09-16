package zookeeper.distributewoker.handler.comm;

public class TaskStructure {
	public String taskName;
	public long privilege;
	public TaskStructure(String taskName, long privilege){
		this.taskName = taskName;
		this.privilege = privilege;
	}
}
