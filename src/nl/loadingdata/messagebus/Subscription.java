package nl.loadingdata.messagebus;

import java.util.LinkedList;
import java.util.Optional;
import java.util.Queue;

public class Subscription<T extends Event> implements Runnable {
	private MessageBus bus;
	private Queue<EventWrapper<T>> pending;
	private EventHandler<T> eventListener;
	private Thread thread;
	private boolean running;
	private Class<T> clazz;
	private EventFilter<T> filter;

	Subscription(MessageBus bus, EventFilter<T> filter, Class<T> clazz, EventHandler<T> listener) {
		this.bus = bus;
		this.filter = filter;
		this.clazz = clazz;
		this.eventListener = listener;
		pending = new LinkedList<>();
		running = true;
		thread = new Thread(this);
		thread.start();
	}

	public void run() {
		while (running) {
			nextEvent().ifPresent(event -> {
				eventListener.onEvent(event.getEvent());
				event.complete();
			});
		}
	}

	private Optional<EventWrapper<T>> nextEvent() {
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
		return Optional.ofNullable(event);
	}

	void shutdown() {
		bus.unsubscribe(this);
		bus = null;

		running = false;
		synchronized (thread) {
			thread.notify();
		}

		synchronized (pending) {
			pending.forEach(e -> e.complete());
			pending.clear();
			pending.notify();
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