package nl.loadingdata.messagebus;

import java.util.LinkedList;
import java.util.Queue;

public class Subscription<T extends Event> {
	private MessageBus bus;
	private Queue<EventWrapper<T>> pending;
	private EventListener<T> eventListener;
	private Thread thread;
	private boolean running;
	private Class<T> clazz;
	private EventFilter<T> filter;

	Subscription(MessageBus bus, EventFilter<T> filter, Class<T> clazz, EventListener<T> listener) {
		this.bus = bus;
		this.filter = filter;
		this.clazz = clazz;
		this.eventListener = listener;
		pending = new LinkedList<>();
		running = true;
		thread = new Thread(() -> {
			while (running) {
				EventWrapper<T> event = getEvent();
				if (event != null) {
					eventListener.onEvent(event.event);
					event.complete();
				}
			}
		});
		thread.start();
	}

	public void cancel() {
		bus.unsubscribe(this);
		bus = null;
	}
	
	public boolean hasEventWaiting() {
		synchronized (pending) {
			return !pending.isEmpty();
		}
	}
	
	public EventWrapper<T> getEvent() {
		EventWrapper<T> event = null;
		while (bus != null && event == null) {
			synchronized (pending) {
				event = pending.poll();
				if (event == null) {
					try {
						pending.wait();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}
		return event;
	}
	
	void shutdown() {
		bus = null;
		running = false;
		synchronized (pending) {
			pending.notify();
		}
		synchronized (thread) {
			thread.notify();
		}
	}
	
	void dispatch(EventWrapper<T> event) {
		synchronized (pending) {
			pending.add(event);
			pending.notify();
		}
	}
	
	boolean wants(Event event) {
		boolean result = event.getClass().isAssignableFrom(clazz);
		if (result && filter != null) {
			result = filter.test(cast(event));
		}
		return result;
	}
	
	boolean isIdle() {
		synchronized (pending) {
			return pending.isEmpty();
		}
	}

	@SuppressWarnings("unchecked")
	private T cast(Event event) {
		return (T) event;
	}

}
