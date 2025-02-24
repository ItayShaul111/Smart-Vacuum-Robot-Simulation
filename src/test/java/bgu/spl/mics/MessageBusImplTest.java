package bgu.spl.mics;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


import static org.junit.jupiter.api.Assertions.*;

class MessageBusImplTest {

    private MessageBusImpl messageBus;
    private MicroService microService1;
    private MicroService microService2;

    @BeforeEach
    void setUp() {
        // Pre-condition: The MessageBus instance must be initialized,
        // and MicroService instances must be registered to ensure they can participate in message passing.
        messageBus = MessageBusImpl.getInstance();
        microService1 = new MicroService("TestService1") {
            @Override
            protected void initialize() {
            }
        };
        microService2 = new MicroService("TestService2") {
            @Override
            protected void initialize() {
            }
        };
        messageBus.register(microService1);
        messageBus.register(microService2);
    }

    @Test
    void testSubscribeEvent() {
        // Pre-condition: MicroService1 must be subscribed to TestEvent.
        class TestEvent implements Event<String> {}
        messageBus.subscribeEvent(TestEvent.class, microService1);

        // Act: Send an event of type TestEvent.
        Future<String> future = messageBus.sendEvent(new TestEvent());

        // Post-condition: A non-null Future object should be returned, indicating that the event was handled successfully.
        assertNotNull(future, "Future should not be null since there is a subscriber.");
    }


    @Test
    void testComplete() {
        // Pre-condition: MicroService1 must be subscribed to TestEvent.
        class TestEvent implements Event<String> {}
        messageBus.subscribeEvent(TestEvent.class, microService1);

        // Act: Send an event, then complete it with a result.
        TestEvent event = new TestEvent();
        Future<String> future = messageBus.sendEvent(event);
        assertNotNull(future, "Future should not be null since there is a subscriber.");

        messageBus.complete(event, "Completed");

        // Post-condition: The Future object must indicate completion and hold the result "Completed."
        assertTrue(future.isDone(), "Future should be marked as done.");
        assertEquals("Completed", future.get(), "Future result should match the completed value.");
    }

    @Test
    void testSendEvent() {
        // Pre-condition: MicroService1 must be subscribed to TestEvent.
        class TestEvent implements Event<String> {}
        messageBus.subscribeEvent(TestEvent.class, microService1);

        // Act: Send an event of type TestEvent.
        Future<String> future = messageBus.sendEvent(new TestEvent());

        // Post-condition: A non-null Future object should be returned, indicating that the event was handled successfully.
        assertNotNull(future, "Future should not be null since there is a subscriber.");
    }

    @Test
    void testRegisterAndUnregister() {
        // Pre-condition: MicroService must be registered and subscribed to TestBroadcast.
        class TestBroadcast implements Broadcast {}
        MicroService testMicroService = new MicroService("TestService") {
            @Override
            protected void initialize() {
            }
        };

        messageBus.register(testMicroService);
        messageBus.subscribeBroadcast(TestBroadcast.class, testMicroService);

        // Act: Unregister the MicroService.
        messageBus.unregister(testMicroService);

        // Post-condition: The unregistered MicroService should not be able to receive messages.
        assertThrows(IllegalStateException.class, () -> messageBus.awaitMessage(testMicroService),
                "MicroService should not receive messages after being unregistered.");
    }

    @Test
    void testAwaitMessage() {
        // Pre-condition: MicroService1 must be subscribed to TestBroadcast.
        class TestBroadcast implements Broadcast {}
        messageBus.subscribeBroadcast(TestBroadcast.class, microService1);

        // Act: Send a broadcast message.
        messageBus.sendBroadcast(new TestBroadcast());

        // Post-condition: MicroService1 should receive the broadcast message via awaitMessage.
        try {
            Message message = messageBus.awaitMessage(microService1);
            assertNotNull(message, "Message should not be null.");
            assertTrue(message instanceof TestBroadcast, "Message should be of type TestBroadcast.");
        } catch (InterruptedException e) {
            fail("Interrupted while awaiting message.");
        }
    }

    @Test
    void testAwaitMessageForUnregisteredMicroService() {
        // Pre-condition: The MicroService must not be registered.
        MicroService unregisteredMicroService = new MicroService("UnregisteredService") {
            @Override
            protected void initialize() {
            }
        };

        // Post-condition: Calling awaitMessage on an unregistered MicroService should throw an IllegalStateException.
        assertThrows(IllegalStateException.class, () -> messageBus.awaitMessage(unregisteredMicroService),
                "Should throw IllegalStateException for unregistered microservice.");
    }
}