package bgu.spl.mics.application.objects;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Holds statistical information about the system's operation.
 * This class aggregates metrics such as the runtime of the system,
 * the number of objects detected and tracked, and the number of landmarks
 * identified.
 */
public class StatisticalFolder {

    private AtomicInteger systemRuntime;
    private AtomicInteger numDetectedObjects;
    private AtomicInteger numTrackedObjects;
    private AtomicInteger numLandmarks;
    private List<LandMark> worldMap;

    private String faultySensor;
    private boolean isError;
    private String errorDescription;
    private ConcurrentHashMap<String, StampedDetectedObjects> lastFramesByCameras;
    private ConcurrentHashMap<String, List<TrackedObject>> lastTrackedByLidars;
    private List<Pose> poses;

    private StatisticalFolder() {
        this.systemRuntime = new AtomicInteger(0);
        this.numDetectedObjects = new AtomicInteger(0);
        this.numTrackedObjects = new AtomicInteger(0);
        this.numLandmarks = new AtomicInteger(0);
        this.worldMap = null;

        this.isError = false;
        this.errorDescription = null;
        this.faultySensor = null;
        this.lastFramesByCameras = new ConcurrentHashMap<>();
        this.lastTrackedByLidars = new ConcurrentHashMap<>();
        this.poses = new ArrayList<>();

    }

    private static class StatisticalFolderHolder {
        private static final StatisticalFolder instance = new StatisticalFolder();
    }

    /**
     * Retrieves the singleton instance of the StatisticalFolder.
     *
     * @return The singleton instance of the StatisticalFolder.
     */
    public static StatisticalFolder getInstance() {
        return StatisticalFolderHolder.instance;
    }

    /**
     * Increments the system runtime by one tick.
     */
    public void incrementSystemRuntime() {
        systemRuntime.incrementAndGet();
    }

    /**
     * Increments the number of detected objects by one.
     */
    public void incrementNumDetectedObjects(int delta) {
        numDetectedObjects.addAndGet(delta);
    }

    /**
     * Increments the number of tracked objects by one.
     */
    public void incrementNumTrackedObjects(int delta) {
        numTrackedObjects.addAndGet(delta);
    }

    /**
     * Increments the number of landmarks by one.
     */
    public void incrementNumLandmarks() {
        numLandmarks.incrementAndGet();
    }

    /**
     * Sets the faulty sensor that caused the error.
     *
     * @param faultySensor The ID of the faulty sensor.
     */
    public void setFaultySensor(String faultySensor) {
        this.faultySensor = faultySensor;
    }

    public void setErrorDescription(String errorDescription) {
        this.errorDescription = errorDescription;
    }

    public void setIsError(boolean isError) {
        this.isError = isError;
    }

    public void setLastCameraFrame(String CameraName, StampedDetectedObjects stampedDetectedObjects) {
        if (lastFramesByCameras.containsKey(CameraName)) {
            lastFramesByCameras.replace(CameraName, stampedDetectedObjects);
        } else {
            lastFramesByCameras.put(CameraName, stampedDetectedObjects);
        }
    }

    public void setLastLidarTracked(String lidarName, List<TrackedObject> trackedObjects) {
        if (lastTrackedByLidars.containsKey(lidarName)) {
            lastTrackedByLidars.replace(lidarName, trackedObjects);
        } else {
            lastTrackedByLidars.put(lidarName, trackedObjects);
        }
    }

    public void setWorldMap(List<LandMark> worldMap) {
        this.worldMap = worldMap;
    }

    public void addPose(Pose pose) {
        poses.add(pose);
    }

    // Getters

    public List<Pose> getPoses() {
        return poses;
    }

    public List<LandMark> getWorldMap() {
        return worldMap;
    }

    public ConcurrentHashMap<String, StampedDetectedObjects> getLastFramesByCameras() {
        return lastFramesByCameras;
    }

    public ConcurrentHashMap<String, List<TrackedObject>> getLastTrackedByLidars() {
        return lastTrackedByLidars;
    }

    public boolean getIsError() {
        return isError;
    }

    public String getErrorDescription() {
        return errorDescription;
    }

    public int getSystemRuntime() {
        return systemRuntime.get();
    }

    public String getFaultySensor() {
        return faultySensor;
    }

    public int getNumDetectedObjects() {
        return numDetectedObjects.get();
    }

    public int getNumTrackedObjects() {
        return numTrackedObjects.get();
    }

    public int getNumLandmarks() {
        return numLandmarks.get();
    }

    public void exportToJson(String filePath) {
        // Create a map to hold the data
        Map<String, Object> data = new LinkedHashMap<>();

        // If there was an error, add error details
        if (isError) {
            Map<String, Object> errorDetails = new LinkedHashMap<>();
            errorDetails.put("Error", errorDescription);
            errorDetails.put("faultySensor", faultySensor); // this is the faultySensor itself and not the string
            errorDetails.put("lastDetectedByCamera", lastFramesByCameras);
            errorDetails.put("lastTrackedByLidar", lastTrackedByLidars);
            errorDetails.put("poses", poses);
            data.put("errorDetails", errorDetails);
        }

        // Add statistics
        data.put("systemRuntime", systemRuntime.get());
        data.put("numDetectedObjects", numDetectedObjects.get());
        data.put("numTrackedObjects", numTrackedObjects.get());
        data.put("numLandmarks", numLandmarks);

        // Add world map (landmarks)
        data.put("landMarks", worldMap);

        // Serialize the map to JSON
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String json = gson.toJson(data);

        // Write to file
        String outputPath = filePath + "/outputBONANZA_file.json";
        try (FileWriter writer = new FileWriter(outputPath)) {
            writer.write(json);
        } catch (IOException e) {
            System.err.println("Error writing JSON to file: " + e.getMessage());
        }
    }

}
