package nl.loadingdata.example;

import nl.loadingdata.messagebus.CountDown;

public class FBItem {
	public static final int NUMBER = 0;
	public static final int FIZZ = 1;
	public static final int BUZZ = 2;
	public static final int JAZZ = 3;
	public static final int NEWLINE = 4;
	public static final int COUNT = 5;
	
	private String[] slot = new String[COUNT];
	private CountDown barrier;
	private ItemAction action;
	
	public FBItem() {
		barrier = new CountDown(COUNT, () -> {
			if (action != null) action.onComplete(this);
		});
	}
	
	public void whenComplete(ItemAction action) {
		this.action = action;
	}
	
	public void update(int key, boolean cond, String val) {
		if (cond) {
			slot[key] = val;
		}
		barrier.countDown();
	}

	private boolean emitNumber() {
		for (int i = 1; i < slot.length - 1; i++) {
			if (slot[i] != null) return false;
		}
		return true;
	}
	
	@Override
	public String toString() {
		String result = slot[NUMBER] + ": ";
		int start = emitNumber() ? NUMBER : FIZZ;
		for (int i = start; i < slot.length; i++) {
			if (slot[i] != null) result += slot[i];
		}
		return result;
	}

}