package nl.loadingdata.messagebus;


public interface EventFilter<E extends Event> {

	public boolean test(E event);

}
