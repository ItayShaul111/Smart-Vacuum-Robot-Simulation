package bgu.spl.mics.application.messages;

import bgu.spl.mics.Event;
import bgu.spl.mics.application.objects.TrackedObject;
import java.util.List;

public class TrackedObjectsEvent implements Event<Boolean> {
    private List <TrackedObject> trackedObjects;

    public TrackedObjectsEvent(List<TrackedObject> trackedObjects) {
        this.trackedObjects = trackedObjects;
    }

    public int getTrackedObjectsTime() {
        return trackedObjects.get(0).getTime();
    }

    public List<TrackedObject> getTrackedObjects() {
        return trackedObjects;
    }

}