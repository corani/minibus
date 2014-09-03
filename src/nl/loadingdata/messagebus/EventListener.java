package nl.loadingdata.messagebus;

public interface EventListener<E extends Event> {

	public void onEvent(E event);

}
