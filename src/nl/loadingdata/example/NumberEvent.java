package nl.loadingdata.example;

import nl.loadingdata.messagebus.Event;

public class NumberEvent implements Event {
	int number;
	FBItem item;

	public NumberEvent(int number, FBItem item) {
		this.number = number;
		this.item = item;
	}
}