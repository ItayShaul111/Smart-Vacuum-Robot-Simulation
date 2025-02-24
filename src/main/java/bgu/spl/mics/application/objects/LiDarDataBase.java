package bgu.spl.mics.application.objects;

import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

/**
 * LiDarDataBase is a singleton class responsible for managing LiDAR data.
 * It provides access to cloud point data and other relevant information for tracked objects.
 */
public class LiDarDataBase {

   private AtomicInteger counter;
    private final List<StampedCloudPoints> cloudPoints;

    // Private Constructor for singleton pattern
    private LiDarDataBase(String filePath) {
        this.cloudPoints = loadCloudPointsFromFile(filePath);
        counter = new AtomicInteger(0);
    }

    private static class SingletonHolder {
        private static LiDarDataBase instance = null;
    }
        /**
     * Returns the singleton instance of LiDarDataBase.
     * 
     * @param filePath The path to the LiDAR data JSON file.
     * @return The singleton instance.
     */
    public static LiDarDataBase getInstance(String filePath) {
        if (SingletonHolder.instance == null) {
            SingletonHolder.instance = new LiDarDataBase(filePath);
        }
        return SingletonHolder.instance;
    }


    /**
     * Returns the list of all StampedCloudPoints.
     *
     * @return The list of StampedCloudPoints.
     */
    public List<StampedCloudPoints> getCloudPoints() {
        return cloudPoints;
    }
    

    private List<StampedCloudPoints> loadCloudPointsFromFile(String filePath) {
        Gson gson = new Gson();
        try (FileReader reader = new FileReader(filePath)) {
            Type type = new TypeToken<List<StampedCloudPoints>>() {}.getType();
            return gson.fromJson(reader, type);
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public void incCounter() {
        this.counter.incrementAndGet();
    }

    public boolean getFinished() {
        return counter.get() == cloudPoints.size();
    }

    public int getCounter() {
        return counter.get();
    }   
}
