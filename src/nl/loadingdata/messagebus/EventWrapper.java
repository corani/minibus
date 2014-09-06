package nl.loadingdata.messagebus;

public class EventWrapper<T extends Event> {
	private T event;
	private EventHandledCallback<T> cb;
	private int subscribers;

	EventWrapper(T event, EventHandledCallback<T> cb) {
		this.event = event;
		this.cb = cb;
	}

	void complete() {
		if (--subscribers == 0 && cb != null) {
			cb.onHandled(event);
		}
	}

	void setSubscribers(int subscribers) {
		this.subscribers = subscribers;
	}

	T getEvent() {
		return event;
	}
}
