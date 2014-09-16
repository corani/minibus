package nl.loadingdata.messagebus;

import java.util.Iterator;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;

class EventQueue {
	private Queue<EventWrapper<? extends Event>> queue = new ConcurrentLinkedQueue<>();
	private boolean running = true;

	private class EQIterator<T> implements Iterator<Optional<T>> {
		@Override
		public Optional<T> next() {
			T event = null;
			while (running && event == null) {
				event = cast(queue.poll());
				if (event == null) {
					waitForEvent();
				}
			}
			return Optional.ofNullable(event);
		}
		
		@SuppressWarnings("unchecked")
		private T cast(EventWrapper<? extends Event> event) {
			return (T) event;
		}
		
		@Override
		public boolean hasNext() {
			return running;
		}
	}
	
	public <E extends Event> boolean add(E event, EventHandledCallback<E> cb) {
		return add(wrap(event, cb));
	}

	public <E extends Event> boolean add(EventWrapper<E> wrapper) {
		if (running) {
			queue.add(wrapper);
			synchronized (this) {
				notify();
			}
		}
		return running;
	}
	
	public void shutdown() {
		running = false;
		queue.forEach(e -> e.cancel());
		queue.clear();
		synchronized (this) {
			notify();
		}
	}
	
	public boolean isIdle() {
		return queue.isEmpty() && running;
	}

	@SuppressWarnings("unchecked")
	public <T extends Event> T unwrap(EventWrapper<? extends Event> event) {
		return (T) event.getEvent();
	}

	private <E extends Event> EventWrapper<E> wrap(E event, EventHandledCallback<E> cb) {
		return new EventWrapper<E>(event, cb);
	}
	
	private synchronized void waitForEvent() {
		try {
			wait();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}			
	}
	
	private Iterable<Optional<EventWrapper<? extends Event>>> iterable() {
		return () -> new EQIterator<>();
	}

	public void forEach(Consumer<EventWrapper<? extends Event>> eventAction) {
		iterable().forEach(optional ->
			optional.ifPresent(eventAction)
		);
	}
}
