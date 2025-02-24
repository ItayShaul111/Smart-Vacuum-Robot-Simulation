package bgu.spl.mics.application;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import bgu.spl.mics.application.objects.Camera;
import bgu.spl.mics.application.objects.FusionSlam;
import bgu.spl.mics.application.objects.GPSIMU;
import bgu.spl.mics.application.objects.LiDarWorkerTracker;
import bgu.spl.mics.application.objects.StatisticalFolder;
import bgu.spl.mics.application.services.CameraService;
import bgu.spl.mics.application.services.FusionSlamService;
import bgu.spl.mics.application.services.LiDarService;
import bgu.spl.mics.application.services.PoseService;
import bgu.spl.mics.application.services.TimeService;

/**
 * The main entry point for the GurionRock Pro Max Ultra Over 9000 simulation.
 * <p>
 * This class initializes the system and starts the simulation by setting up
 * services, objects, and configurations.
 * </p>
 */
public class GurionRockRunner {

    /**
     * The main method of the simulation.
     * This method sets up the necessary components, parses configuration files,
     * initializes services, and starts the simulation.
     *
     * @param args Command-line arguments. The first argument is expected to be the
     *             path to the configuration file.
     */
    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Usage: GurionRockRunner <path to configuration file>");
            return;
        }

        String basePath = null;
        String configFilePath = args[0];

        // String configFilePath = args[0];
        Gson gson = new Gson();
        try (FileReader reader = new FileReader(configFilePath)) {
            
            System.out.println("Reading configuration file...");
            basePath = new File(configFilePath).getParent(); //the path to the input folder
            JsonObject config = gson.fromJson(reader, JsonObject.class);

            // 1. Parse Camera Configurations
            JsonObject cameraConfig = config.getAsJsonObject("Cameras");
            JsonArray cameraConfigurations = cameraConfig.getAsJsonArray("CamerasConfigurations");
            String cameraDataPath = cameraConfig.get("camera_datas_path").getAsString();
            List<Camera> cameras = initializeCameras(cameraConfigurations, basePath + "//" + cameraDataPath);

            // 2. Parse LiDAR Configurations
            JsonObject lidarConfig = config.getAsJsonObject("LiDarWorkers");
            JsonArray lidarConfigurations = lidarConfig.getAsJsonArray("LidarConfigurations");
            String lidarDataPath = lidarConfig.get("lidars_data_path").getAsString();
            List<LiDarWorkerTracker> lidarWorkers = initializeLiDarWorkers(lidarConfigurations,basePath + "//"  + lidarDataPath);

            // 3. Parse Pose Data
            String poseDataPath = config.get("poseJsonFile").getAsString();
            GPSIMU gpsIMU = new GPSIMU(basePath + "//"  + poseDataPath);

            // 4. Initialize Statistical Folder
            StatisticalFolder statistics = StatisticalFolder.getInstance();

            // 5. Extract Tick Time and Duration
            int tickTime = config.get("TickTime").getAsInt();
            int duration = config.get("Duration").getAsInt();

            // 6. Start Simulation
            FusionSlam fusionSlam = FusionSlam.getInstance();
            fusionSlam.setNumOfSensors(cameras.size() + lidarWorkers.size() + 1);

            initializeAndRunThreads(cameras, lidarWorkers, gpsIMU, fusionSlam, tickTime, duration);
            System.out.println("Simulation complete.");

            statistics.exportToJson(basePath);

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Failed to read configuration file.");
        }
    }

    /*************************************************** Helper Function *****************************************************/
    private static void initializeAndRunThreads(
            List<Camera> cameras,
            List<LiDarWorkerTracker> lidarWorkers,
            GPSIMU gpsIMU,
            FusionSlam fusionSlam,
            int tickTime,
            int duration) {
        List<Thread> threads = new ArrayList<>();

        // Initialize camera services
        for (Camera camera : cameras) {
            threads.add(new Thread(new CameraService(camera)));
        }

        // Initialize LiDAR services
        for (LiDarWorkerTracker lidar : lidarWorkers) {
            threads.add(new Thread(new LiDarService(lidar)));
        }

        // Initialize pose service
        threads.add(new Thread(new PoseService(gpsIMU)));

        // Initialize Fusion-SLAM service
        Thread fusionThread = new Thread(new FusionSlamService());

        // Initialize time service
        Thread timeThread = new Thread(new TimeService(tickTime * 1000, duration));

        // Start threads
        threads.forEach(Thread::start);
        fusionThread.start();

        try {
            Thread.sleep(1); //sleep to give time for all the microservice to subscribe for tick
        } catch (Exception e) {}

        timeThread.start();


        // Wait for Fusion-SLAM and TimeService to finish
        try {
            fusionThread.join();
            timeThread.join();
        } catch (InterruptedException e) {
            System.err.println("Error during Fusion-SLAM thread join.");
        }
    }

    private static List<Camera> initializeCameras(JsonArray cameraConfigurations, String dataPath) {
        List<Camera> cameras = new ArrayList<>();
        for (JsonElement cameraElement : cameraConfigurations) {
            JsonObject cameraObject = cameraElement.getAsJsonObject();
            int id = cameraObject.get("id").getAsInt();
            int frequency = cameraObject.get("frequency").getAsInt();
            String cameraKey = cameraObject.get("camera_key").getAsString();
            cameras.add(new Camera(id, frequency, dataPath, cameraKey));
        }
        return cameras;
    }

    private static List<LiDarWorkerTracker> initializeLiDarWorkers(JsonArray lidarConfigurations, String dataPath) {
        List<LiDarWorkerTracker> lidarWorkers = new ArrayList<>();
        for (JsonElement lidarElement : lidarConfigurations) {
            JsonObject lidarObject = lidarElement.getAsJsonObject();
            int id = lidarObject.get("id").getAsInt();
            int frequency = lidarObject.get("frequency").getAsInt();
            lidarWorkers.add(new LiDarWorkerTracker(id, frequency, dataPath));
        }
        return lidarWorkers;
    }
}
