#include<unistd.h>
#include<SoftwareSerial.h>
#include<string.h>

int input;
int device = A1;
const byte numChars = 32;

char receivedChars[numChars];
char tempChars[numChars];
char message[numChars] = {0};
int statusCode = 0;
SoftwareSerial hc05(10,11);
boolean newData = false;
static boolean sending = false;

static boolean recieving = false;

void setup() {
 Serial.begin(9600);
 Serial.println("Listening and repeating data");
  hc05.begin(9600);
}
void send()
{
    sending = true;
    if(!recieving)
    {
        Serial.println("sending string");
        char x[] = "<ala>";
        
        for(int i = 0; i< strlen(x); i++)
        {
          hc05.write((byte)x[i]);
        }
    }
    sending = false;
}

void recieve()
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
          receivedChars[index] = rec;
          index++;
          if(index >= numChars){
            index = numChars -1;
          }
        }
        else
        {
          receivedChars[index] = "\0";
          recieving = false;
          index = 0;
          newData = true;
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

    char * strtokIndx; // this is used by strtok() as an index

    strtokIndx = strtok(tempChars,",");      // get the first part - the string
    strcpy(message, strtokIndx); // copy it to messageFromPC
 
    strtokIndx = strtok(NULL, ",");
    statusCode = atoi(strtokIndx);    
}
void showParsedData() {
    Serial.print("Message ");
    Serial.println(message);
    Serial.print("Status ");
    Serial.println(statusCode);
}
void loop() {
  recieve();
  if(newData){
    strcpy(tempChars, receivedChars);
    send();
    parseData();
    showParsedData();
    newData = false;
  }
}