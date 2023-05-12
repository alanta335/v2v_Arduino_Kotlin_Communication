#include<ArduinoJson.h>

bool sendJsonViaBT(DynamicJsonDocument &dev , HardwareSerial &printSerial, SoftwareSerial &sendSerial)
{
  serializeJson(dev, printSerial);
  sendSerial.write('<');
  auto t = serializeJson(dev, sendSerial);
  Serial.print("\nSent the size:" );
  Serial.println(t);
  sendSerial.write('>');
  if (!t) return false;
  return true;
}

