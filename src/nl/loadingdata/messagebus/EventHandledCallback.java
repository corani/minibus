package nl.loadingdata.messagebus;

public interface EventHandledCallback<T extends Event> {

	public void onHandled(T event);
	public default void onCancelled(T event) {}
	
}
