package zookeeper.distributewoker.config;

public class PathConfig {
	public static final String WORK_PATH = "/workPath"; 
	public static final String RESULT_PATH = "/resultPath";
	public static final String NODE_SPLITOR = "/";
	
	public static String buildWorkPath(String key){
		return WORK_PATH + NODE_SPLITOR + key;
	}
	
	public static String buildResultPath(String key){
		return RESULT_PATH + NODE_SPLITOR + key;
	}
}
