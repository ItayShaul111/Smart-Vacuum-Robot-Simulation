package bgu.spl.mics.application.objects;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FusionSlamTest {

    private FusionSlam fusionSlam;

    @BeforeEach
    void setUp() {
        fusionSlam = FusionSlam.getInstance();
        fusionSlam.getLandmarks().clear(); // Clearing the landmarks map before each test ensures pre-condition validity.
    }

    @Test
    void convertCloudPoint() {
        // Arrange
        CloudPoint cloudPoint = new CloudPoint(1.0, 1.0); // Pre-condition: Valid CloudPoint object.
        Pose pose = new Pose(1.0f, 1.0f, 90.0f, 1);       // Pre-condition: Valid Pose object with defined coordinates and orientation.

        // Act
        CloudPoint result = fusionSlam.convertCloudPoint(cloudPoint, pose);

        // Assert
        // Post-condition: The transformed CloudPoint coordinates should match the expected results.
        assertEquals(0.0, result.getX(), 0.01, "X coordinate should match after transformation.");
        assertEquals(2.0, result.getY(), 0.01, "Y coordinate should match after transformation.");

        // Invariant: The original cloudPoint should remain unchanged.
        assertEquals(1.0, cloudPoint.getX(), "Original CloudPoint X coordinate should remain unchanged.");
        assertEquals(1.0, cloudPoint.getY(), "Original CloudPoint Y coordinate should remain unchanged.");
    }

    @Test
    void addOrSetObjInWorldMap() {
        // Arrange
        List<CloudPoint> cloudPoints = Arrays.asList(
                new CloudPoint(1.0, 2.0),
                new CloudPoint(3.0, 4.0)
        ); // Pre-condition: List of valid CloudPoint objects.
        TrackedObject trackedObject = new TrackedObject("LM1", 1, "Landmark 1", cloudPoints); // Pre-condition: Valid TrackedObject.
        Pose pose = new Pose(0.0f, 0.0f, 0.0f, 1); // Pre-condition: Valid Pose with neutral transformation.

        // Act
        fusionSlam.addOrSetObjInWorldMap(trackedObject, pose);

        // Assert
        // Post-condition: Ensure that one landmark is added to the landmarks list.
        assertEquals(1, fusionSlam.getLandmarks().size(), "There should be one landmark in the map.");

        // Post-condition: Validate the properties of the newly added landmark.
        LandMark landmark = fusionSlam.getLandmarks().get(0);
        assertEquals("LM1", landmark.getId(), "Landmark ID should match.");
        assertEquals(2, landmark.getCloudPoints().size(), "CloudPoint count should match.");

        // Invariant: Ensure no unexpected modifications to other system states.
        assertTrue(fusionSlam.getLandmarks().stream().allMatch(l -> l.getCloudPoints().size() > 0),
                "All landmarks should have valid CloudPoints.");
    }
}