package nl.loadingdata.messagebus;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;


public class MessageBus implements Runnable {
	private Thread thread;
	private boolean requestStop = false;
	private Queue<EventWrapper<? extends Event>> events = new LinkedList<>();
	private List<Subscription<? extends Event>> subscriptions = new ArrayList<>(); 

	public <T extends Event> Subscription<T> subscribe(Class<T> clazz, EventFilter<T> filter, EventHandler<T> listener) {
		Subscription<T> sub = new Subscription<T>(this, filter, clazz, listener);
		synchronized (subscriptions) {
			subscriptions.add(sub);
		}
		return sub;
	}

	public <T extends Event> Subscription<T> subscribe(Class<T> clazz, EventHandler<T> listener) {
		return subscribe(clazz, null, listener);
	}

	public void unsubscribe(Subscription<? extends Event> sub) {
		synchronized (subscriptions) {
			subscriptions.remove(sub);
		}
	}
	
	public <T extends Event> void publish(T event, EventHandledCallback<T> cb) {
		synchronized (events) {
			events.add(wrap(event, cb));
			if (thread != null) {
				synchronized (thread) {
					thread.notify();
				}
			}
		}
	}

	public <T extends Event> void publish(T event) {
		publish(event, null);
	}
	
	public boolean isIdle() {
		synchronized (events) {
			if (!events.isEmpty()) return false;
		}
		synchronized (subscriptions) {
			for (Subscription<? extends Event> sub : subscriptions) {
				if (!sub.isIdle()) return false;
			}
		}
		return true;
	}
	
	public synchronized void start() {
		if (thread == null) {
			thread = new Thread(this);
			thread.start();
		}
	}
	
	public synchronized void stop() {
		if (thread != null) {
			requestStop = true;
			synchronized (thread) {
				thread.notify();
			}
		}
	}

	@Override
	public void run() {
		while (!requestStop) {
			EventWrapper<?> event = null;
			synchronized (events) {
				event = events.poll();
			}
			if (event != null) {
				dispatch(event);
			} else {
				synchronized (thread) {
					try {
						thread.wait();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}
		synchronized (subscriptions) {
			subscriptions.forEach(sub -> sub.shutdown());
			subscriptions.clear();
		}
		thread = null;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private <T extends Event> EventWrapper<?> wrap(T event, EventHandledCallback<T> cb) {
		return new EventWrapper(event, cb);
	}
	
	@SuppressWarnings("unchecked")
	private <T extends Event> Subscription<T> cast(Subscription<?> sub) {
		return (Subscription<T>) sub;
	}
	
	private <T extends Event> void dispatch(EventWrapper<T> event) {
		List<Subscription<T>> matching = new ArrayList<>();
		synchronized (subscriptions) {
			subscriptions.forEach(sub -> {
				if (sub.wants(event.getEvent())) {
					matching.add(cast(sub));
				}
			});
		}
		event.setSubscribers(matching.size());
		matching.forEach(sub -> sub.dispatch(event));
	}
	
}
