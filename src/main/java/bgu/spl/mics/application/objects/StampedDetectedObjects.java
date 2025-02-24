package bgu.spl.mics.application.objects;

import java.util.List;

/**
 * Represents objects detected by the camera at a specific timestamp.
 * Includes the time of detection and a list of detected objects.
 */
public class StampedDetectedObjects {
    
    private final int time; // The time of detection
    private final List<DetectedObject> detectedObjects; // List of detected objects

    /**
     * Constructor for StampedDetectedObjects.
     *
     * @param time            The time the objects were detected.
     * @param detectedObjects The list of detected objects.
     */
    public StampedDetectedObjects(int time, List<DetectedObject> detectedObjects) {
        this.time = time;
        this.detectedObjects = detectedObjects;
    }

    /**
     * Gets the time of detection.
     *
     * @return The time.
     */
    public int getTime() {
        return time;
    }

    /**
     * Gets the list of detected objects.
     *
     * @return The list of detected objects.
     */
    public List<DetectedObject> getDetectedObjects() {
        return detectedObjects;
    }
}
