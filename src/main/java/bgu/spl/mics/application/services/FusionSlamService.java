package bgu.spl.mics.application.services;

import bgu.spl.mics.Callback;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.CrashedBroadcast;
import bgu.spl.mics.application.messages.PoseEvent;
import bgu.spl.mics.application.messages.TerminatedBroadcast;
import bgu.spl.mics.application.messages.TrackedObjectsEvent;
import bgu.spl.mics.application.objects.FusionSlam;
import bgu.spl.mics.application.objects.LiDarDataBase;
import bgu.spl.mics.application.objects.StatisticalFolder;
import bgu.spl.mics.application.objects.TrackedObject;
import java.util.List;

/**
 * FusionSlamService integrates data from multiple sensors to build and update
 * the robot's global map.
 * 
 * This service receives TrackedObjectsEvents from LiDAR workers and PoseEvents
 * from the PoseService,
 * transforming and updating the map with new landmarks.
 */
public class FusionSlamService extends MicroService {

    private FusionSlam fusionSlam;
    private LiDarDataBase liDarDataBase;

    /**
     * Constructor for FusionSlamService.
     */
    public FusionSlamService() {
        super("FusionSlamService");
        this.fusionSlam = FusionSlam.getInstance();
        this.liDarDataBase = LiDarDataBase.getInstance("");
    }

    /**
     * Initializes the FusionSlamService.
     * Registers the service to handle TrackedObjectsEvents, PoseEvents, and
     * TickBroadcasts,
     * and sets up callbacks for updating the global map.
     */
    @Override
    protected void initialize() {
        subscribeEvent(TrackedObjectsEvent.class, new Callback<TrackedObjectsEvent>() {
            public void call(TrackedObjectsEvent trackedObjectsEvent) {
                if (trackedObjectsEvent.getTrackedObjects().isEmpty()){
                    return;
                };
                int trackedObjectsTime = trackedObjectsEvent.getTrackedObjectsTime();

                // If the needed pose is already available, update the world map with the
                // tracked objects
                if (fusionSlam.getPoses().size() >= trackedObjectsTime) {
                    List<TrackedObject> trackedObjects = trackedObjectsEvent.getTrackedObjects();
                    for (TrackedObject trackedObject : trackedObjects) {
                        fusionSlam.addOrSetObjInWorldMap(trackedObject,
                                fusionSlam.getPoses().get(trackedObjectsTime - 1));
                        // fusionSlam.getLiDarDataBase().incCounter();
                        liDarDataBase.incCounter();
                    }

                    complete(trackedObjectsEvent, true);
                } else {
                    fusionSlam.getEventsWaitingForPose().add(trackedObjectsEvent);
                }

            }
        });

        subscribeEvent(PoseEvent.class, new Callback<PoseEvent>() { 
            public void call(PoseEvent poseEvent) {
                // Add the new pose to the list of poses
                fusionSlam.getPoses().add(poseEvent.getPose());
                complete(poseEvent, true);

                // Check if there are any TrackedObjectsEvents waiting for this pose
                if (!fusionSlam.getEventsWaitingForPose().isEmpty()) {
                    // Check for each event if the pose time matches the TrackedObjectsEvent time
                    for (TrackedObjectsEvent trackedObjectsEvent : fusionSlam.getEventsWaitingForPose()) {

                        // If the times match, update the world map with the tracked objects
                        if (trackedObjectsEvent.getTrackedObjectsTime() == poseEvent.getPose().getTime()) {
                            List<TrackedObject> trackedObjects = trackedObjectsEvent.getTrackedObjects();
                            for (TrackedObject trackedObject : trackedObjects) {
                                fusionSlam.addOrSetObjInWorldMap(trackedObject, poseEvent.getPose());
                                // fusionSlam.getLiDarDataBase().incCounter();
                                liDarDataBase.incCounter();
                            }
                            complete(trackedObjectsEvent, true);
                        }
                    }

                }
            }
        });

        subscribeBroadcast(TerminatedBroadcast.class, new Callback<TerminatedBroadcast>() {
            public void call(TerminatedBroadcast terminatedBroadcast) {
                // If the sender is a sensor, decrease the number of sensors
                if (terminatedBroadcast.getSender().equals("CameraService") ||
                        terminatedBroadcast.getSender().equals("LiDarService") ||
                        terminatedBroadcast.getSender().equals("PoseService")) {
                    fusionSlam.setNumOfSensors(fusionSlam.getNumOfSensors() - 1);

                    // If there are no more sensors, send a TerminatedBroadcast from FusionSlamService
                    if (fusionSlam.getNumOfSensors() == 0) {
                        StatisticalFolder.getInstance().setWorldMap(fusionSlam.getLandmarks());
                        sendBroadcast(new TerminatedBroadcast("FusionSlamService"));
                    }

                // If the sender is TimeService, terminate the system
                } else if (terminatedBroadcast.getSender().equals("TimeService")) {
                    sendBroadcast(new TerminatedBroadcast("FusionSlamService"));

                // If the sender is FusionSlamService, terminate the system
                } else {
                    StatisticalFolder.getInstance().setWorldMap(fusionSlam.getLandmarks());
                    sendBroadcast(new TerminatedBroadcast("FusionSlamService"));
                    terminate();
                }
            }
        });

        subscribeBroadcast(CrashedBroadcast.class, new Callback<CrashedBroadcast>() {
            public void call(CrashedBroadcast crashedBroadcast) {
                StatisticalFolder.getInstance().setWorldMap(fusionSlam.getLandmarks());
                sendBroadcast(new TerminatedBroadcast("FusionSlamService"));
                terminate();
            }
        });
        ;
    }

}
