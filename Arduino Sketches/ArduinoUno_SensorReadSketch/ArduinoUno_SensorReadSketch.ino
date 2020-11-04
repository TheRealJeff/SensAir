#include <Wire.h>
#include <SoftwareSerial.h>
#include "SparkFunCCS811.h" 

#define CCS811_ADDR 0x5B    // Default I2C Address 
#define MQ2_PIN A0          // Pins
#define HC05_RX 10
#define HC05_TX 11


SoftwareSerial btSerial(HC05_RX,HC05_TX);    //  RX,TX     
CCS811 ccs811(CCS811_ADDR);

void setup()
{
  Serial.begin(9600); 
  btSerial.begin(38400);  //Baud Rate for AT-command Mode.  
  Serial.println("|---------- Set Up ----------|"); 
  
  while(!Serial){;}
  Wire.begin(); //  Inialize I2C 

  if (ccs811.begin() == false)
  {
    Serial.print("CCS811 error. Please check wiring.");
  }
}

void loop()
{
  if (ccs811.dataAvailable())
  {
    ccs811.readAlgorithmResults();    // read results from CSS811
    int mq2_reading = analogRead(MQ2_PIN);    // read results from mq2

    int co2 = ccs811.getCO2();

    btSerial.write(co2);   
    btSerial.write("C");
    Serial.print(co2);
    Serial.println(" CO2 Measured");

    int tvoc = ccs811.getTVOC();   // print tvoc reading

    btSerial.write(tvoc);   
    btSerial.write("T");
    Serial.print(tvoc);
    Serial.println(" TVOC Measured");

    
    btSerial.write(mq2_reading);
    btSerial.write("M");
    Serial.print(mq2_reading);
    Serial.println(" MQ2 ppm Measured");

  }

//  if (btSerial.available())       // UNCOMMENT to allow for AT commands. Also uncomment all else in loop()
//    Serial.write(btSerial.read());
//
//  
//  if (Serial.available())
//    btSerial.write(Serial.read());
  
  delay(100); //Don't spam the I2C bus
}
