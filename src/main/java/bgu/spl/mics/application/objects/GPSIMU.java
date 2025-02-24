package bgu.spl.mics.application.objects;

import java.util.List;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;

/**
 * Represents the robot's GPS and IMU system.
 * Provides information about the robot's position and movement.
 */
public class GPSIMU {
    
    private int currentTick;
    private STATUS status;
    private List <Pose> jsonPoseData;

    public GPSIMU(String jsonFilePath) {
        this.currentTick = 0;
        this.status = STATUS.UP;
        this.jsonPoseData = loadPosesFromFile(jsonFilePath);
    }

    public int getCurrentTick() {
        return currentTick;
    }

    public STATUS getStatus() {
        return status;
    }

    public List<Pose> getJsonPoseData() {
        return jsonPoseData;
    }

    public void setStatus(STATUS status) {
        this.status = status;
    }

    public void setCurrentTick(int currentTick) {
        this.currentTick = currentTick;
    }

    private List<Pose> loadPosesFromFile(String filePath) {
        Gson gson = new Gson();
        try (FileReader reader = new FileReader(filePath)) {
            Type type = new TypeToken<List<Pose>>() {}.getType();
            return gson.fromJson(reader, type);
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

}
