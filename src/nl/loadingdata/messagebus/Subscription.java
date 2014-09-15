package nl.loadingdata.messagebus;


public class Subscription<T extends Event> implements Runnable {
	private MessageBus bus;
	private EventQueue pending;
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
		pending = new EventQueue();
		running = true;
		thread = new Thread(this);
		thread.start();
	}

	public void run() {
		while (running) {
			pending.next()
				.ifPresent(event -> {
					eventListener.onEvent(pending.unwrap(event));
					event.complete();
				});
		}
	}

	void shutdown() {
		bus.unsubscribe(this);
		bus = null;

		running = false;
		synchronized (thread) {
			thread.notify();
		}

		pending.shutdown();
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