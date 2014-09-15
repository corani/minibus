package nl.loadingdata.messagebus;
import java.util.List;


public class MessageBus implements Runnable {
	private Thread thread;
	private boolean requestStop = false;
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
		if (!requestStop) {
			subscriptions.remove(sub);
		}
	}

	public <T extends Event> void publish(T event, EventHandledCallback<T> cb) {
		if (requestStop) {
			throw new IllegalStateException("MessageBus shutting down");
		}
		synchronized (events) {
			events.add(event, cb);
		}
	}

	public <T extends Event> void publish(T event) {
		publish(event, null);
	}

	public boolean isIdle() {
		if (!events.isEmpty()) return false;
		return subscriptions.isIdle();
	}

	public boolean isRunning() {
		return (thread != null) && !requestStop;
	}

	public synchronized void start() {
		if (requestStop) {
			throw new IllegalStateException("MessageBus shutting down");
		}
		if (thread == null) {
			thread = new Thread(this);
			thread.start();
		}
	}

	public synchronized void stop() {
		if (thread != null) {
			requestStop = true;
			events.shutdown();
		}
	}

	@Override
	public void run() {
		while (!requestStop) {
			events.next()
				.ifPresent(event -> dispatch(event));
		}
		subscriptions.shutdown();
		events.shutdown();
		thread = null;
		requestStop = false;
	}

	private <T extends Event> void dispatch(EventWrapper<T> event) {
		List<Subscription<T>> matching = subscriptions.allMatching(event);
		event.setSubscribers(matching.size());
		matching.forEach(sub -> sub.dispatch(event));
	}

}