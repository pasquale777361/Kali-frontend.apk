# Kali Linux Android GUI

## Description

This repository contains the source code for an Android application that serves as a graphical user interface (GUI) for a remote Kali Linux server. The app allows you to connect to your server via SSH, execute common Kali tools from a predefined list, and view the command output directly on your Android device in real-time.

### Features

*   **SSH Connectivity:** Securely connect to your Kali Linux server using SSH credentials.
*   **Tool Library:** Access a list of common Kali tools (Nmap, Nikto, Dirb, etc.) from a user-friendly interface.
*   **Dynamic Command Execution:** For tools that require arguments (like a target IP address), the app will prompt you to enter them before execution.
*   **Real-time Output:** View the output of running commands as it is generated, which is ideal for long-running tasks.
*   **Session Management:** The UI dynamically updates to show the current connection status, and you can easily connect and disconnect from your server.

## How to Build the APK

To build the APK from the source code, you will need [Android Studio](https://developer.android.com/studio).

1.  **Clone the repository:**
    ```bash
    git clone <repository-url>
    ```

2.  **Open the project in Android Studio:**
    *   Launch Android Studio.
    *   Select `File > Open` from the menu bar.
    *   Navigate to the directory where you cloned the repository and select it.
    *   Android Studio will import the project and download the necessary dependencies (as defined in `app/build.gradle`).

3.  **Build the APK:**
    *   Once the project has been synced, select `Build > Build Bundle(s) / APK(s) > Build APK(s)` from the menu bar.
    *   Android Studio will start the build process.
    *   When the build is complete, a notification will appear in the bottom-right corner of the window. Click the **locate** link in the notification to find the generated APK file. The file is typically located in `app/build/outputs/apk/debug/`.

4.  **Install the APK:**
    *   You can now transfer the generated APK file to your Android device and install it. Make sure you have enabled installation from unknown sources in your device's security settings.