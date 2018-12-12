package ops.dao;

/*
 * 栏杆，只有两个操作。
 * 可以在操作完成后返回。每个操作都延迟2s就可达到模拟效果。
 * 需要注意一点 放下栏杆的操作要在车离开以后进行，所以要和Sensor配合
 */
public interface Rail_Operations {
	void raise();
	void down();
}
