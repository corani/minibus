package nl.loadingdata.example;

import nl.loadingdata.messagebus.Event;

public class PrintEvent implements Event {
	String string;

	public PrintEvent(String string) {
		this.string = string;
	}
}