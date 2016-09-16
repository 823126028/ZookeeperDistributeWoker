package zookeeper.distributewoker.test;

import java.util.concurrent.CountDownLatch;

import zookeeper.distributewoker.worker.Worker;
import zookeeper.distributewoker.worker.task.RandomSayHelloFactory;

public class WorkerTest {
	public static void main(String[] args) throws InterruptedException{
		CountDownLatch cdl = new CountDownLatch(1);
		Worker worker = new Worker("127.0.0.1:2181", "2", new RandomSayHelloFactory());
		worker.start();
		cdl.await();
	}
}
