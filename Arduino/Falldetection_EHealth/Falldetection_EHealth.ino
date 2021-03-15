/* ========================================================================================
I2Cdev device library code is placed under the MIT license
Copyright (c) 2011 Jeff Rowberg

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
======================================================================================== */



#include <SPI.h>
#include <WiFiNINA.h> //Needs to be installed using Arduino library manager
#include "Wire.h"
#include "arduino_secrets.h" 

/******************************************************************************
 * Network related vars and consts                                            *
 ******************************************************************************/

//Arduino ID
int arduinoID = 2;

//WiFi Data
char ssid[] = SECRET_SSID;    //These SECRET_* constants are provided by the file arduino_secrets.h and should contain
char pass[] = SECRET_PASS;    //the exact SSID + the password as #define's. Example:
int status = WL_IDLE_STATUS;  //#define SECRET_SSID "MySSID"

//Init WiFiClient for http requests to FD-Server
WiFiClient client;

//Server Address
//char server[] = "http://lxvongobsthndl.ddns.net";
//Using IP Address instead of dynamic address since WiFiClientConnect doesn't seem to work with dns
IPAddress server(84,136,99,39);
int serverport = 3000;

/******************************************************************************
 * Gyroscope/Accelerometer related vars and consts                            *
 ******************************************************************************/

//I2C address of the MPU6050 module
const int MPU_ADDRESS = 0x68;

//Vars to hold sensor data
int16_t AcX, AcY, AcZ, Tmp, GyX, GyY, GyZ;
float ax = 0, ay = 0, az = 0, gx = 0, gy = 0, gz = 0;
//offsets calculated using "MPU6050 offset-finder" by Robert R. Fenichel, based on Jeff Rowberg's "MPU6050_RAW"
//See: https://github.com/jrowberg/i2cdevlib and legal notice on top
int16_t AcXoff = -1731, AcYoff = -2387, AcZoff = 1765, GyXoff = 95, GyYoff = 20, GyZoff = -11;

//Triggers 
boolean fall = false;     //stores if a fall has occurred
boolean trigger1 = false; //stores if first trigger (lower threshold) was activated
boolean trigger2 = false; //stores if second trigger (upper threshold) was activated
boolean trigger3 = false; //stores if third trigger (orientation change) was activated

//Trigger counters
byte trigger1count = 0; //stores the counts past, since trigger 1 was set true
byte trigger2count = 0; //stores the counts past, since trigger 2 was set true
byte trigger3count = 0; //stores the counts past, since trigger 3 was set true
int angleChange = 0;


/******************************************************************************
 * SETUP                                                                      *
 ******************************************************************************/
void setup() {
  
  //INIT gyro
  Wire.begin();
  Wire.beginTransmission(MPU_ADDRESS);
  Wire.write(0x6B);
  Wire.write(0);      //wake up MPU6050
  Wire.endTransmission(true);

  //open serial for logging
  Serial.begin(9600);

  //wait for serial to open.
  //Remove this in a prod env, since the Serial will never open in a not dev env!
  while (!Serial);

  //Check Wifi Module
  if (WiFi.status() == WL_NO_MODULE) {
    Serial.println("Communication with WiFi module failed!");
    while(true); //Endless loop -> do not continue
  }

  //Check WiFi Module Firmware
  String fv = WiFi.firmwareVersion();
  if (fv < WIFI_FIRMWARE_LATEST_VERSION) {
    Serial.println("Firmware outdated! Please upgrade the firmware!");
  }

  //Attempt to connect to wifi network.
  while (status != WL_CONNECTED) {
    Serial.print("Attempting to connect to SSID: ");
    Serial.println(ssid);
    //Connect
    status = WiFi.begin(ssid, pass);
    //wait a bit for connection to establish
    delay(10000);
  }

  //ARDUINO NOW CONNECTED TO WIFI

  Serial.println("Connected!");
  printCurrentNet();
  printWifiData();

  Serial.println("---------------------------------------------------------------");
}

/******************************************************************************
 * LOOP                                                                       *
 ******************************************************************************/
