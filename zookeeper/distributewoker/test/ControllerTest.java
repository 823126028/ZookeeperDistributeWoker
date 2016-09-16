package zookeeper.distributewoker.test;

import java.util.ArrayList;
import java.util.List;

import zookeeper.distributewoker.Controller;

import zookeeper.distributewoker.handler.TimedRunner;
import zookeeper.distributewoker.handler.comm.TaskStructure;
import zookeeper.distributewoker.handler.workchooser.RandomStrategyChooser;

public class ControllerTest {
	public static void main(String[] args) throws Exception{
		List<TaskStructure> list = new ArrayList<TaskStructure>();
		for(int i = 0; i < 10; i++){
			list.add(new TaskStructure("firstBlood_"+ i,i));
		}
		Controller c = new Controller("127.0.0.1:2181", "/master", "/test3", new TimedRunner(list, new RandomStrategyChooser()));
		c.doClearAllResult();
		c.start();
		Thread.sleep(1000000000l);
	}
}
