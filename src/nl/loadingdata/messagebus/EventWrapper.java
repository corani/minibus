package nl.loadingdata.messagebus;

public class EventWrapper<T extends Event> {
	T event;
	EventHandledCallback<T> cb;
	int subscribers;

	public EventWrapper(T event, EventHandledCallback<T> cb) {
		this.event = event;
		this.cb = cb;
	}

	public void complete() {
		if (--subscribers == 0 && cb != null) {
			cb.onHandled(event);
		}
	}
}
