# v2v_Arduino_Kotlin_Communication

App used to detect traffic congestion and SOS message broadcasting without network connectivity using Arduino.

This is my final year B.tech poject

# Android Application README


## Prerequisites
Before you begin, make sure you have the following software installed on your machine:
- Android Studio: The official integrated development environment (IDE) for Android app development. You can download it from the official [Android Studio website](https://developer.android.com/studio).
- Java Development Kit (JDK): Android Studio requires JDK to be installed. You can download the latest JDK from the [Oracle website](https://www.oracle.com/java/technologies/javase-jdk11-downloads.html).

## Getting Started

1. Clone or download the project repository to your local machine from github https://github.com/alanta335/v2v_Arduino_Kotlin_Communication.git.

2. Launch Android Studio and open the project by selecting "Open an existing Android Studio project" and navigating to the project's root directory.

3. Once the project is loaded, Android Studio will take care of downloading and syncing the necessary dependencies.

## Building the Application

To build the Android application, follow these steps:

1. Click on the **Build** menu in the Android Studio toolbar.

2. From the drop-down menu, select **Make Project** or press `Ctrl + F9` (Windows/Linux) or `Cmd + F9` (macOS).

3. Android Studio will compile your code, package the app, and generate an APK file.

4. In the file MapsActivity.kt located in projectFolder/app/src/main/java/com/example/gmap/MapsActivity.kt change the mac address of the bluetooth module to be connected before running in line 204.

5. In the file strings.xml located in projectFolder/app/src/main/res/values/strings.xml change the baseURL to the url of server hosted before running in line 10.

6. Add the google map api key created from google cloud website in the file app/src/main/AndroidManifest.xml.

## Running the Application

To run the Android application on an emulator or a physical device, follow these steps:

1. Make sure you have an Android Virtual Device (AVD) configured in Android Studio, or connect a physical Android device to your computer using a USB cable and enable USB debugging.

2. Click on the **Run** menu in the Android Studio toolbar.

3. From the drop-down menu, select **Run 'app'** or press `Shift + F10` (Windows/Linux) or `Ctrl + R` (macOS).

4. Android Studio will build the project (if necessary), deploy the APK to the selected device, and launch the application.

5. The application should now be running on the selected device, whether it's an emulator or a physical device.

# Arduino Code README

This README file provides instructions on how to run an Arduino code sketch on an Arduino board.

## Prerequisites
Before you begin, make sure you have the following software and hardware requirements:
- Arduino IDE: The official integrated development environment for Arduino. You can download it from the official [Arduino website](https://www.arduino.cc/en/software).
- Arduino board: Make sure you have an Arduino board compatible with the code you want to run. Common boards include Arduino Uno, Arduino Nano, Arduino Mega, etc.
- USB cable: To connect the Arduino board to your computer.

## Getting Started

1. Install the Arduino IDE on your computer by following the installation instructions provided on the Arduino website.

2. Connect your Arduino board to your computer using the USB cable.

3. Launch the Arduino IDE.

4. In the Arduino IDE, select the appropriate board model from the **Tools > Board** menu. Choose the board that matches the one you have connected.

5. Select the correct port for your Arduino board from the **Tools > Port** menu. If you are unsure which port to select, you can check the available ports in the **Device Manager** (Windows) or **System Information** (macOS).

## Uploading the Code

To upload the Arduino code sketch to your Arduino board, follow these steps:

1. Open the Arduino code sketch file (usually with a `.ino` extension) in the Arduino IDE. It can be located in projectFolder/arduino/bt/bt.ino .

2. Verify that the code compiles without any errors by clicking on the **Verify** button (checkmark icon) or by selecting **Sketch > Verify/Compile** from the menu. If there are any errors, review the error messages and make the necessary corrections in the code.

3. Once the code is successfully compiled, click on the **Upload** button (right arrow icon) or select **Sketch > Upload** from the menu. The Arduino IDE will compile the code again and upload it to the connected Arduino board.

4. Wait for the upload process to complete. You can monitor the progress in the status bar at the bottom of the Arduino IDE.

5. Once the upload is finished, the Arduino IDE will display a "Done uploading" message. The code is now running on your Arduino board.

## Serial Monitor

To interact with the Arduino board and view output from your code, you can use the Serial Monitor in the Arduino IDE. Here's how to use it:

1. With the Arduino board connected and the code running, click on the **Serial Monitor** button (magnifying glass icon) or select **Tools > Serial Monitor** from the menu.

2. Set the baud rate to match the one specified in your code. The default baud rate is usually 9600.

3. The Serial Monitor window will open, allowing you to send and receive data to and from the Arduino board. You can send commands, view sensor readings, or debug output.

4. To close the Serial Monitor, simply click on the close button (X) in the top-right corner of the window.

# MongoDB Node Connection - V2V Arduino Kotlin Communication Server-Side

This README file provides instructions on how to run the server-side code for the MongoDB Node Connection - V2V Arduino Kotlin Communication project.

## Prerequisites
Before you begin, make sure you have the following software and hardware requirements:

### Software Requirements
- Node.js: The server-side code is written in Node.js, so you need to have Node.js installed on your machine. You can download it from the official [Node.js website](https://nodejs.org).

### Hardware Requirements
- Arduino Board: To connect Arduino devices and send/receive data.
- Internet Connection: The server-side code requires an internet connection to communicate with the MongoDB database and other devices.

## Getting Started

1. Clone or download the project repository to your local machine. You can do this by running the following command in your terminal:

```bash
git clone https://github.com/alanta335/mongodb_node_connection-v2v_Arduino_Kotlin_Communication-server-side.git
```

2. Navigate to the project's root directory in your terminal:

```bash
cd mongodb_node_connection-v2v_Arduino_Kotlin_Communication-server-side
```

3. Install the project dependencies by running the following command:

```bash
npm install
```

## Configuration

Before running the server-side code, you need to configure your MongoDB database connection. Follow these steps:

1. Open the `config.js` file located in the project's root directory.

2. Replace the placeholder values in the `mongoURI` variable with your MongoDB connection string. You can obtain this connection string from your MongoDB provider.

## Running the Server

To run the server-side code, follow these steps:

1. In the project's root directory, run the following command:

```bash
npm start
```

2. The server will start running and will connect to the MongoDB database using the provided connection string.

3. You should see a message in the console saying "Server running on port 3000" or a similar message indicating that the server has started successfully.

4. The server is now ready to receive requests and communicate with Arduino devices and the Kotlin client.

## API Endpoints

The server exposes several API endpoints to interact with the MongoDB database and handle the V2V communication. Here are the main endpoints:

- **GET /api/devices**: Retrieves all devices from the database.
- **GET /api/devices/:id**: Retrieves a specific device by its ID.
- **POST /api/devices**: Creates a new device.
- **PUT /api/devices/:id**: Updates a specific device by its ID.
- **DELETE /api/devices/:id**: Deletes a specific device by its ID.

You can test these endpoints using tools like Postman or by making HTTP requests from your Kotlin client.

# Ngrok README

This README file provides instructions on how to run and use Ngrok, a tool for creating secure tunnels to expose local servers to the internet.

## Prerequisites
Before you begin, make sure you have the following software requirements:

- Ngrok: You can download Ngrok from the official [Ngrok website](https://ngrok.com/download).

## Getting Started

1. Download Ngrok from the official website based on your operating system.

2. Extract the downloaded archive to a directory of your choice.

## Running Ngrok

To run Ngrok and expose your local server to the internet, follow these steps:

1. Open a terminal or command prompt.

2. Navigate to the directory where you extracted Ngrok.

3. To start Ngrok and create a secure tunnel to your local server, run the following command:

```bash
./ngrok http <port>
```

Replace `<port>` with the port number on which your local server is running. For example, if your local server is running on port 3000, the command would be:

```bash
./ngrok http 3000
```

4. Ngrok will start and display a forwarding URL that you can use to access your local server from the internet. The URL will typically be in the format `http://<random_subdomain>.ngrok.io` or `https://<random_subdomain>.ngrok.io`.

5. Copy the forwarding URL and use it in your Kotlin app the string.xml file mentioned in the Android application readme.
