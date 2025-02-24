package bgu.spl.mics.application.services;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.objects.StatisticalFolder;
import bgu.spl.mics.application.messages.TerminatedBroadcast;
import bgu.spl.mics.Callback;
import bgu.spl.mics.application.messages.CrashedBroadcast;

/**
 * TimeService acts as the global timer for the system, broadcasting
 * TickBroadcast messages
 * at regular intervals and controlling the simulation's duration.
 */
public class TimeService extends MicroService {

  private final int tickTime;
  private final int duration; // Number of ticks
  private int currentTick;

  /**
   * Constructor for TimeService.
   *
   * @param TickTime The duration of each tick in milliseconds.
   * @param Duration The total number of ticks before the service terminates.
   */
  public TimeService(int TickTime, int Duration) {
    super("TimeService");
    this.tickTime = TickTime;
    this.duration = Duration;
    this.currentTick = 1;
  }

  /**
   * Initializes the TimeService.
   * Starts broadcasting TickBroadcast messages and terminates after the specified
   * duration.
   */
  protected void initialize() {
    subscribeBroadcast(TickBroadcast.class, new Callback<TickBroadcast>() {
      @Override
      public void call(TickBroadcast tickBroadcast) {
        try {
          Thread.sleep(tickTime );
        } catch (InterruptedException e) {
        }
        currentTick++;
        if (currentTick > duration + 1) {
          sendBroadcast(new TerminatedBroadcast("TimeService"));
          terminate();
        } else {
          sendBroadcast(new TickBroadcast(currentTick));
          StatisticalFolder.getInstance().incrementSystemRuntime();
        }
      }
    });

    subscribeBroadcast(TerminatedBroadcast.class, new Callback<TerminatedBroadcast>() {
      @Override
      public void call(TerminatedBroadcast terminatedBroadcast) {
        if (terminatedBroadcast.getSender().equals("FusionSlamService")) {
            terminate();
        }
      }
    });

    subscribeBroadcast(CrashedBroadcast.class, new Callback<CrashedBroadcast>() {
      @Override
      public void call(CrashedBroadcast c) {
        terminate();
      }
    });

    // Manually sending first tickBraodcast
    try {
      Thread.sleep(tickTime);
    } catch (InterruptedException e) {
    }
    sendBroadcast(new TickBroadcast(currentTick));

  }
}
