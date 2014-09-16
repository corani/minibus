package nl.loadingdata.messagebus;



class Subscription<T extends Event> {
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
		thread = new Thread(() -> {
			// This call loops until the event queue is shut down
			pending.forEach(event -> {
				eventListener.onEvent(pending.unwrap(event));
				event.complete();
			});
		});
		thread.start();
	}

	public void shutdown() {
		bus.unsubscribe(this);
		bus = null;
		pending.shutdown();
	}

	public boolean isIdle() {
		return pending.isIdle();
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

	@SuppressWarnings("unchecked")
	private T cast(Event event) {
		return (T) event;
	}
}