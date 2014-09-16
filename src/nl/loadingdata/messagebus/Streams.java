package nl.loadingdata.messagebus;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collector;

public class Streams {
	public static <T> Collector<T, List<T>, List<T>> asList() {
		return Collector.of(
			()       -> new ArrayList<>(),
			(l, e)   -> l.add(e),
			(l1, l2) -> {
				l1.addAll(l2);
				return l1;
			}
		);
	}
}
