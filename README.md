# 🤖 Smart Vacuum Robot Simulation
**Concurrent Perception & Mapping System – SPL225 @ BGU**

A modular simulation of an autonomous vacuum robot’s perception and mapping system, developed as part of the **Systems Programming Course** at Ben-Gurion University.  
The system models real-time sensor fusion using **multithreaded Java Microservices** and a custom **MessageBus framework**.

---

## 🔧 Technologies Used
- **Java 8** – Core implementation using threads, lambdas, and generics  
- **Custom MessageBus** – Publish/subscribe, event routing & futures  
- **Multithreading & Synchronization** – Thread-per-service model  
- **GSON** – For JSON parsing of input sensor data  
- **JUnit** – Test-driven development for core components  
- **Maven** – Build automation and dependency resolution  
- **Docker-compatible** – Tested in isolated environments  

---

## 💡 Project Structure

### `bgu.spl.mics.application`
- **`messages/`**  
  Contains all system messages (Events and Broadcasts) used in the pub/sub framework:
  - `DetectObjectsEvent.java`
  - `TrackedObjectsEvent.java`
  - `PoseEvent.java`
  - `TickBroadcast.java`
  - `CrashedBroadcast.java`, `TerminatedBroadcast.java`

- **`objects/`**  
  Data representations of sensors, tracked objects, the robot pose, and statistics:
  - `Camera.java`, `LiDarWorkerTracker.java`, `GPSIMU.java`
  - `DetectedObject.java`, `TrackedObject.java`, `LandMark.java`
  - `StampedCloudPoints.java`, `StampedDetectedObjects.java`
  - `FusionSlam.java`, `Pose.java`, `CloudPoint.java`, `StatisticalFolder.java`
  - `STATUS.java`, `LiDarDataBase.java`

- **`services/`**  
  Microservice implementations:
  - `CameraService.java`
  - `LiDarService.java`
  - `FusionSlamService.java`

---

## ✨ Features
- Fully event-driven concurrent design  
- Global system timer using `TickBroadcast`  
- Real-time pose-aware mapping with coordinate transformation  
- Supports multiple sensors with different frequencies  
- Fault detection & emergency shutdown using broadcast messages  
- Generates a final JSON output containing runtime statistics, detected landmarks, and crash report (if applicable)  

---

## 🚀 How to Run

### 1. Build
```bash
mvn clean compile
```

### 2. Run Tests
```bash
mvn test
```

### 3. Run Application
To run the simulation, provide the path to a valid configuration JSON file:
```bash
mvn exec:java -Dexec.mainClass=bgu.spl.mics.application.GurionRockRunner -Dexec.args="/path/to/configuration_file.json"
```

---

## 📄 Output Description
Generates an `output_file.json` in the same directory as the configuration file, containing:
- Runtime statistics  
- Final map (landmarks)  
- Crash report (if applicable)

---

## 🧪 Testing
```bash
mvn test
```
Includes unit tests for core components:
- `MessageBusImpl` – Publish/subscribe mechanisms and future resolution
- `FusionSlam` – Landmark transformation and pose estimation
- Sensor services (`CameraService`, `LiDarService`) – Message handling and event generation

---

## 📁 Directory Structure
```
src/
└── main/
    └── java/
        └── bgu/
            └── spl/
                └── mics/
                    └── application/
                        ├── messages/
                        ├── objects/
                        └── services/
```

---

## 🔗 Example Configuration
For testing the system, several input sets are provided under the following folders:
- `example_input/`
- `example_input_2/`
- `example_input_with_error/`

Each folder contains a `configuration_file.json` and supporting sensor data (`camera_data.json`, `lidar_data.json`, `pose_data.json`, etc.).

---

## 📚 Course Information
- **Course:** SPL225 – Systems Programming Lab  
- **Institution:** Ben-Gurion University of the Negev  
- **Year:** 2025  
- **Environment:** Linux CS Lab, Docker-compatible  

---

## 🧑‍💻 Authors

**Ben Kapon**  
Student at BGU  
[LinkedIn](https://www.linkedin.com/in/ben-kapon1/)

**Itay Shaul**  
Student at BGU  
[LinkedIn](https://www.linkedin.com/in/itay-shaul/)

---

## 📝 Important Note
This project was designed and tested on a **Docker-compatible environment**.  
Ensure file paths and JSON formats are valid when running in local or CI environments.

> This project demonstrates real-time sensor integration using advanced concurrent programming techniques in Java.

