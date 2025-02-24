package bgu.spl.mics.application.objects;

import java.util.ArrayList;
import java.util.List;

import bgu.spl.mics.application.messages.DetectObjectsEvents;

/**
 * LiDarWorkerTracker is responsible for managing a LiDAR worker.
 * It processes DetectObjectsEvents and generates TrackedObjectsEvents by using data from the LiDarDataBase.
 * Each worker tracks objects and sends observations to the FusionSlam service.
 */
public class LiDarWorkerTracker {

    private final int id; // The ID of the LiDAR
    private final int frequency; // Time interval at which the LiDAR sends new events
    private STATUS status; // Current status of the LiDAR

    // The one beneath might be deleted WWWWWWWWWWWWWWWARNING
    private List<TrackedObject> lastTrackedObjects; // The last objects the LiDAR tracked
    private final List<DetectObjectsEvents> recievedFromCam; // The StampedObjects recieved from camera
    private final LiDarDataBase lidarDataBase;

    public LiDarWorkerTracker(int id, int frequency, String jsonFilePath) {
        this.id = id;
        this.frequency = frequency;
        this.status = STATUS.UP; // Default status
        this.recievedFromCam = new ArrayList<>();
        this.lastTrackedObjects = new ArrayList<>();
        this.lidarDataBase = LiDarDataBase.getInstance(jsonFilePath);
    }

    public int getId() {
        return id;
    }

    public int getFrequency() {
        return frequency;
    }

    public STATUS getStatus() {
        return status;
    }

    public void setStatus(STATUS status) {
        this.status = status;
    }

    public List<TrackedObject> getLastTrackedObjects() {
        return lastTrackedObjects;
    }

    public void setLastTrackedObjects (List<TrackedObject> updatedLastTrackedObjects) {
        this.lastTrackedObjects = updatedLastTrackedObjects;
    }

    public List<DetectObjectsEvents> getRecievedFromCam() {
        return recievedFromCam;
    }

    public LiDarDataBase getLidarDataBase() {
        return lidarDataBase;
    }

}