void loop() {
  
  // debug wifi client
  while (client.available()) {
    char c = client.read();
    Serial.write(c);
  }

  //Read sensor
  mpuRead();
  //Apply offsets: 
  ax = (AcX + AcXoff) / 16384.00;
  ay = (AcY + AcYoff) / 16384.00;
  az = (AcZ + AcZoff) / 16384.00;
  gx = (GyX + GyXoff) / 131.07;
  gy = (GyY + GyYoff) / 131.07;
  gz = (GyZ + GyZoff) / 131.07;


  //calc amplitute vector for 3 axis
  float Raw_AM = pow(pow(ax, 2) + pow(ay, 2) + pow(az, 2), 0.5);
  //Value of Raw_AM is within 0 to 1 so we multiply it by 10 to be able to use it easily in if else conditions 
  int AM = Raw_AM * 10;
  //LOG to serial
  Serial.println(AM);
  
  if (trigger3==true) {
    trigger3count++;
    //LOG to serial
    Serial.println(trigger3count);
    if (trigger3count >= 10) { 
      angleChange = pow(pow(gx, 2) + pow(gy, 2) + pow(gz, 2), 0.5);
      //LOG to serial
      Serial.println(angleChange); 
      //If orientation changes remain between 0-10 degrees
      if ((angleChange >= 0) && (angleChange <= 10)) { //if orientation changes remains between 0-10 degrees
        fall = true;
        trigger3 = false;
        trigger3count = 0;
      }
      else { //user regained normal orientation
        trigger3 = false; 
        trigger3count = 0;
        Serial.println("TRIGGER 3 DEACTIVATED");
      }
    }
  }
  //If a fall has been detected
  if (fall == true) { 
    Serial.println("FALL DETECTED");
    //Send Alert
    alert();
    fall = false;
  }
  if (trigger2count >= 6) { //allow 0.5s for orientation change
    trigger2 = false; 
    trigger2count = 0;
    Serial.println("TRIGGER 2 DECACTIVATED");
  }
  if (trigger1count >= 6) { //allow 0.5s for AM to break upper threshold
    trigger1 = false;
    trigger1count = 0;
    Serial.println("TRIGGER 1 DECACTIVATED");
  }
  if (trigger2 == true) {
    trigger2count++;
    angleChange = pow(pow(gx, 2) + pow(gy, 2) + pow(gz, 2), 0.5);
    Serial.println(angleChange);
    if (angleChange >= 30 && angleChange <= 400) {
      trigger3 = true;
      trigger2 = false;
      trigger2count = 0;
      Serial.println(angleChange);
      Serial.println("TRIGGER 3 ACTIVATED");
    }
  }
  if (trigger1 == true) {
    trigger1count++;
    if (AM >= 12) { //if AM breaks upper threshold (3g)
      trigger2 = true;
      Serial.println("TRIGGER 2 ACTIVATED");
      trigger1 = false; 
      trigger1count = 0;
    }
  }
  if (AM <= 2 && trigger2 == false) { //if AM breaks lower threshold (0.4g)
    trigger1 = true;
    Serial.println("TRIGGER 1 ACTIVATED");
  }
  
  //delay is needed in order to not overload the port
  delay(100);
}


/******************************************************************************
 * FUNCTIONS                                                                  *
 ******************************************************************************/

/**
 * Reads data from MPU sensor and stores it in vars declared at the beginning
 * of this file.
 */
void mpuRead() {
  Wire.beginTransmission(MPU_ADDRESS);
  Wire.write(0x3B);                             // starting with register 0x3B (ACCEL_XOUT_H)
  Wire.endTransmission(false);
  Wire.requestFrom(MPU_ADDRESS, 14, true);      // requesting a total of 14 registers
  AcX = Wire.read() << 8 | Wire.read();         // 0x3B (ACCEL_XOUT_H) & 0x3C (ACCEL_XOUT_L)    
  AcY = Wire.read() << 8 | Wire.read();         // 0x3D (ACCEL_YOUT_H) & 0x3E (ACCEL_YOUT_L)
  AcZ = Wire.read() << 8 | Wire.read();         // 0x3F (ACCEL_ZOUT_H) & 0x40 (ACCEL_ZOUT_L)
  Tmp = Wire.read() << 8 | Wire.read();         // 0x41 (TEMP_OUT_H) & 0x42 (TEMP_OUT_L)
  GyX = Wire.read() << 8 | Wire.read();         // 0x43 (GYRO_XOUT_H) & 0x44 (GYRO_XOUT_L)
  GyY = Wire.read() << 8 | Wire.read();         // 0x45 (GYRO_YOUT_H) & 0x46 (GYRO_YOUT_L)
  GyZ = Wire.read() << 8 | Wire.read();         // 0x47 (GYRO_ZOUT_H) & 0x48 (GYRO_ZOUT_L)
}

/**
 * Sends a http Post request to the /alert endpoint of FD-Server
 */
void alert() {
  //close existing connection
  client.stop();

  String bodyData = "{ \"arduinoID\": " + String(arduinoID) + " }";

  if (client.connect(server, serverport)) {
    Serial.println("Connecting...");

    client.println("POST /alert HTTP/1.1");
    client.println("Content-Type: application/json");
    client.println("User-Agent: ArduinoWiFi/1.1");
    client.println("Host: lxvongobsthndl.ddns.net:3000");
    client.println("Connection: keep-alive");
    client.print("Content-Length: ");
    client.println(bodyData.length());
    client.println();
    client.println(bodyData);
    client.println();

    Serial.println("Alert sent!");
  }
  else {
    Serial.println("Connection to Backend failed!");
  }
}

/** 
 *  Prints data about the Wifi connection to Serial
 */
void printWifiData() {
  //print board's IP address
  IPAddress ip = WiFi.localIP();
  Serial.print("IP Address: ");
  Serial.println(ip);

  //print board's MAC address
  byte mac[6];
  WiFi.macAddress(mac);
  Serial.print("MAC address: ");
  printMacAddress(mac);
}

/**
 * Prints data about the connected network to Serial
 */
void printCurrentNet() {
  //print the SSID of the network board's connected to
  Serial.print("SSID: ");
  Serial.println(WiFi.SSID());

  //print the MAC address of the router board's connected to
  byte bssid[6];
  WiFi.BSSID(bssid);
  Serial.print("BSSID: ");
  printMacAddress(bssid);

  //print the received signal strength
  long rssi = WiFi.RSSI();
  Serial.print("signal strength (RSSI):");
  Serial.println(rssi);

  //print the encryption type
  byte encryption = WiFi.encryptionType();
  Serial.print("Encryption Type:");
  Serial.println(encryption, HEX);
  
  Serial.println();
}

/**
 * Prints a MAC address to Serial
 * @args byte mac[] - the mac address to be printed in byte array format
 */
void printMacAddress(byte mac[]) {
  for (int i = 5; i >= 0; i--) {
    if (mac[i] < 16) {
      Serial.print("0");
    }

    Serial.print(mac[i], HEX);
    
    if (i > 0) {
      Serial.print(":");
    }
  }
  Serial.println();
}
