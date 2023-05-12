// #include <SPI.h>  
// #include "RF24.h" 

// RF2 \         4 myRadio (7, 8); 
// struct package
// {
//   int id=0;
//   char  text[100] ="empty";
// };

// byte addresses[][6] = {"0"}; 



// typedef struct package Package;
// Package data;

// void setup() 
// {
//   Serial.begin(115200);
//   delay(1000);

//   myRadio.begin(); 
//   myRadio.setChannel(125); 
//   myRadio.setPALevel(RF24_PA_MAX);
//   myRadio.setDataRate( RF24_250KBPS ) ; 
//   myRadio.openReadingPipe(1, addresses[0]);
//   myRadio.startListening();
// }


// void loop()  
// {

//   if ( myRadio.available()) 
//   {
//     while (myRadio.available())
//     {
//       myRadio.read( &data, sizeof(data) );
//     }
//     Serial.print("\---:");
//     Serial.print(data.id);
//     Serial.print("\n");
//     Serial.println(data.text);
//   }

// }
