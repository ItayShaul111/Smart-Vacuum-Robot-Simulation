package bgu.spl.mics.application.services;


import bgu.spl.mics.Callback;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.objects.CloudPoint;
import bgu.spl.mics.application.objects.DetectedObject;
import bgu.spl.mics.application.objects.LiDarWorkerTracker;
import bgu.spl.mics.application.objects.TrackedObject;
import bgu.spl.mics.application.objects.STATUS;
import bgu.spl.mics.application.objects.StampedCloudPoints;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.messages.TerminatedBroadcast;
import bgu.spl.mics.application.messages.CrashedBroadcast;
import bgu.spl.mics.application.messages.DetectObjectsEvents;
import bgu.spl.mics.application.messages.TrackedObjectsEvent;
import bgu.spl.mics.application.objects.StampedDetectedObjects;
import bgu.spl.mics.application.objects.StatisticalFolder;
import java.util.ArrayList;
import java.util.List;

/**
 * LiDarService is responsible for processing data from the LiDAR sensor and
 * sending TrackedObjectsEvents to the FusionSLAM service.
 * 
 * This service interacts with the LiDarWorkerTracker object to retrieve and process
 * cloud point data and updates the system's StatisticalFolder upon sending its
 * observations.
 */
public class LiDarService extends MicroService {

    private final LiDarWorkerTracker liDarWorker;
    private int currentTime;

    /**
     * Constructor for LiDarService.
     *
     * @param LiDarWorkerTracker A LiDAR Tracker worker object that this service will use to process data.
     */
    public LiDarService(LiDarWorkerTracker LiDarWorkerTracker) {
        super("LiDarService" + LiDarWorkerTracker.getId());
        this.liDarWorker = LiDarWorkerTracker;
        this.currentTime = 0;
    }

    /**
     * Initializes the LiDarService.
     * Registers the service to handle DetectObjectsEvents and TickBroadcasts,
     * and sets up the necessary callbacks for processing data.
     */
    @Override
    protected void initialize() {
        // Subscribe to TickBroadcast
        subscribeBroadcast(TickBroadcast.class, new Callback<TickBroadcast>() {
            @Override
            public void call(TickBroadcast tickBroadcast) {
                currentTime = tickBroadcast.getTick();

                if(liDarWorker.getLidarDataBase().getFinished() && liDarWorker.getRecievedFromCam().isEmpty()){
                    liDarWorker.setStatus(STATUS.DOWN);
                    sendBroadcast(new TerminatedBroadcast("LiDarService"));
                    terminate();
                }

                List<DetectObjectsEvents> readyToBeSent = new ArrayList<>();
                while (!liDarWorker.getRecievedFromCam().isEmpty()
                        && liDarWorker.getRecievedFromCam().get(0).getStampedDetectedObjects().getTime()
                                + liDarWorker.getFrequency() <= currentTime) {
                    readyToBeSent.add(liDarWorker.getRecievedFromCam().get(0));
                    liDarWorker.getRecievedFromCam().remove(0);
                }
                
                if (readyToBeSent.isEmpty()) {
                    return;
                }

                // Combing all available objects from all events from cameras to one list
                List <DetectedObject> allDetectedObjects = new ArrayList<>();
                for (DetectObjectsEvents event : readyToBeSent){
                    for (DetectedObject detectedObject : event.getStampedDetectedObjects().getDetectedObjects()) {
                        allDetectedObjects.add(detectedObject);
                    }
                }

                // Creating an event for the FusionSlam
                StampedDetectedObjects combinedStamped = new StampedDetectedObjects(readyToBeSent.get(0).getStampedDetectedObjects().getTime(), allDetectedObjects);
                List <TrackedObject> combinedTracked = convertToTrackedObjects(combinedStamped);

                // Check if error detected
                if (combinedTracked == null || combinedTracked.isEmpty()) {
                    return;    
                }
                
                liDarWorker.setLastTrackedObjects(combinedTracked); /// MIGHT BE DELETED
                StatisticalFolder.getInstance().setLastLidarTracked("LiDarTrackerWorker " + liDarWorker.getId(), combinedTracked);

                TrackedObjectsEvent currentEvent = new TrackedObjectsEvent(combinedTracked);
                /*Future <Boolean> future = */sendEvent(currentEvent);

                // Updating the statistical folder
                StatisticalFolder.getInstance().incrementNumTrackedObjects(combinedTracked.size());

                // Completing cam events
                for (DetectObjectsEvents event : readyToBeSent){
                    complete(event, true);
                }

             }
        });

        // Subscribe to DetectObjectsEvent
        subscribeEvent(DetectObjectsEvents.class, new Callback<DetectObjectsEvents>() {
            @Override
            public void call(DetectObjectsEvents event) {
                if(liDarWorker.getLidarDataBase().getFinished() && liDarWorker.getRecievedFromCam().isEmpty()){
                    liDarWorker.setStatus(STATUS.DOWN);
                    sendBroadcast(new TerminatedBroadcast("LiDarService"));
                    terminate();
                }
                if(event.getStampedDetectedObjects().getTime() + liDarWorker.getFrequency() <= currentTime){
                    List <TrackedObject> trackedObjects = convertToTrackedObjects(event.getStampedDetectedObjects());
                    // Check if error detected
                    if (trackedObjects == null || trackedObjects.isEmpty()) {
                        return;    
                    }

                    liDarWorker.setLastTrackedObjects(trackedObjects);
                    StatisticalFolder.getInstance().setLastLidarTracked("LiDarTrackerWorker " + liDarWorker.getId(), trackedObjects);

                    TrackedObjectsEvent trackedObjectsEvent = new TrackedObjectsEvent(trackedObjects);
                    /*Future <Boolean> future = */sendEvent(trackedObjectsEvent);

                    StatisticalFolder.getInstance().incrementNumTrackedObjects(trackedObjects.size());
                    
                    
                    complete(event, true); // Completing the recived from camera event.

                } else {
                    liDarWorker.getRecievedFromCam().add(event);
                }

            }
        });

        // Subscribe to TerminatedBroadcast
        subscribeBroadcast(TerminatedBroadcast.class, new Callback<TerminatedBroadcast>() {
            @Override
            public void call(TerminatedBroadcast terminatedBroadcast) {
                if (terminatedBroadcast.getSender().equals("FusionSlamService")) {
                    liDarWorker.setStatus(STATUS.DOWN);
                    terminate();
                }
            }
        });

        // Subscribe to CrashedBroadcast
        subscribeBroadcast(CrashedBroadcast.class, new Callback<CrashedBroadcast>() {
            @Override
            public void call(CrashedBroadcast crashedBroadcast) {
                liDarWorker.setStatus(STATUS.ERROR);
                terminate();
            }
        });
    }

