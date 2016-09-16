package zookeeper.distributewoker.worker;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.I0Itec.zkclient.IZkDataListener;

import zookeeper.Animal;
import zookeeper.distributewoker.ResultHandler;
import zookeeper.distributewoker.ResultHandler.Result;
import zookeeper.distributewoker.config.PathConfig;
import zookeeper.distributewoker.worker.task.IWorkerTaskFactory;

public class Worker {
	private Animal animal;
	private Map<String,Result> executedBox = new ConcurrentHashMap<String,Result>();
	private ExecutorService executors;
	private ResultHandler resultHandler;
	private IWorkerTaskFactory workerTaskFactory;
	
	public Worker(String host, String workerId, IWorkerTaskFactory workerTaskFactory){
		animal = new Animal(host, workerId);
		resultHandler = new ResultHandler(animal);
		executors = Executors.newFixedThreadPool(3);
		this.workerTaskFactory = workerTaskFactory;
	}
	
	public void doAfterSucess(String taskName,Result result){
		executedBox.put(taskName, resultHandler.renewResultKey(taskName, result.getBeginTime(), animal.key(), Result.SUCCESS_STATE));
	}
	
	public void doAfterFailue(String taskName,Result result){
		executedBox.put(taskName, resultHandler.renewResultKey(taskName, result.getBeginTime(), animal.key(), Result.FAIL_STATE));
	}
	
	public boolean checkTaskIsValid(String task, Result result){
		Result zookData = Result.makeResult(animal.getData(PathConfig.buildResultPath(task)));
		if(zookData.getState().equals(Result.SUCCESS_STATE)){
			return false;
		}
		return executedBox.get(task) != null && executedBox.get(task).equals(result);
	}
	
	public boolean checkTaskShouldHandle(String taskName, Result result){
		if(result.getState().equals(Result.SUCCESS_STATE)){
			return false;
		}
		if(!result.getWorkKey().equals(animal.key())){
			return false;
		}
		Result inBoxResult = executedBox.get(taskName);
		if (inBoxResult != null && inBoxResult.equals(result)) {
			return false;
		}
		return true;
	}

	public void start(){
		animal.createEphemeral(PathConfig.buildWorkPath(animal.key()), animal.key(), false);
		/**
		 * 更改自己的child_data,而不是检查result_path下面的数据,如果是检查result_path数据也就是说很多无关数据要处理。
		 * 更改work相关的数据,更改worker的数据。
		 */
		animal.getClient().subscribeDataChanges(PathConfig.buildWorkPath(animal.key()), new IZkDataListener() {
			@Override
			public void handleDataDeleted(String dataPath) throws Exception {
			}
			
			@Override
			public void handleDataChange(String dataPath, Object data) throws Exception {
				Map<String,Result>	map = resultHandler.getChildrenAndDtas();
				for (Entry<String,Result> entry : map.entrySet()) {
					if(!checkTaskShouldHandle(entry.getKey(), entry.getValue())){
						continue;
					}
					executedBox.put(entry.getKey(), entry.getValue());
					executors.execute(workerTaskFactory.getRunnerTask(entry.getKey(), entry.getValue(), Worker.this));
				}

		}});
	}
	

}
