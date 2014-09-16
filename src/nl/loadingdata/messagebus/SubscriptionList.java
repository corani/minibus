package nl.loadingdata.messagebus;

import java.util.ArrayList;
import java.util.List;

public class SubscriptionList extends ArrayList<Subscription<? extends Event>> {
	private static final long serialVersionUID = -696666819507098269L;

	public synchronized <T extends Event> boolean add(Subscription<T> sub) {
		return super.add(sub);
	}
	
	public synchronized void remove(Subscription<? extends Event> sub) {
		super.remove(sub);
	}

	public synchronized boolean isIdle() {
		return parallelStream()
					.allMatch(s -> s.isIdle());
	}

	public synchronized void shutdown() {
		forEach(sub -> sub.shutdown());
		clear();
	}
	
	@SuppressWarnings("unchecked")
	public synchronized <T extends Event> List<Subscription<T>> allMatching(T event) {
		return parallelStream()
			.filter (s -> s.wants(event))
			.map(s -> (Subscription<T>) s)
			.collect(Streams.asList());
	}

}
