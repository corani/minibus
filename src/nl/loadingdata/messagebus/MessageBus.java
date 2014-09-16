package nl.loadingdata.messagebus;


public class MessageBus {
	private boolean running = false;
	private EventQueue events = new EventQueue();
	private SubscriptionList subscriptions = new SubscriptionList();

	public <T extends Event> Subscription<T> subscribe(Class<T> clazz, EventFilter<T> filter, EventHandler<T> listener) {
		Subscription<T> sub = new Subscription<T>(this, filter, clazz, listener);
		subscriptions.add(sub);
		return sub;
	}

	public <T extends Event> Subscription<T> subscribe(Class<T> clazz, EventHandler<T> listener) {
		return subscribe(clazz, null, listener);
	}

	public void unsubscribe(Subscription<? extends Event> sub) {
		if (running) {
			subscriptions.remove(sub);
		}
	}

	public <T extends Event> void publish(T event, EventHandledCallback<T> cb) {
		events.add(event, cb);
	}

	public <T extends Event> void publish(T event) {
		publish(event, null);
	}

	public boolean isIdle() {
		if (!events.isIdle()) return false;
		return subscriptions.isIdle();
	}

	public boolean isRunning() {
		return !running;
	}

	public void start() {
		if (!running) {
			new Thread(() -> {
				// This call loops until the EventQueue is shut down
				events.forEach(wrapper -> dispatch(wrapper));
			}).start();
			running = true;
		}
	}

	public void stop() {
		if (running) {
			running = false;
			events.shutdown();
			subscriptions.shutdown();
		}
	}

	private <T extends Event> void dispatch(EventWrapper<T> wrapper) {
		subscriptions.allMatching(wrapper.getEvent(), matching -> {
			wrapper.setSubscribers(matching.size());
			matching.forEach(sub -> sub.dispatch(wrapper));
		});
	}

}