package nl.loadingdata.example;

import nl.loadingdata.messagebus.Event;

public class CompleteEvent implements Event {
	FBItem item;

	public CompleteEvent(FBItem item) {
		this.item = item;
	}
	
	public PrintEvent toPrintEvent() {
		return new PrintEvent(item.toString());
	}
}