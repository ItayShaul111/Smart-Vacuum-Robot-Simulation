package bgu.spl.mics.application.objects;

import java.util.ArrayList;
import java.util.List;

import bgu.spl.mics.application.messages.TrackedObjectsEvent;

/**
 * Manages the fusion of sensor data for simultaneous localization and mapping (SLAM).
 * Combines data from multiple sensors (e.g., LiDAR, camera) to build and update a global map.
 * Implements the Singleton pattern to ensure a single instance of FusionSlam exists.
 */
public class FusionSlam {

    private List<Pose> posesList; // List of Pose objects representing the robot's poses
    private List<LandMark> landmarksList; // WORLDMAP
    private List<TrackedObjectsEvent> eventsWaitingForPose;
    // private LiDarDataBase liDarDataBase;
    private int numOfSensors;

    private FusionSlam() {
        posesList = new ArrayList<>();
        landmarksList = new ArrayList<>();
        eventsWaitingForPose = new ArrayList<>();
        this.numOfSensors = 0;
        // liDarDataBase = LiDarDataBase.getInstance("");
    }

    // Singleton instance holder
    private static class FusionSlamHolder {
        private static FusionSlam instance = new FusionSlam();
    }

    public static FusionSlam getInstance() {
        return FusionSlamHolder.instance;
    }

    // public LiDarDataBase getLiDarDataBase() {
    //     return liDarDataBase;
    // }

    public List<Pose> getPoses() {
        return posesList;
    }

    public int getNumOfSensors() {
        return numOfSensors;
    }

    public void setNumOfSensors(int numOfSensors) {
        this.numOfSensors = numOfSensors;
    }

    public List<LandMark> getLandmarks() {
        return landmarksList;
    }

    public List<TrackedObjectsEvent> getEventsWaitingForPose() {
        return eventsWaitingForPose;
    }
    
    // Helper function to determine wether a landmark already exists in the world map
    public LandMark isExistsInWorldMap (String id) {
        for (LandMark lm : landmarksList) {
            if (lm.getId().equals(id)) {
                return lm;
            }
        }
        return null;
    }

    // Helper function to convert a CloudPoint to the global coordinate system
    public CloudPoint convertCloudPoint (CloudPoint cloudPoint, Pose pose) {
        double radYaw = pose.getYaw() * (Math.PI / 180);
        double x = cloudPoint.getX() * Math.cos(radYaw) - Math.sin(radYaw) * cloudPoint.getY() + pose.getX();
        double y = cloudPoint.getX() * Math.sin(radYaw) + Math.cos(radYaw) * cloudPoint.getY() + pose.getY();
        return new CloudPoint(x, y);
    }

    // Helper function to add a new landmark to the world map
    public void addOrSetObjInWorldMap (TrackedObject trackedObject, Pose pose) {
        List<LandMark> currentLandMarks = getLandmarks();

        // Convert the cloud points to the global coordinate system
        List<CloudPoint> convertedCloudPoints = new ArrayList<>();
        for (CloudPoint cp : trackedObject.getCoordinates()) {
            convertedCloudPoints.add(convertCloudPoint(cp, pose));
        }

        // Check if the landmark already exists in the world map
        LandMark prevLandMark = isExistsInWorldMap(trackedObject.getId());
        
        // First case - Landmark does not exist in the world map
        if(prevLandMark == null) {
            prevLandMark = new LandMark(trackedObject.getId(), trackedObject.getDescription(), convertedCloudPoints);
            currentLandMarks.add(prevLandMark);

            // Update the StatiscalFolder
            StatisticalFolder.getInstance().incrementNumLandmarks();

        // Second case - Landmark already exists in the world map
        } else {
            List<CloudPoint> prevCloudPoints = prevLandMark.getCloudPoints();
            List<CloudPoint> newCloudPoints = new ArrayList<>();
            for (int i = 0; i < prevCloudPoints.size() && i < convertedCloudPoints.size(); i++) {
                double x = (prevCloudPoints.get(i).getX() + convertedCloudPoints.get(i).getX()) / 2;
                double y = (prevCloudPoints.get(i).getY() + convertedCloudPoints.get(i).getY()) / 2;
                newCloudPoints.add(new CloudPoint(x, y));
            }
            
            // In case the new landmark has less cloud points than the previous one
            for (int i = convertedCloudPoints.size(); i < prevCloudPoints.size(); i++) {
                newCloudPoints.add(prevCloudPoints.get(i));
            }
            
            // In case the new landmark has more cloud points than the previous one
            for (int i = prevCloudPoints.size(); i < convertedCloudPoints.size(); i++) {
                newCloudPoints.add(convertedCloudPoints.get(i));
            }
            
            prevLandMark.setCloudPoints(newCloudPoints);
        }
    }

    
}
