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
char sosDev[216];
const byte numChars = 128;
char recievedChars[numChars];
char tempChars[numChars];
int statusCode = 0;

SoftwareSerial hc05(3,6);

boolean newData = false;
static boolean sending = false;
static boolean recieving = false;
struct sosDevices{
  double lat;
  double longi;
  char deviceID[32]; 
};
struct sosDevices sos[2] , selfDevice;
char res1[64];
char res2[32];

char res3[64];
char p1[32], p2[32];
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
    p2[0] = '~';
    // Serial.println(recievedChars);
    //strcpy(sosDev, recievedChars);
    for(int i = 0; i< 31; i++)
    {
      p1[i] = recievedChars[i];
      Serial.println(p1[i]);
      p2[i+1] = recievedChars[i+31];
    }
    p1[32]='\0';
    deserializeJson(device, recievedChars);
    char devID[21];
    strcpy(devID , device["did"]);
    selfDevice.lat = device["lat"];
    selfDevice.longi = device["long"];
    strcpy(selfDevice.deviceID ,  device["did"]);
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
  sos[no_of_devices].lat = json["lat"];
    sos[no_of_devices].longi = json["long"];
    strcpy(sos[no_of_devices].deviceID ,  json["did"]);
    no_of_devices++;
}
void broadcast()
{
  myRadio.openWritingPipe(addresses[0]);
  if(selfSOS) {
    myRadio.write(&p1, sizeof(p1)); 
    Serial.print("\nWrote Part 1");
    Serial.println(p1);
    myRadio.write(&p2, sizeof(p2));
    Serial.print("Wrote part 2");
    Serial.println(p2);
    //Serial.print(x);
  }
  myRadio.stopListening();
}

void recieveFromOther(){

  myRadio.openReadingPipe(1, addresses[0]);
  myRadio.startListening();
  Serial.print(myRadio.available());

      if ( myRadio.available()) 
      {

        myRadio.read(&res1, sizeof(res1));
        Serial.println(res1);
        if(res1[0]=='{')
        {
          myRadio.read(&res2, sizeof(res2));
          Serial.println(res2);
          if(res2[0]=='~')
            {
              strcat(res1, res2+1);
              Serial.print("res:");
              Serial.println(res1);
        
              DynamicJsonDocument dev1(512);
              deserializeJson(dev1,res1);
              sendJsonViaBT(dev1 ,Serial, hc05);
            }
            else return;
        }
        else return;
        
      }
      
    
  myRadio.stopListening();
}

void loop() {
  recieveAsBytes();
  if(newData){
    strcpy(tempChars, recievedChars);
    //send();
    parseData();
    newData = false;
  }

  broadcast();
  recieveFromOther();
}