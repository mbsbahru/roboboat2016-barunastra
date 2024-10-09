# 9th Roboboat 2016 Barunastra Android
This Android project was developed for the 9th International Roboboat Competition 2016 during my time with the Barunastra Roboboat ITS team. Initially built using Eclipse IDE, the project has now been transitioned to work with Android Studio IDE.

**Barunastra Roboboat ITS Team 2016**: Rudy D., Darwin S., Yohan P., Muhammad Bahru Sholahuddin, Ericza D. S., Anas M. N.

![Barunastra Roboboat 2016 Preview](https://github.com/mbsbahru/roboboat2016-barunastra/blob/main/figures/project_barunastra.png)

## Project Structure
The project is structured with source files, resources, and configuration files for both Eclipse IDE and Android Studio IDE. Below is an overview of the key components:

### Android Studio IDE
```bash
.
├── android-hsv-pick/android-studio-project/app
│   ├── src
│   │   ├── main
│   │   │   ├── AndroidManifest.xml
│   │   │   ├── res
│   │   │   │   ├── layout
│   │   │   │   ├── biner11.xml
│   │   │   │   ├── ...
│   │   │   ├── java/com/mbsbahru/roboboat2016_barunastra
│   │   │   │   ├── BlobDetector.java
│   │   │   │   ├── BluetoothConf.java
│   │   │   │   ├── MainActivity.java
│   │   │   │   ├── ...
```

### Eclipse IDE
```bash
.
├── android-hsv-pick/eclipse-ide-project
│   ├── AndroidManifest.xml
│   ├── res
│   │   ├── layout
│   │   │   ├── biner11.xml
│   │   │   ├── ...
│   ├── src/mbs/RVAllMission12345
│   │   ├── BlobDetector.java
│   │   ├── BluetoothConf.java
│   │   ├── MainActivity.java
│   │   ├── ...
```

## Android Studio Setup

To set up the project, download Android Studio IDE [here](https://developer.android.com/studio) and build the project using Java for the backend and XML for the frontend.

### Dependencies
- **OpenCV 4.1.0**: Required for computer vision functionalities. Download [here](https://sourceforge.net/projects/opencvlibrary/files/4.1.0/opencv-4.1.0-android-sdk.zip/download).

### Building the Project

1. Download or clone the repository.
2. Open the project in Android Studio (`android-studio-project`).
3. Build the project: **Build** → **Rebuild Project**.
4. If successful, connect an Android device with USB debugging enabled, and press **Run**.
5. If build errors occur, ensure that OpenCV is properly integrated by following the instructions in the **Installations** section.

### Installing the APK

- Download the `.apk` [here](https://drive.google.com/file/d/1NhF9o5D2DPzwKQo5v9N8kOdDGCv6tQhN/view?usp=sharing).
- Install it on your device and grant necessary permissions in the app settings.

---

## The Roboboat 2016 Challenges

The Roboboat 2016 competition featured several key challenges, including **Navigation Test**, **Obstacle Avoidance**, **Automated Docking**, and **Acoustic Beacon Positioning**. Below are detailed descriptions of each challenge and how we approached them.

### Competition Overview
The mission consisted of multiple tasks including Navigation, Obstacle Avoidance, Docking, and Beacon Detection. Here’s an overview diagram of the competition:

![Roboboat Competition Overview](https://github.com/mbsbahru/roboboat2016-barunastra/blob/main/figures/irc_overview.png)

### Navigation Test
The vehicle must pass through a starting gate and a speed gate, demonstrating steady navigation and speed control. Each gate is made of two navigation buoys, spaced 6-10 feet apart. Points are earned by passing through these gates correctly.

![Navigation Test](https://github.com/mbsbahru/roboboat2016-barunastra/blob/main/figures/irc_navigation_test.png)

- `SeekBarVal11.java`: Detects the red gate buoy.
- `SeekBarVal12.java`: Detects the green gate buoy.

### Obstacle Avoidance
The vehicle must navigate through an obstacle field marked by buoys, passing through designated entrance and exit gates without touching the obstacles. Buoys are color-coded to distinguish between gates and obstacles, and failure to avoid obstacles results in task failure.

![Obstacle Avoidance](https://github.com/mbsbahru/roboboat2016-barunastra/blob/main/figures/irc_obstacle_avoidance.png)

- `SeekBarVal21.java`: Detects the red entrance/exit buoy.
- `SeekBarVal22.java`: Detects the white entrance/exit buoy.
- `SeekBarVal23.java`: Detects the green entrance/exit buoy.
- `SeekBarVal24.java`: Detects the yellow obstacle buoy.
- `SeekBarVal25.java`: Detects the black obstacle buoy.

### Automated Docking
The docking challenge requires the vehicle to locate and dock in a specific bay on a floating platform. Each docking bay is marked with a colored symbol, and the vehicle must dock in the correct sequence.

![Automated Docking](https://github.com/mbsbahru/roboboat2016-barunastra/blob/main/figures/irc_auto_docking.png)

- `SeekBarVal31.java`: Detects green docking symbols.
- `SeekBarVal32.java`: Detects red docking symbols.
- `SeekBarVal33.java`: Detects blue docking symbols.
- `SeekBarVal34.java`: Detects black docking symbols.

### Acoustic Beacon Positioning
This task involves detecting a buoy with an active underwater acoustic beacon. The vehicle must correctly identify and circle the buoy associated with the active beacon frequency.

![Acoustic Beacon Positioning](https://github.com/mbsbahru/roboboat2016-barunastra/blob/main/figures/irc_acoustic_beacon.png)

- `SeekBarVal41.java`: Detects the buoy associated with the active acoustic beacon.

---

Thank you for checking out this repository!
