package zookeeper.distributewoker;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import zookeeper.Animal;
import zookeeper.distributewoker.comm.Utils;
import zookeeper.distributewoker.config.PathConfig;
import zookeeper.distributewoker.config.SymbolConfig;

public class ResultHandler {
	private Animal animal;
	
	public ResultHandler(Animal animal){
		this.animal = animal;
	}
	
	public void createResultKey(String key,long beginTime ,String workerIndex,String state){
		Result result = new Result(state, beginTime, Utils.getNowTimeStramp() , workerIndex);
		this.animal.createPersistent(PathConfig.buildResultPath(key), result.buildKey());
	}
	
	public Result renewResultKey(String key,long beginTime,String workerIndex,String state){
		Result result = new Result(state, beginTime, Utils.getNowTimeStramp() , workerIndex);
		this.animal.setData(PathConfig.buildResultPath(key), result.buildKey());
		return result;
	}
	
	public List<String> getChildren(){
		if(!this.animal.getClient().exists(PathConfig.RESULT_PATH)){
			return Collections.emptyList();
		}
		return this.animal.getClient().getChildren(PathConfig.RESULT_PATH);
	}
	
	public static class Result{
		Map<String,String> resultMap = new HashMap<String,String>();
		
		private static final String WOKER_KEY = "workerKey";
		private static final String BEGIN_TIME = "beginTime";
		private static final String EXCUTED_TIME = "excutedTime";
		private static final String STATE = "state";
		public static final String RUNNING_STATE = "running";
		public static final String FAIL_STATE = "fail";
		public static final String SUCCESS_STATE = "success";
		public static final String RECEIVED_STATE = "received";
		
		public Result(String state, long beginTime, long excuteTime, String workerKey){
			resultMap.put(WOKER_KEY, workerKey);
			resultMap.put(BEGIN_TIME, "" + beginTime);
			resultMap.put(EXCUTED_TIME, "" + excuteTime);
			resultMap.put(STATE, state);
		}
		
		@Override
		public boolean equals(Object object){
			Result r = (Result)object;
			for (Entry<String,String> entry : resultMap.entrySet()) {
				if(!r.getResultMap().get(entry.getKey()).equals(entry.getValue()))
					return false;
			}
			return true;
		}
		
		public String getState(){
			return resultMap.get(STATE);
		}
		
		public String getWorkKey(){
			return resultMap.get(WOKER_KEY);
		}
		
		public Long getBeginTime(){
			return Long.parseLong(resultMap.get(BEGIN_TIME));
		}
		
		public Long getExcutedTime(){
			return Long.parseLong(resultMap.get(EXCUTED_TIME));
		}
		
		public Result(Map<String,String> resultMap){
			this.resultMap = resultMap;
		}
		
		public Map<String,String> getResultMap(){
			return resultMap;
		}
		
		public String buildKey(){
			String totalKey = "";
			for(Entry<String,String> entry: resultMap.entrySet()){
				totalKey += entry.getKey() + SymbolConfig.COMMA + entry.getValue() + SymbolConfig.QUORA;
			}
			return totalKey;
		}
		
		public static Result makeResult(String resultStr){
			Map<String,String> temp = new HashMap<String,String>();
			String[] elements = resultStr.split(SymbolConfig.QUORA);
			for (String element : elements) {
				String[] elementTuple = element.split(SymbolConfig.COMMA);
				temp.put(elementTuple[0], elementTuple[1]);
			}
			return new Result(temp);
		}
	}
	
	public Map<String,Result> getChildrenAndDtas(){
		Map<String,Result> resultMap = new HashMap<String,Result>();
		for (String child : this.getChildren()) {
			Result result = Result.makeResult(this.animal.getData(PathConfig.buildResultPath(child)));
			resultMap.put(child,result);
		}
		return resultMap;
	}
	

}
