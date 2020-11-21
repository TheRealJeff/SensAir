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
                  
  int co2 = ccs811.getCO2();                      // co2                    ppm
  int tvoc = ccs811.getTVOC();                    // TVOC                   ppb
  int mq2_reading = analogRead(MQ2_PIN);          // MQ2                    ppb
  float humidity = bme280.readFloatHumidity();    // BME280 humidity        %
  float pressure = bme280.readFloatPressure();    // BME280 pressure        Pascals
  float temp = bme280.readTempC()-5;                // BME280 temp            Celsius

  String co2_str = String(co2)+"C,";
  String tvoc_str = String(tvoc)+"t,";
  String mq2_str = String(mq2_reading)+"M,";
  String humidity_str = String(humidity)+"H,";
  String pressure_str = String(pressure)+"P,";
  String alt_str = String(alt)+"A,";
  String temp_str = String(temp)+"T,";
  
  int flag;
  if(btSerial.available())
    flag = btSerial.read();
  if(flag=='1')
  {
    btSerial.print(co2_str);
    btSerial.print(tvoc_str);
    btSerial.print(mq2_str);
    btSerial.print(humidity_str);
    btSerial.print(pressure_str);
    btSerial.print(alt_str);
    btSerial.print(temp_str);
    btSerial.print('\n');
  }
//      Serial.print("CO2:\t\t");
//      Serial.print(co2);
//      Serial.println(" ppm");
    
//      Serial.print("TVOC:\t\t");
//      Serial.print(tvoc);
//      Serial.println(" ppb");
//  
//      Serial.print("MQ2:\t\t");
//      Serial.print(mq2_reading);
//      Serial.println(" ppb");
//    
//      Serial.print("HUMIDITY:\t");
//      Serial.print(humidity);
//      Serial.println(" %");
//    
//      Serial.print("PRESSURE:\t");
//      Serial.print(pressure);
//      Serial.println(" Pa");
//
//      Serial.print("ALTITUDE:\t");
//      Serial.print(alt);
//      Serial.println(" m");
//  
//      Serial.print("TEMPERATURE:\t");
//      Serial.print(temp);   
//      Serial.print("\n\n");

  delay(50); //Don't spam the I2C bus

//  if (btSerial.available())       // UNCOMMENT to allow for AT commands. Also uncomment all else in loop()
//    Serial.write(btSerial.read());
//
//  
//  if (Serial.available())
//    btSerial.write(Serial.read());
}
