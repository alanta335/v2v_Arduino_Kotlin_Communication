#include<unistd.h>
#include<SoftwareSerial.h>
#include<string.h>
#include <ArduinoJson.h>
#include "jsonWriteSerial.h"
#include <SPI.h>  
#include "RF24.h" 

int input;
RF24 myRadio (7, 8); 
byte addresses[][6] = {"012345"}; 

const byte numChars = 128;
char recievedChars[numChars];
char tempChars[numChars];
int statusCode = 0;
SoftwareSerial hc05(5,6);
boolean newData = false;
static boolean sending = false;
static boolean recieving = false;

struct sosDevices{
  double lat;
  double longi;
  char deviceID[32]; 
};
struct sosDevices sos[10] , selfDevice;

int no_of_devices = 0;
bool selfSOS = false;
void setup() {

  Serial.begin(9600);
  Serial.println("Listening and repeating data");
  hc05.begin(9600);

  myRadio.begin();  
  myRadio.setChannel(115); 
  myRadio.setPALevel(RF24_PA_MAX);
  myRadio.setDataRate( RF24_250KBPS ) ; 
  myRadio.openWritingPipe( addresses[0]);
  myRadio.openReadingPipe(1, addresses[0]);
  myRadio.startListening();
}

void recieveAsBytes()
{
  
  static byte index = 0;
  char startMarker = '<';
  char endMarker = '>';
  char rec;

  if(!sending){
    while(hc05.available()>0 && newData == false)
    {
      rec = hc05.read();
      if(recieving == true)
      {
        if(rec!=endMarker)
        {
          recievedChars[index] = rec;
          index++;
          if(index >= numChars){
            index = numChars -1;
          }
        }
        else
        {
          recievedChars[index] = '\0';
          recieving = false;
          index = 0;
          newData = true;
          //Serial.println(recievedChars);
        }
      }
      else if(rec == startMarker)
      {
        recieving = true;
      }

    }
    }
  
}
void parseData() {      // split the data into its parts
    DynamicJsonDocument device(512);
    // Serial.println(recievedChars);
    deserializeJson(device, recievedChars);
    char devID[21];
    strcpy(devID , device["device-id"]);
    // if(!findDeviceInSOS(devID))
    // {
    //   addDevicesToSOS(device);
    //   Serial.print("No of devices: "); Serial.println(no_of_devices);
    // }
    selfDevice.lat = json["latitude"];
    selfDevice.longi = json["longitude"];
    strcpy(selfDevice.deviceID ,  json["device-id"]);
    selfSOS = true;
    if(sendJsonViaBT(device, Serial, hc05))
      Serial.println("Sent the JSON");
    else
      Serial.println("Error sending the JSON");
}
bool findDeviceInSOS(char *deviceID)
{
  for(int i = 0; i< no_of_devices; i++) {
    if(strcmp(deviceID, sos[no_of_devices].deviceID))
    {
      return true;
    }
  }
  return false;
}

void addDevicesToSOS(DynamicJsonDocument &json){
  sos[no_of_devices].lat = json["latitude"];
    sos[no_of_devices].longi = json["longitude"];
    strcpy(sos[no_of_devices].deviceID ,  json["device-id"]);
    no_of_devices++;
}
void broadcast()
{
  if(selfSOS) {
    myRadio.write(&selfDevice, sizeof(selfDevice)); 
    Serial.print("\nPackage:",selfDevice.deviceID);
  }
  else return;
    
}

void recieveFromOther(){
  if ( myRadio.available()) 
  {
    for(int o=0;o<25;o++)
    {
      myRadio.read(&data, sizeof(data) );
    }
    Serial.println(data.);
  }
}

void loop() {
  recieveAsBytes();
  if(newData){
    strcpy(tempChars, recievedChars);
    //send();
    parseData();
    broadcast();
    newData = false;
  }
  recieveFromOther();
}