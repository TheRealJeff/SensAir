#include <Wire.h>
#include <SoftwareSerial.h>
#include "SparkFunCCS811.h" 

#define CCS811_ADDR 0x5B    // Default I2C Address 
#define MQ2_PIN A0          // Pins
#define RX 10
#define TX 11


SoftwareSerial bt_serial(10,11);     
CCS811 ccs811(CCS811_ADDR);

void setup()
{
  pinMode(LED_BUILTIN, OUTPUT);
  digitalWrite(LED_BUILTIN, LOW);

  Serial.begin(9600); 
  bt_serial.begin(9600);  //Baud Rate for AT-command Mode.  
  Serial.println("***AT commands mode***"); 
  
  while(!Serial){;}
  Wire.begin(); //  Inialize I2C 

  if (ccs811.begin() == false)
  {
    Serial.print("CCS811 error. Please check wiring.");
    while (1)
      ;
  }
  
}

void loop()
{
  if (ccs811.dataAvailable())
  {
    ccs811.readAlgorithmResults();    // read results
    int mq2_reading = analogRead(MQ2_PIN);  

   
    Serial.print(ccs811.getCO2());
    Serial.println("C");
   
    Serial.print(ccs811.getTVOC());
    Serial.println("T");

    Serial.print(mq2_reading);
    Serial.println("M");
;
  }

  // Keep reading from HC-05 and send to Arduino Serial Monitor
    
  //from bluetooth to Terminal. 
 if (bt_serial.available()) 
   Serial.write(bt_serial.read()); 
 //from termial to bluetooth 
 if (Serial.available()) 
   bt_serial.write(Serial.read());

  
  delay(100); //Don't spam the I2C bus
}


void sendCommand(const char * command){
  Serial.print("Command send :");
  Serial.println(command);
  bt_serial.println(command);
  //wait some time
  delay(100);
  
  char reply[100];
  int i = 0;
  while (bt_serial.available()) {
    reply[i] = bt_serial.read();
    i += 1;
  }
  //end the string
  reply[i] = '\0';
  Serial.print(reply);
  Serial.println("Reply end");
}
