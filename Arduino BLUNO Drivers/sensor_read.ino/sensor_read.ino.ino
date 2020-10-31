#include <Wire.h>


// Pin/Register Definitions
#define CSS811_ADDRESS 0b1011010
#define ALG_RESULT_DATA_REGISTER byte(0x02)
byte val = 0;

#define MQ_PIN A0

// Setup 
void setup() 
{
  Wire.begin();               // initialize I2C
  Serial.begin(9600);         // make sure serial monitor matches this rate
}

void loop() 
{
  // Check if lines are high (aka check for pull-up resistors)
  if((digitalRead(A4) == HIGH) && (digitalRead(A5) == HIGH))
  {  
  
    Wire.beginTransmission(CSS811_ADDRESS);
    Wire.write(ALG_RESULT_DATA_REGISTER);
    Wire.endTransmission();

    Wire.requestFrom(CSS811_ADDRESS,4);
    byte co2_1,co2_2,tvoc_1,tvoc_2;
    while(Wire.available())
    {
      co2_1 = Wire.read();
      delay(500);
      co2_2 = Wire.read();
      delay(500);
      tvoc_1 = Wire.read();
      delay(500);
      tvoc_2 = Wire.read();
      delay(500);
    }
    int eco2_reading = int(co2_1)+int(co2_2);
    Serial.print("\t");
    Serial.print(eco2_reading);
    Serial.println(" ppm CO2");
  
    int tvoc_reading = int(tvoc_1)+int(tvoc_2);
    Serial.print("\t");
    Serial.print(tvoc_reading);
    Serial.println(" ppb TVOC");

    
    int combustible_gas_reading = analogRead(MQ_PIN);
    delay(500);
    Serial.print("\t");
    Serial.print(combustible_gas_reading);
    Serial.println(" ppb Combustible Gasses\n\n");
  }
  else
  {
    Serial.println("Failed - Lines LOW");   // if this prints, SDA and SCLK need pull up resistors
  }

}
