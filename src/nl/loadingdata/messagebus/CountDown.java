package nl.loadingdata.messagebus;

public class CountDown {
	private int count;
	private Runnable action;

	public CountDown(int count, Runnable action) {
		this.count = count;
		this.action = action;
	}

	public void countDown() {
		if (--count == 0) {
			action.run();
		}
	}

}
