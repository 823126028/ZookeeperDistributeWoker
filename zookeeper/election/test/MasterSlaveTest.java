package zookeeper.election.test;

import java.util.concurrent.CountDownLatch;

import zookeeper.election.MasterSlaveSwitch;
import zookeeper.log.LogUtil;

public class MasterSlaveTest {
	public static void main(String[] args){
		CountDownLatch latch = new CountDownLatch(1);
		Runnable doAfterOwn = new Runnable() {
			@Override
			public void run() {
				LogUtil.info("u have owned the condition");
			}
		};
		Runnable doAfterRelease = new Runnable(){
			@Override
			public void run() {
				LogUtil.info("u have release the condition");
			}
		};
		MasterSlaveSwitch mss = new MasterSlaveSwitch("127.0.0.1:2181", "/app/Server1", "127.0.0.1:8080",doAfterOwn, doAfterRelease);
		mss.start();
		try {
			latch.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
