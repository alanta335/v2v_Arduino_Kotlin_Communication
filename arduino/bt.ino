#include<unistd.h>
#include<SoftwareSerial.h>
#include<string.h>

char inputByte;

 SoftwareSerial hc05(0,1);

char recieve[100]= "";
#define led 13
void setup() {
 Serial.begin(9600);
 pinMode(13,OUTPUT);
  hc05.begin(9600);
}

void loop() {
  int i = 0;
  int flag = false;
  digitalWrite(led, 1);
  char s[] = "hello from arduino";
  
  while(i<strlen(s))
  {
    hc05.write((int)s[i]);
    i++;
  }
  i = 0;
while(hc05.available()>0){
  inputByte= hc05.read(); 
  recieve[i++] = (char)inputByte;
  flag = true;
  hc05.flush();
  delay(20);
  }
  digitalWrite(led, 0);
  //recieve[i] = '\0';
  if(flag){
  
  Serial.println(recieve);
  Serial.println(strlen(recieve));
  memset(recieve, '\0', sizeof(recieve));

  }
}