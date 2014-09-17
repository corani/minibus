package nl.loadingdata.example;

import static java.util.stream.IntStream.range;

public class FBItem {
	public static final int NUMBER = 0;
	public static final int FIZZ = 1;
	public static final int BUZZ = 2;
	public static final int JAZZ = 3;
	public static final int NEWLINE = 4;
	public static final int COUNT = 5;
	
	private String[] slot = new String[COUNT];
	
	public void update(int key, boolean cond, String val) {
		slot[key] = cond ? val : null;
	}

	private int getStart() {
		boolean emit = range(FIZZ, NEWLINE)
			.mapToObj(i -> slot[i])
			.allMatch(item -> item == null);
		return emit ? NUMBER : FIZZ;
	}
	
	@Override
	public String toString() {
		return range(getStart(), COUNT)
			.mapToObj(i -> slot[i])
			.filter(item -> item != null)
			.reduce(slot[NUMBER] + ": ",
					(s1, s2) -> s1 + s2);
	}

}