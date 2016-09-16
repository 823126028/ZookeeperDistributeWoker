package zookeeper.nameservice.test;

import java.util.concurrent.CountDownLatch;

import zookeeper.Animal;

public class NameServiceWatcherTest {
	public static void main(String[] args) throws InterruptedException{
		CountDownLatch c = new CountDownLatch(1);
		Animal animal = new Animal("127.0.0.1:2181", "193.113.1.2");
		animal.createEphemeral("/workPath/1", "193.113.1.1", false);
		c.await();
	}
}
