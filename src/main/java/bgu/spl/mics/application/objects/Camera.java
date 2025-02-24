package bgu.spl.mics.application.objects;


import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;



/**
 * Represents a camera sensor on the robot.
 * Responsible for detecting objects in the environment.
 */
public class Camera {
    
    private final int id; // The ID of the camera
    private final String cameraKey; // The key of the camera
    private final int frequency; // Time interval at which the camera sends new events
    private STATUS status; // Current status of the camera
    private final List<StampedDetectedObjects> jsonDetectedObjectsList; // List of stamped detected objects
   

    /**
     * Constructor for Camera.
     *
     * @param id                 The unique ID of the camera.
     * @param frequency          The time interval at which the camera sends new events.
     */
    public Camera(int id, int frequency, String jsonFilePath, String cameraKey) {
        this.id = id;
        this.frequency = frequency;
        this.cameraKey = cameraKey;
        this.jsonDetectedObjectsList = loadDetectedObjectsFromFile(jsonFilePath, cameraKey);
        this.status = STATUS.UP; // Default status
       
    }

    public List<StampedDetectedObjects> getJsonDetectedObjectsList() {
        return jsonDetectedObjectsList;
    }

    /**
     * Gets the ID of the camera.
     *
     * @return The camera ID.
     */
    public int getId() {
        return id;
    }

    /**
     * Gets the frequency of the camera.
     *
     * @return The time interval in ticks for sending events.
     */
    public int getFrequency() {
        return frequency;
    }

    /**
     * Gets the current status of the camera.
     *
     * @return The status (UP, DOWN, ERROR).
     */
    public STATUS getStatus() {
        return status;
    }

    /**
     * *Gets the camera key
     *
     * @return the key.
     */

    public String getCameraKey() {
        return cameraKey;
    }

    /**
     * Sets the status of the camera.
     *
     * @param status The new status of the camera.
     */
    public void setStatus(STATUS status) {
        this.status = status;
    }

      /**
     * Loads detected objects from a JSON file.
     *
     * @param filePath The path to the JSON file.
     * @return A list of StampedDetectedObjects.
     */
    private List<StampedDetectedObjects> loadDetectedObjectsFromFile(String filePath, String cameraID) {
        Gson gson = new Gson();
        try (FileReader reader = new FileReader(filePath)) {
            // Parse the JSON file into a map of camera IDs to their data
            Type type = new TypeToken<Map<String, List<StampedDetectedObjects>>>() {}.getType();
            Map<String, List<StampedDetectedObjects>> cameraData = gson.fromJson(reader, type);

            // Return the data for the specified camera ID
            List<StampedDetectedObjects> cameraObjects = cameraData.get(cameraID);

            if (cameraObjects != null) {
                return new ArrayList<>(cameraObjects);
            } else {
                return new ArrayList<>();
            }
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
}
