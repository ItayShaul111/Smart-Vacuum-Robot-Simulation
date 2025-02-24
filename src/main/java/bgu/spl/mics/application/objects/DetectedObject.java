package bgu.spl.mics.application.objects;

/**
 * DetectedObject represents an object detected by the camera.
 * It contains information such as the object's ID and description.
 */
public class DetectedObject {

    private final String id; // The unique ID of the object
    private final String description; // Description of the object

    /**
     * Constructor for DetectedObject.
     *
     * @param id          The unique ID of the detected object.
     * @param description A description of the detected object.
     */
    public DetectedObject(String id, String description) {
        this.id = id;
        this.description = description;
    }

    /**
     * Gets the ID of the detected object.
     *
     * @return The ID.
     */
    public String getId() {
        return id;
    }

    /**
     * Gets the description of the detected object.
     *
     * @return The description.
     */
    public String getDescription() {
        return description;
    }
}
