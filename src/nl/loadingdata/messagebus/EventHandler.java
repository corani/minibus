package nl.loadingdata.messagebus;

public interface EventHandler<E extends Event> {

	public void onEvent(E event);

}
