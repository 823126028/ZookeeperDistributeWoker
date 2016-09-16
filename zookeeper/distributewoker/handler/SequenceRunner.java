package zookeeper.distributewoker.handler;
import java.util.List;
import zookeeper.distributewoker.ResultHandler.Result;
import zookeeper.distributewoker.comm.Utils;
import zookeeper.distributewoker.config.PathConfig;
import zookeeper.distributewoker.handler.comm.TaskStructure;
import zookeeper.distributewoker.handler.workchooser.IWorkerChooser;

public class SequenceRunner extends HandlerRunner{
	
	public SequenceRunner(List<TaskStructure> taskList, IWorkerChooser workerChooser) {
		super(taskList, workerChooser);
	}

	private boolean firstTime(String workerIndex){
		if(taskChooser.curTaskName() == null){
			this.createNewResultKey(taskChooser.nextTaskName(), workerIndex);
			return true;
		}
		return false;
	}
	
	private boolean expired(long excutedTime){
		return Utils.getNowTimeStramp() - excutedTime - 10 > 0; 
	}	
	
	@Override
	protected boolean handleTask() {
		if(taskChooser.isEnd())
			return false;
		String workerIndex = this.workerChooser.getWorkerIndex(this.controller.getWorkerHandler().getWorkers());
		if(workerIndex == null){
			return false;
		}
		if (firstTime(workerIndex)) {
			return false;
		}
		String data = this.controller.getAnimal().getData(PathConfig.buildResultPath(taskChooser.curTaskName()));
		Result result = Result.makeResult(data);
		if(result.getState().equals(Result.SUCCESS_STATE)){
			if(taskChooser.nextTaskName() != null){
				this.createNewResultKey(taskChooser.nextTaskName(), workerIndex);
			}
			return false;
		}
		if(expired(result.getExcutedTime()) || result.getState().equals(Result.FAIL_STATE)){
			this.reNewResultKey(taskChooser.curTaskName(), result.getBeginTime(), workerIndex);
			return false;
		}
		return false;
	}

	@Override
	protected void doCheck() {
	}
}
