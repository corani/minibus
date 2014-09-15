package nl.loadingdata.messagebus;

import java.util.LinkedList;
import java.util.Optional;

public class EventQueue extends LinkedList<EventWrapper<? extends Event>>{
	private static final long serialVersionUID = 1176368599120850502L;
	private boolean running = true;

	public synchronized Optional<EventWrapper<? extends Event>> next() {
		EventWrapper<? extends Event> event = null;
		while (running && event == null) {
			event = poll();
			if (event == null) {
				waitForEvent();
			}
		}
		return Optional.ofNullable(event);
	}
	
	public synchronized <E extends Event> boolean add(EventWrapper<E> ew) {
		if (running) {
			super.add(ew);
			notify();
		}
		return running;
	}
	
	public synchronized <E extends Event> boolean add(E event, EventHandledCallback<E> cb) {
		return add(wrap(event, cb));
	}
	
	public synchronized void shutdown() {
		running = false;
		forEach(e -> e.complete());
		clear();
		notify();
	}
	
	public synchronized boolean isIdle() {
		return isEmpty();
	}

	@SuppressWarnings("unchecked")
	public <T> T unwrap(EventWrapper<? extends Event> event) {
		return (T) event.getEvent();
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public <E extends Event> EventWrapper<? extends Event> wrap(E event, EventHandledCallback<E> cb) {
		return new EventWrapper(event, cb);
	}
	
	private synchronized void waitForEvent() {
		try {
			wait();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}			
	}
}
