package nl.loadingdata.example;

import static java.util.stream.IntStream.range;

import nl.loadingdata.messagebus.MessageBus;


public class FizzBuzz {

	public static void main(String[] args) {
		final MessageBus bus = new MessageBus();
		bus.start();

		// NUMBER
		bus.subscribe(NumberEvent.class,
			e -> e.item.update(FBItem.NUMBER, true, "" + e.number)
		);
		// FIZZ
		bus.subscribe(NumberEvent.class,
			e -> e.item.update(FBItem.FIZZ, (e.number % 3) == 0, "Fizz")
		);
		// BUZZ
		bus.subscribe(NumberEvent.class,
			e -> e.item.update(FBItem.BUZZ, (e.number % 5) == 0, "Buzz")
		);
		// JAZZ
		bus.subscribe(NumberEvent.class,
			e -> e.item.update(FBItem.JAZZ, (e.number % 7) == 0, "Jazz")
		);
		// NEWLINE
		bus.subscribe(NumberEvent.class,
			e -> e.item.update(FBItem.NEWLINE, true, "\n")
		);
		// CONVERT TO STRING
		bus.subscribe(CompleteEvent.class,
			e -> bus.publish(e.toPrintEvent())
		);
		// PRINT
		bus.subscribe(PrintEvent.class,
			System.out::print
		);

		// SCHEDULE
		range(0, 105).forEach(i -> {
			NumberEvent numberEvent = new NumberEvent(i + 1, new FBItem());
			bus.publish(numberEvent,
				e -> bus.publish(new CompleteEvent(e.item))
			);
		});

		// WAIT
		while (!bus.isIdle()) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		bus.stop();
	}
}