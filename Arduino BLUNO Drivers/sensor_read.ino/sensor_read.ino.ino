 #include <Wire.h>

// Pin/Register Definitions
#define CSS811_ADDRESS 0b1011010
#define STATE_REGISTER byte(0x00  )
#define MEAS_MODE_REGISTER byte(0x01)
#define ALG_RESULT_DATA_REGISTER byte(0x02)
#define HW_ID_REGISTER byte(0x20)
byte val = 0;

#define NWAKE_PIN 0
#define MQ_PIN A0

// Setup 
void setup() 
{
  Wire.begin();               // initialize I2C
  Serial.begin(9600);         // make sure serial monitor matches this rate
  //Why such a slow rate?

  digitalWrite(NWAKE_PIN, HIGH);
  delayMicroseconds(50);
  Wire.beginTransmission(CSS811_ADDRESS);
  Wire.write(MEAS_MODE_REGISTER);
  Wire.write(byte(0b00010000));
  Wire.endTransmission();
}

void loop() 
{
  // Check if lines are high (aka check for pull-up resistors)
  if((digitalRead(A4) == HIGH) && (digitalRead(A5) == HIGH))
  {  
    digitalWrite(NWAKE_PIN, HIGH);
    delayMicroseconds(50);
    Wire.beginTransmission(CSS811_ADDRESS);
    Wire.write(ALG_RESULT_DATA_REGISTER);
    Wire.endTransmission();

    Wire.requestFrom(CSS811_ADDRESS,4);
    digitalWrite(NWAKE_PIN, LOW);
    byte co2_1,co2_2,tvoc_1,tvoc_2;
    while(Wire.available())
    {
      co2_1 = Wire.read();
      delay(500);         //Do these delays really need to be here?
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
    delay(500);       //Does this delay need to be here?
    //If you want to slow down the entire loop, just add a single delay at the end. It's clearer
    Serial.print("\t");
    Serial.print(combustible_gas_reading);
    Serial.println(" ppb Combustible Gasses\n\n");
  }
  else
  {
    Serial.println("Failed - Lines LOW");   // if this prints, SDA and SCLK need pull up resistors
  }

}
