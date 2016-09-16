package zookeeper.distributewoker.handler;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import zookeeper.distributewoker.ResultHandler.Result;
import zookeeper.distributewoker.comm.CoolDownContainer;
import zookeeper.distributewoker.comm.Utils;
import zookeeper.distributewoker.handler.comm.TaskStructure;
import zookeeper.distributewoker.handler.workchooser.IWorkerChooser;

public class TimedRunner extends HandlerRunner{
	private CoolDownContainer CoolDownContainer;
	

	@Override
	public void doCheck() {
		CoolDownContainer.tick();
	}
	
	public TimedRunner(List<TaskStructure> taskList,IWorkerChooser workerChooser){
		super(taskList,workerChooser);
		CoolDownContainer = new CoolDownContainer(new Runnable() {
			@Override
			public void run() {
				Map<String,Result> retryMap = findAllRetryMap();
				retry(retryMap);
			}
		}, 5);
	}
	
	

	
	@Override
	public boolean handleTask() {
		String nextTask = taskChooser.nextTaskName();
		if(nextTask == null){
			return false;
		}
		TaskStructure nextStructure = taskChooser.getStructureByName(nextTask);
		if(Utils.getNowTimeStramp() < nextStructure.privilege){
			return false;
		}
		String workerIndex = this.workerChooser.getWorkerIndex(this.controller.getWorkerHandler().getWorkers());
		if(workerIndex == null){
			return false;
		}
		this.createNewResultKey(nextStructure.taskName, workerIndex);
		return true;
	}
		
	private void retry(Map<String,Result> retryMap){
		for (Entry<String, Result> retryEntry : retryMap.entrySet()) {
			String workerIndex = this.workerChooser.getWorkerIndex(this.controller.getWorkerHandler().getWorkers());
			if(workerIndex == null){
				return;
			}
			this.reNewResultKey(retryEntry.getKey(), retryEntry.getValue().getBeginTime(), workerIndex);
		}
	}
	
	private Map<String,Result> findAllRetryMap(){
		Map<String,Result> allResultMap = this.controller.getResultHandler().getChildrenAndDtas();
		Map<String,Result> retryMap = new HashMap<String,Result>();
		for (Entry<String, Result> entry : allResultMap.entrySet()) {
			if(entry.getValue().getState().equals(Result.SUCCESS_STATE)){
				continue;
			}
			if(entry.getValue().getState().equals(Result.FAIL_STATE)){
				retryMap.put(entry.getKey(), entry.getValue());
			}
			if(( entry.getValue().getState().equals(Result.RUNNING_STATE) || entry.getValue().getState().equals(Result.RECEIVED_STATE))&& expired(entry.getValue().getExcutedTime())){
				retryMap.put(entry.getKey(),  entry.getValue());
			}
		}
		return retryMap;
	}
	
	private boolean expired(long excutedTime){
		return Utils.getNowTimeStramp() - excutedTime - 10 > 0; 
	}	
}
