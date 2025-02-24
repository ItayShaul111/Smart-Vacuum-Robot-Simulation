package bgu.spl.mics.application.services;

import bgu.spl.mics.Callback;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.objects.Camera;
import bgu.spl.mics.application.objects.DetectedObject;
import bgu.spl.mics.application.objects.STATUS;
import bgu.spl.mics.application.messages.CrashedBroadcast;
import bgu.spl.mics.application.messages.DetectObjectsEvents;
import bgu.spl.mics.application.messages.TerminatedBroadcast;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.objects.StampedDetectedObjects;
import bgu.spl.mics.application.objects.StatisticalFolder;



/**
 * CameraService is responsible for processing data from the camera and
 * sending DetectObjectsEvents to LiDAR workers.
 * 
 * This service interacts with the Camera object to detect objects and updates
 * the system's StatisticalFolder upon sending its observations.
 */
public class CameraService extends MicroService {

    private final Camera camera;

    /**
     * Constructor for CameraService.
     * @param camera - The camera object associated with this service.
     */
    public CameraService(Camera camera) {
        super("CameraService" + camera.getId());
        this.camera = camera;
    }

    protected void initialize() {
        subscribeBroadcast(TickBroadcast.class, new Callback<TickBroadcast>() {
            public void call(TickBroadcast tickBroadcast) {
                if (camera.getJsonDetectedObjectsList() != null && !camera.getJsonDetectedObjectsList().isEmpty()) {
                    StampedDetectedObjects currentStamped = camera.getJsonDetectedObjectsList().get(0);
                    if (currentStamped.getDetectedObjects().isEmpty()) {
                        camera.getJsonDetectedObjectsList().remove(0);
                        return;
                    }
                    if (currentStamped.getTime() + camera.getFrequency() == tickBroadcast.getTick()) {
                        for (DetectedObject obj : currentStamped.getDetectedObjects()) {
                            // Check if the object is an error
                            if(obj.getId().equals("ERROR")) {
                                System.out.println("CameraService: " + getName() + " detected an error object");
                                StatisticalFolder.getInstance().setErrorDescription(obj.getDescription());
                                StatisticalFolder.getInstance().setFaultySensor("Camera " + camera.getId());
                                StatisticalFolder.getInstance().setIsError(true);
                                sendBroadcast(new CrashedBroadcast());
                                terminate();
                                return;
                            }
                        }

                        StatisticalFolder.getInstance().setLastCameraFrame("Camera " + camera.getId(), currentStamped);
                        camera.getJsonDetectedObjectsList().remove(0);

                        DetectObjectsEvents eve = new DetectObjectsEvents(currentStamped);
                        /*Future <Boolean> future = */sendEvent(eve);
                        if(camera.getJsonDetectedObjectsList().isEmpty()){
                            camera.setStatus(STATUS.DOWN);
                            sendBroadcast(new TerminatedBroadcast("CameraService"));
                            terminate();
                        }
                        // Update the statistical folder with the number of detected objects
                        StatisticalFolder.getInstance().incrementNumDetectedObjects(currentStamped.getDetectedObjects().size());
                    }
                }
            }
        }); 
    

        subscribeBroadcast(TerminatedBroadcast.class, terminatedBroadcast -> {
            if (terminatedBroadcast.getSender().equals("FusionSlamService")) {
                camera.setStatus(STATUS.DOWN);
                terminate();
            }
        });

        subscribeBroadcast(CrashedBroadcast.class, crashedBroadcast -> {
            camera.setStatus(STATUS.ERROR);
            terminate();
        });
    }
}
