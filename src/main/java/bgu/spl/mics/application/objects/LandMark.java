package bgu.spl.mics.application.objects;

import java.util.List;

/**
 * Represents a landmark in the environment map.
 * Landmarks are identified and updated by the FusionSlam service.
 */
public class LandMark {
    
    private String id; // The ID of the landmark
    private String description; // The description of the landmark
    List <CloudPoint> cloudPoints; // List of CloudPoints representing the point cloud of the landmark according to the charging station's coordinate system

    public LandMark(String id, String description, List<CloudPoint> cloudPoints) {
        this.id = id;
        this.description = description;
        this.cloudPoints = cloudPoints;
    }

    public String getId() {
        return id;
    }

    public String getDescription(){
        return description;
    }

    public List<CloudPoint> getCloudPoints() {
        return cloudPoints;
    }

    public void setCloudPoints(List<CloudPoint> cloudPoints) {
        this.cloudPoints = cloudPoints;
    }

    
}

