package bgu.spl.mics.application.objects;

import org.junit.jupiter.api.Test;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CameraTest {

    @Test
    void testLoadDetectedObjectsFromFile_Success() throws IOException {
        // Arrange: Create JSON file with multiple detected objects
        String jsonFilePath = "test_camera_data.json";
        String cameraKey = "camera1";
        String jsonContent = "{\n" +
                "    \"camera1\": [\n" +
                "        {\n" +
                "            \"time\": 2,\n" +
                "            \"detectedObjects\": [\n" +
                "                {\"id\": \"Wall_1\", \"description\": \"Wall\"}\n" +
                "            ]\n" +
                "        },\n" +
                "        {\n" +
                "            \"time\": 4,\n" +
                "            \"detectedObjects\": [\n" +
                "                {\"id\": \"Wall_3\", \"description\": \"Wall\"},\n" +
                "                {\"id\": \"Chair_Base_1\", \"description\": \"Chair Base\"}\n" +
                "            ]\n" +
                "        }\n" +
                "    ]\n" +
                "}";

        try (FileWriter writer = new FileWriter(jsonFilePath)) {
            writer.write(jsonContent);
        }

        Camera camera = new Camera(1, 10, jsonFilePath, cameraKey);

        // Act: Load the detected objects from the JSON file
        List<StampedDetectedObjects> result = camera.getJsonDetectedObjectsList();

        // Assert: Verify the loaded data
        assertNotNull(result, "The result should not be null.");
        assertEquals(2, result.size(), "The list should contain two stamped objects.");

        // Assert: Validate the details of the first detected object
        StampedDetectedObjects firstDetected = result.get(0);
        assertEquals(2, firstDetected.getTime(), "The time of the first object should match.");
        assertEquals(1, firstDetected.getDetectedObjects().size(), "The first object should have one detected object.");
        assertEquals("Wall_1", firstDetected.getDetectedObjects().get(0).getId(), "The ID of the first detected object should match.");
        assertEquals("Wall", firstDetected.getDetectedObjects().get(0).getDescription(), "The description of the first detected object should match.");

        // Assert: Validate the details of the second detected object
        StampedDetectedObjects secondDetected = result.get(1);
        assertEquals(4, secondDetected.getTime(), "The time of the second object should match.");
        assertEquals(2, secondDetected.getDetectedObjects().size(), "The second object should have two detected objects.");
        assertEquals("Wall_3", secondDetected.getDetectedObjects().get(0).getId(), "The ID of the first detected object in the second stamp should match.");
        assertEquals("Chair_Base_1", secondDetected.getDetectedObjects().get(1).getId(), "The ID of the second detected object in the second stamp should match.");
    }

    @Test
    void testLoadDetectedObjectsFromFile_EmptyDetectedObjects() throws IOException {
        // Arrange: Create a JSON file with an empty list of detected objects for the camera (Pre-condition).
        String jsonFilePath = "test_camera_with_empty_objects.json";
        String cameraKey = "camera1";
        String jsonContent = "{\n" +
                "    \"camera1\": [\n" +
                "        {\n" +
                "            \"time\": 1,\n" +
                "            \"detectedObjects\": []\n" +
                "        }\n" +
                "    ]\n" +
                "}";

        // Pre-condition: The JSON file must exist and should contain an empty detectedObjects list.
        try (FileWriter writer = new FileWriter(jsonFilePath)) {
            writer.write(jsonContent);
        }

        Camera camera = new Camera(1, 10, jsonFilePath, cameraKey);

        // Act: Call the method that loads detected objects.
        List<StampedDetectedObjects> result = camera.getJsonDetectedObjectsList();

        // Assert: Verify the post-conditions.
        // Post-condition: The result list should not be null.
        assertNotNull(result, "The result should not be null.");

        // Post-condition: The result list should contain one entry (the stamped object), but its detectedObjects list should be empty.
        assertEquals(1, result.size(), "The list should contain one stamped object.");
        assertTrue(result.get(0).getDetectedObjects().isEmpty(), "The detectedObjects list should be empty.");
    }
}