package bgu.spl.mics.application.messages;

import bgu.spl.mics.Event;
import bgu.spl.mics.application.objects.StampedDetectedObjects;

public class DetectObjectsEvents implements Event <Boolean> {
    
    private final StampedDetectedObjects stampedDetectedObjects;

      /**
     * Constructor for DetectObjectsEvent.
     * @param stampedDetectedObjects - The detected objects and their timestamp.
     */
    public DetectObjectsEvents(StampedDetectedObjects stampedDetectedObjects) {
        this.stampedDetectedObjects = stampedDetectedObjects;
    }

    /**
     * @return The stamped detected objects associated with this event.
     */
    public StampedDetectedObjects getStampedDetectedObjects() {
        return stampedDetectedObjects;
    }

    
}
