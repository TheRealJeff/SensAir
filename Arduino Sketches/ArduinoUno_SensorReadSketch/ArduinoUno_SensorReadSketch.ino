#include <Wire.h>
#include <SoftwareSerial.h>
#include "SparkFunCCS811.h" 

#define CCS811_ADDR 0x5B    // Default I2C Address 
#define MQ2_PIN A0          // Pins
#define RX 2
#define TX 3


SoftwareSerial btSerial(RX,TX);    //  RX,TX     
CCS811 ccs811(CCS811_ADDR);

void setup()
{
  pinMode(LED_BUILTIN, OUTPUT);
  digitalWrite(LED_BUILTIN, LOW);

  Serial.begin(9600); 
  btSerial.begin(38400);  //Baud Rate for AT-command Mode.  
  Serial.println("|---------- Set Up ----------"); 
  
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

    int co2 = ccs811.getCO2();
    Serial.print(co2);    // print co2 reading
    Serial.print("C");
    btSerial.print(co2);   
    btSerial.print("C");

    int tvoc = ccs811.getTVOC();   // print tvoc reading
    Serial.print(tvoc);   
    Serial.print("T");
    btSerial.print(tvoc);   
    btSerial.print("T");

    Serial.print(mq2_reading);    // print mq2 reading
    Serial.print("M");
    btSerial.print(mq2_reading);
    btSerial.print("M");
;
  }
    
 
// if (btSerial.available())       // uncomment to enter AT command mode 
//   Serial.write(btSerial.read()); 
// if (Serial.available()) 
//   btSerial.write(Serial.read());

  
  delay(100); //Don't spam the I2C bus
}
