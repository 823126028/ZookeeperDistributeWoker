package zookeeper.distributewoker.comm;

public class Tuple<T,S>{
	public T left;
	public S right;
	public Tuple(){};
	public Tuple(T left, S right){
		this.left = left;
		this.right = right;
	}
}
