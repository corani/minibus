package nl.loadingdata.messagebus;

import java.util.Optional;


class Subscription<T extends Event> implements Runnable {
	private MessageBus bus;
	private EventQueue pending;
	private EventHandler<T> eventListener;
	private Thread thread;
	private Class<T> clazz;
	private EventFilter<T> filter;

	Subscription(MessageBus bus, EventFilter<T> filter, Class<T> clazz, EventHandler<T> listener) {
		this.bus = bus;
		this.filter = filter;
		this.clazz = clazz;
		this.eventListener = listener;
		pending = new EventQueue();
		thread = new Thread(this);
		thread.start();
	}

	public void run() {
		for (Optional<EventWrapper<? extends Event>> optional : pending.iterable()) {
			optional.ifPresent(event -> {
				eventListener.onEvent(pending.unwrap(event));
				event.complete();
			});
		}
	}

	void shutdown() {
		bus.unsubscribe(this);
		bus = null;

		pending.shutdown();
		synchronized (thread) {
			thread.notify();
		}
	}

	void dispatch(EventWrapper<T> event) {
		pending.add(event);
	}

	boolean wants(Event event) {
		boolean result = event.getClass().isAssignableFrom(clazz);
		if (result && filter != null) {
			result = filter.test(cast(event));
		}
		return result;
	}

	boolean isIdle() {
		return pending.isIdle();
	}

	@SuppressWarnings("unchecked")
	private T cast(Event event) {
		return (T) event;
	}
}