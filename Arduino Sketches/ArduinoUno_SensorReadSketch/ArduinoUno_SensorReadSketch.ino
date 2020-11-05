#include <Wire.h>
#include <SoftwareSerial.h>
#include "SparkFunCCS811.h"
#include "SparkFunBME280.h" 

#define CCS811_ADDR 0x5B    // Default I2C Address 
#define MQ2_PIN A0          // Pins
#define HC05_RX 10
#define HC05_TX 11


SoftwareSerial btSerial(HC05_RX,HC05_TX);    //  RX,TX     
CCS811 ccs811(CCS811_ADDR);
BME280 bme280;
const float local_pressure = 101690;
float alt;

void setup()
{
  Serial.begin(9600); 
  btSerial.begin(38400);  //Baud Rate for AT-command Mode.  
  Serial.println("|---------- Set Up ----------|"); 
  
  while(!Serial){;}
  Wire.begin(); //  Inialize I2C 
  Wire.setClock(400000);

  bme280.beginI2C();

  if (ccs811.begin() == false)
    Serial.print("CCS811 error. Please check wiring.");
  if (bme280.beginI2C() == false)
    Serial.print("BME280 error. Please check wiring.");

  bme280.setReferencePressure(local_pressure);
  alt = bme280.readFloatAltitudeMeters();
  
  delay(10);
}

void loop()
{
//  if (ccs811.dataAvailable())
//  {
//    ccs811.readAlgorithmResults();    // read results from CSS811
//                                      // for each datapoint, we write it to bluetooth then print in serial
//    int co2 = ccs811.getCO2();        // co2
//      btSerial.write(co2);   
//      btSerial.write('C');
//      Serial.print("CO2:\t\t");
//      Serial.print(co2);
//      Serial.println(" ppm");
//      
//  
//    int tvoc = ccs811.getTVOC();    // TVOC
//      btSerial.write(tvoc);   
//      btSerial.write('V');
//      Serial.print("TVOC:\t\t");
//      Serial.print(tvoc);
//      Serial.println(" ppb");
//
//    
//    int mq2_reading = analogRead(MQ2_PIN);  // MQ2   
//      btSerial.write(mq2_reading);
//      btSerial.write('M');
//      Serial.print("MQ2:\t\t");
//      Serial.print(mq2_reading);
//      Serial.println(" ppb");
//
//    float humidity = bme280.readFloatHumidity();  // BME280 humidity
//      btSerial.write(humidity);   
//      btSerial.write('H');
//      Serial.print("HUMIDITY:\t");
//      Serial.print(humidity);
//      Serial.println(" %");
//
//    float pressure = bme280.readFloatPressure();  // BME280 pressure
//      btSerial.write(pressure);   
//      btSerial.write('P');
//      Serial.print("PRESSURE:\t");
//      Serial.print(pressure);
//      Serial.println(" Pa");
//
//
//      btSerial.write(alt);                      // BME280 altitude (measured in setup)
//      btSerial.write('A');
//      Serial.print("ALTITUDE:\t");
//      Serial.print(alt);
//      Serial.println(" m");
//
//    float temp = bme280.readTempC();            // BME280 temp
//      btSerial.write(temp);   
//      btSerial.write('T');
//      Serial.print("TEMPERATURE:\t");
//      Serial.print(temp);  
//      Serial.println(" C");  
//      Serial.print("\n\n");
//  }

  if (btSerial.available())       // UNCOMMENT to allow for AT commands. Also uncomment all else in loop()
    Serial.write(btSerial.read());

  
  if (Serial.available())
    btSerial.write(Serial.read());
  
//  delay(50); //Don't spam the I2C bus
}
