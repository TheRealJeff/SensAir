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
  btSerial.begin(9600);  //Baud Rate for AT-command Mode.  
  Serial.println("|---------- Set Up ----------|"); 
  
//  while(!Serial){;}
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
    ccs811.readAlgorithmResults();    // read results from CSS811
                                      // for each datapoint, we write it to bluetooth then print in serial
    int co2 = ccs811.getCO2();        // co2
      Serial.print("CO2:\t\t");
      Serial.print(co2);
      Serial.println(" ppm");
      
  
    int tvoc = ccs811.getTVOC();    // TVOC
      Serial.print("TVOC:\t\t");
      Serial.print(tvoc);
      Serial.println(" ppb");

    
    int mq2_reading = analogRead(MQ2_PIN);  // MQ2   
      Serial.print("MQ2:\t\t");
      Serial.print(mq2_reading);
      Serial.println(" ppb");

    float humidity = bme280.readFloatHumidity();  // BME280 humidity
      Serial.print("HUMIDITY:\t");
      Serial.print(humidity);
      Serial.println(" %");

    float pressure = bme280.readFloatPressure();  // BME280 pressure
      btSerial.print(String(pressure));   
      btSerial.print(",");
      Serial.print("PRESSURE:\t");
      Serial.print(pressure);
      Serial.println(" Pa");


      Serial.print("ALTITUDE:\t");
      Serial.print(alt);
      Serial.println(" m");
  
    float temp = bme280.readTempC();            // BME280 temp
      Serial.print("TEMPERATURE:\t");
      Serial.print(temp);   
      Serial.print("\n\n");
 

  String co2_str = String(co2)+"C";
  String tvoc_str = String(tvoc)+"T";
  String mq2_str = String(mq2_reading)+"M";
  String humidity_str = String(humidity)+"H";
  String pressure_str = String(pressure)+"P";
  String alt_str = String(alt)+"A";
  String temp_str = String(temp)+"T";
  
  
  btSerial.print(co2_str);
  btSerial.print(tvoc_str);
  btSerial.print(mq2_str);
  btSerial.print(humidity_str);
  btSerial.print(pressure_str);
  btSerial.print(alt_str);
  btSerial.print(temp_str);

  delay(20); //Don't spam the I2C bus

//  if (btSerial.available())       // UNCOMMENT to allow for AT commands. Also uncomment all else in loop()
//    Serial.write(btSerial.read());
//
//  
//  if (Serial.available())
//    btSerial.write(Serial.read());
}
