package bgu.spl.mics;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * The {@link MessageBusImpl class is the implementation of the MessageBus
 * interface.
 * Write your implementation here!
 * Only one public method (in addition to getters which can be public solely for
 * unit testing) may be added to this class
 * All other methods and members you add the class must be private.
 */
public class MessageBusImpl implements MessageBus {

	private final Map<Class<? extends Event<?>>, ConcurrentLinkedQueue<MicroService>> eventSubscriptions;
	private final Map<Class<? extends Broadcast>, ConcurrentLinkedQueue<MicroService>> broadcastSubscriptions;
	private final Map<MicroService, BlockingQueue<Message>> microserviceQueues;
	private final Map<Event<?>, Future<?>> eventFutures;

	// *********************************************** Singleton
	// ***************************************************
	private static class SingletonHolder {
		private static MessageBusImpl instance = new MessageBusImpl();
	}

	public static MessageBusImpl getInstance() {
		return SingletonHolder.instance;
	}

	// *********************************************** Constructor
	// **************************************************
	private MessageBusImpl() {
		this.eventSubscriptions = new ConcurrentHashMap<>();
		this.broadcastSubscriptions = new ConcurrentHashMap<>();
		this.microserviceQueues = new ConcurrentHashMap<>();
		this.eventFutures = new ConcurrentHashMap<>();
	}

	// *********************************************** Methods
	// ******************************************************
	@Override
	public <T> void subscribeEvent(Class<? extends Event<T>> type, MicroService m) {
		eventSubscriptions.putIfAbsent(type, new ConcurrentLinkedQueue<>());
		eventSubscriptions.get(type).add(m);
	}

	///////////////////// MAYBE ADD REGISTER IN ONE OF THOSE METHODS
	@Override
	public void subscribeBroadcast(Class<? extends Broadcast> type, MicroService m) {
		broadcastSubscriptions.putIfAbsent(type, new ConcurrentLinkedQueue<>());
		broadcastSubscriptions.get(type).add(m);
	}

	@Override
	public <T> void complete(Event<T> e, T result) {
		@SuppressWarnings("unchecked")
		Future<T> future = (Future<T>) eventFutures.remove(e); // maybe not remove
		future.resolve(result);
	}

	@Override
	public void sendBroadcast(Broadcast b) {
		ConcurrentLinkedQueue<MicroService> broadSubs = broadcastSubscriptions.get(b.getClass());
		if (broadSubs == null || broadSubs.isEmpty()) {
			return;
		}
		synchronized(broadSubs){
			for (MicroService m : broadSubs) {
				microserviceQueues.get(m).add(b);
			}
		}
	}

	@Override
	public <T> Future<T> sendEvent(Event<T> e) {
		ConcurrentLinkedQueue<MicroService> subscribers = eventSubscriptions.get(e.getClass());

		synchronized (e.getClass()) {

			// Checks if there are no subscribers for that type of event
			if (subscribers == null || subscribers.isEmpty()) {
				return null;
			}

			Future<T> future = new Future<>();
			MicroService selectedMicroService;
			eventFutures.put(e, future);

			// Round-Robin selection of subscribers
			selectedMicroService = subscribers.remove();
			subscribers.add(selectedMicroService);
			microserviceQueues.get(selectedMicroService).add(e); // Adds the event to the selected microservice queue

			return future;
		}
	}

	@Override
	public void register(MicroService m) {
		microserviceQueues.putIfAbsent(m, new LinkedBlockingQueue<Message>());

	}

	@Override
	public void unregister(MicroService m) {
		synchronized(eventSubscriptions){
			eventSubscriptions.values().forEach(queue -> queue.remove(m));
		}
		synchronized(broadcastSubscriptions){
			broadcastSubscriptions.values().forEach(queue -> queue.remove(m));
		}
		synchronized(microserviceQueues){
			microserviceQueues.remove(m);
		}
	}

	@Override
	public Message awaitMessage(MicroService m) throws InterruptedException {
		BlockingQueue<Message> queue = microserviceQueues.get(m);
		if (queue == null) {
			throw new IllegalStateException("MicroService is not registered.");
		}
		return queue.take();
	}
}
