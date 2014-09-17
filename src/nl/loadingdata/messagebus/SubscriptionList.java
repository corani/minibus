package nl.loadingdata.messagebus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

class SubscriptionList {
	private List<Subscription<? extends Event>> list = Collections.synchronizedList(new ArrayList<>());

	public <T extends Event> Subscription<T> add(Subscription<T> sub) {
		list.add(sub);
		return sub;
	}
	
	public void remove(Subscription<? extends Event> sub) {
		list.remove(sub);
	}

	public boolean isIdle() {
		return list.parallelStream()
				.allMatch(s -> s.isIdle());
	}

	public void shutdown() {
		list.forEach(sub -> sub.shutdown());
		list.clear();
	}
	
	@SuppressWarnings("unchecked")
	public synchronized <T extends Event> void allMatching(T event, Consumer<List<Subscription<T>>> action) {
		List<Subscription<T>> matching = list.parallelStream()
				.filter (s -> s.wants(event))
				.map(s -> (Subscription<T>) s)
				.collect(Streams.asList());
		action.accept(matching);
	}

}