       /**. HELPER FUNCTION BECAUSE WE NEED HELP
    * Converts a list of DetectedObjects from a StampedDetectedObjects to a list of TrackedObjects.
    *
    * @param stampedDetectedObjects The StampedDetectedObjects containing DetectedObjects and the associated time.
    * @return A list of TrackedObjects created from the detected objects.
    */
    public List<TrackedObject> convertToTrackedObjects(StampedDetectedObjects stampedDetectedObjects) {
        List<TrackedObject> result = new ArrayList<>();
        List<StampedCloudPoints> lidarDataBase = liDarWorker.getLidarDataBase().getCloudPoints();
    
        // Iterate through detected objects
        for (DetectedObject detectedObject : stampedDetectedObjects.getDetectedObjects()) {
            for (StampedCloudPoints stampedCloudPoint : lidarDataBase) {
                // Check if error detected
                if (stampedCloudPoint.getTime() == stampedDetectedObjects.getTime() && stampedCloudPoint.getId().equals("ERROR")) {
                    StatisticalFolder.getInstance().setIsError(true);
                    StatisticalFolder.getInstance().setFaultySensor("LiDarTrackerWorker " + liDarWorker.getId());
                    StatisticalFolder.getInstance().setErrorDescription("LidarWorker" + liDarWorker.getId() + " disconnected");
                    sendBroadcast(new CrashedBroadcast());
                    terminate();
                    return null; // In order to stop the loop.
                }

                if (stampedCloudPoint.getId().equals(detectedObject.getId()) &&
                    stampedCloudPoint.getTime() == stampedDetectedObjects.getTime()) {
                
                    List<CloudPoint> cloudPoints = new ArrayList<>();
                    for (List<Double> cloudPts : stampedCloudPoint.getCloudPoints()){
                        cloudPoints.add(new CloudPoint(cloudPts.get(0), cloudPts.get(1)));
                    }

                    // Create a new TrackedObject with the matched data
                    TrackedObject newTrackedObject = new TrackedObject(
                        stampedCloudPoint.getId(),
                        stampedCloudPoint.getTime(),
                        detectedObject.getDescription(),
                        cloudPoints
                    );
                    
                    // Add to the result list and break
                    result.add(newTrackedObject);
                    break;
                }
            }
        }
        return result; // Return the list of converted TrackedObjects
    }

}
