package bgu.spl.mics.application.services;

import bgu.spl.mics.application.objects.GPSIMU;

import bgu.spl.mics.Callback;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.CrashedBroadcast;
import bgu.spl.mics.application.messages.PoseEvent;
import bgu.spl.mics.application.messages.TerminatedBroadcast;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.objects.STATUS;
import bgu.spl.mics.application.objects.StatisticalFolder;


/**
 * PoseService is responsible for maintaining the robot's current pose (position and orientation)
 * and broadcasting PoseEvents at every tick.
 */
public class PoseService extends MicroService {

    private final GPSIMU gpsimu;

    /**
     * Constructor for PoseService.
     *
     * @param gpsimu The GPSIMU object that provides the robot's pose data.
     */
    public PoseService(GPSIMU gpsimu) {
        super("PoseService");
        this.gpsimu = gpsimu;
    }

    /**
     * Initializes the PoseService.
     * Subscribes to TickBroadcast and sends PoseEvents at every tick based on the current pose.
     */
    @Override
    protected void initialize() {
        subscribeBroadcast(TickBroadcast.class, new Callback<TickBroadcast>() {
            @Override
            public void call(TickBroadcast tickBroadcast) {
                if (tickBroadcast.getTick() > gpsimu.getJsonPoseData().size()) {
                     sendBroadcast(new TerminatedBroadcast("PoseService"));
                     gpsimu.setStatus(STATUS.DOWN);
                     terminate();
                     return;
                }

                PoseEvent poseEvent = new PoseEvent(gpsimu.getJsonPoseData().get(gpsimu.getCurrentTick()));

                // Add the pose to the statistical folder   
                StatisticalFolder.getInstance().addPose(poseEvent.getPose());

                gpsimu.setCurrentTick(tickBroadcast.getTick());

                /*Future<Boolean> future = */sendEvent(poseEvent);
                // if (future.get(3000, TimeUnit.MILLISECONDS) == null) { // TO BE CHANGED WHEN IMPLEMENTING STATISTICAL FOLDER                                     
                //     gpsimu.setStatus(STATUS.ERROR);
                //     terminate();
                // }
                
            }
            
        });

        // MIGHT BE DELETED
        subscribeBroadcast(TerminatedBroadcast.class, new Callback<TerminatedBroadcast>() {
            @Override
            public void call(TerminatedBroadcast terminatedBroadcast) {
                if (terminatedBroadcast.getSender().equals("FusionSlamService")) {
                    gpsimu.setStatus(STATUS.DOWN);
                    terminate();
                }
            }
            
        });

        subscribeBroadcast(CrashedBroadcast.class, new Callback<CrashedBroadcast>() {
            @Override
            public void call(CrashedBroadcast c) {
                gpsimu.setStatus(STATUS.ERROR);
                terminate();
            }
        });
    }
}
