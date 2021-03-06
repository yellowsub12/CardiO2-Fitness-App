#include <SoftwareSerial.h>
#define TxD 2
#define RxD 3
#include <Wire.h>
#include "MAX30105.h" //sparkfun MAX3010X library
#include "heartRate.h"
MAX30105 particleSensor;
SoftwareSerial Bluetooth (RxD, TxD);





// Large amounts of this code come from various example files
// 




byte receivedData;
const byte RATE_SIZE = 4; //Increase this for more averaging. 4 is good.
byte rates[RATE_SIZE]; //Array of heart rates
byte rateSpot = 0;
long lastBeat = 0; //Time at which the last beat occurred

float beatsPerMinute;
int beatAvg;
//#define MAX30105 //if you have Sparkfun's MAX30105 breakout board , try #define MAX30105 
#define MAX30105
#define USEFIFO

//Bluetooth transmitter handles comminication automatically, 
//enabled by the fact the sensor does not rely on any received information, only outgoing.

void setup()
{
  Bluetooth.begin(9600);
  Serial.begin(9600);
  Serial.println("Initializing...");

  // Initialize sensor
  if (!particleSensor.begin(Wire, I2C_SPEED_FAST)) //Use default I2C port, 400kHz speed
  {
    Serial.println("MAX30102 was not found. Please check wiring/power/solder jumper at MH-ET LIVE MAX30102 board. ");
    while (1);
  }

  //Setup to sense a nice looking saw tooth on the plotter
  byte ledBrightness = 0x7F; //Options: 0=Off to 255=50mA
  byte sampleAverage = 4; //Options: 1, 2, 4, 8, 16, 32
  byte ledMode = 2; //Options: 1 = Red only, 2 = Red + IR, 3 = Red + IR + Green
  //Options: 1 = IR only, 2 = Red + IR on MH-ET LIVE MAX30102 board
  int sampleRate = 200; //Options: 50, 100, 200, 400, 800, 1000, 1600, 3200
  int pulseWidth = 411; //Options: 69, 118, 215, 411
  int adcRange = 16384; //Options: 2048, 4096, 8192, 16384
  // Set up the wanted parameters
  particleSensor.setup(ledBrightness, sampleAverage, ledMode, sampleRate, pulseWidth, adcRange); //Configure sensor with these settings
  //particleSensor.setup();
}
double avered = 0; double aveir = 0;
double sumirrms = 0;
double sumredrms = 0;
int i = 0;
int Num = 100;//calculate SpO2 by this sampling interval

double ESpO2 = 95.0;//initial value of estimated SpO2
double FSpO2 = 0.7; //filter factor for estimated SpO2
double frate = 0.95; //low pass filter for IR/red LED value to eliminate AC component
#define TIMETOBOOT 3000 // wait for this time(msec) to output SpO2
#define SCALE 88.0 //adjust to display heart beat and SpO2 in the same scale
#define SAMPLING 5 //if you want to see heart beat more precisely , set SAMPLING to 1
#define FINGER_ON 30000 // if red signal is lower than this , it indicates your finger is not on the sensor
#define MINIMUM_SPO2 80.0

void loop()
{
// long irValue = particleSensor.getIR();
  uint32_t ir, red , green;
  double fred, fir;
  double SpO2 = 0; //raw SpO2 before low pass filtered

//#ifdef USEFIFO
  particleSensor.check(); //Check the sensor, read up to 3 samples

  while (particleSensor.available()) {//do we have new data
#ifdef MAX30105
   red = particleSensor.getFIFORed(); //Sparkfun's MAX30105
    ir = particleSensor.getFIFOIR();  //Sparkfun's MAX30105
#else
    red = particleSensor.getFIFOIR(); //why getFOFOIR output Red data by MAX30102 on MH-ET LIVE breakout board
    ir = particleSensor.getFIFORed(); //why getFIFORed output IR data by MAX30102 on MH-ET LIVE breakout board
#endif
    i++;
    fred = (double)red;
    fir = (double)ir;
    avered = avered * frate + (double)red * (1.0 - frate);//average red level by low pass filter
    aveir = aveir * frate + (double)ir * (1.0 - frate); //average IR level by low pass filter
    sumredrms += (fred - avered) * (fred - avered); //square sum of alternate component of red level
    sumirrms += (fir - aveir) * (fir - aveir);//square sum of alternate component of IR level
    //if ((i % SAMPLING) == 0) {//slow down graph plotting speed for arduino Serial plotter by thin out
      if ( millis() > TIMETOBOOT) {
    
       /* float ir_forGraph = (2.0 * fir - aveir) / aveir * SCALE;
        float red_forGraph = (2.0 * fred - avered) / avered * SCALE;
        //trancation for Serial plotter's autoscaling
        if ( ir_forGraph > 100.0) ir_forGraph = 100.0;
        if ( ir_forGraph < 80.0) ir_forGraph = 80.0;
        if ( red_forGraph > 100.0 ) red_forGraph = 100.0;
        if ( red_forGraph < 80.0 ) red_forGraph = 80.0;*/
        //        Serial.print(red); Serial.print(","); Serial.print(ir);Serial.print(".");
        if (ir < FINGER_ON) ESpO2 = MINIMUM_SPO2; //indicator for finger detached
       // Serial.print(ir_forGraph); // to display pulse wave at the same time with SpO2 data
       // Serial.print(","); Serial.print(red_forGraph); // to display pulse wave at the same time with SpO2 data
       // Serial.print(",");
      // Serial.print(" BPM=");
      //Serial.print(beatsPerMinute);
      // Bluetooth.print(beatsPerMinute);
      long irValue = particleSensor.getIR();
  if (checkForBeat(irValue) == true)
  {
    //We sensed a beat!
    long delta = millis() - lastBeat;
    lastBeat = millis();

    beatsPerMinute = 60 / (delta / 1000.0);

    if (beatsPerMinute < 255 && beatsPerMinute > 20)
    {
      rates[rateSpot++] = (byte)beatsPerMinute; //Store this reading in the array
      rateSpot %= RATE_SIZE; //Wrap variable

      //Take average of readings
      beatAvg = 0;
      for (byte x = 0 ; x < RATE_SIZE ; x++)
        beatAvg += rates[x];
      beatAvg /= RATE_SIZE;
    }
  }

      //Communication with the application
      //serial stuff is for debugging, bluetooth for app communication
      
      Serial.print("Avg BPM=");
      Bluetooth.print("s"); //start of stream for bpm
      Serial.print(beatAvg); 
      Bluetooth.print(beatAvg);
      Bluetooth.print("e");  //end of stream
      Serial.print("  ,SPO2=");
      Bluetooth.print("t"); //start of stream for sp02
        Serial.print(ESpO2); //low pass filtered SpO2
        Bluetooth.print(ESpO2);
        Bluetooth.print("o");  //end of stream
       // Serial.print(","); Serial.print(85.0); //reference SpO2 line
       // Serial.print(","); Serial.print(90.0); //warning SpO2 line
       // Serial.print(","); Serial.print(95.0); //safe SpO2 line
       // Serial.print(","); Serial.println(100.0); //max SpO2 line
       Serial.println();
      }
   // }
    if ((i % Num) == 0) {
      double R = (sqrt(sumredrms) / avered) / (sqrt(sumirrms) / aveir);
      // Serial.println(R);
      SpO2 = -23.3 * (R - 0.4) + 100; //http://ww1.microchip.com/downloads/jp/AppNotes/00001525B_JP.pdf
      ESpO2 = FSpO2 * ESpO2 + (1.0 - FSpO2) * SpO2;//low pass filter
      //  Serial.print(SpO2);Serial.print(",");Serial.println(ESpO2);
      sumredrms = 0.0; sumirrms = 0.0; i = 0;
      break;
    }
    particleSensor.nextSample(); //We're finished with this sample so move to next sample
    //Serial.println(SpO2);
  }/*
#else

  while (1) {//do we have new data
#ifdef MAX30105
   red = particleSensor.getRed();  //Sparkfun's MAX30105
    ir = particleSensor.getIR();  //Sparkfun's MAX30105
#else
    red = particleSensor.getIR(); //why getFOFOIR outputs Red data by MAX30102 on MH-ET LIVE breakout board
    ir = particleSensor.getRed(); //why getFIFORed outputs IR data by MAX30102 on MH-ET LIVE breakout board
#endif
    i++;
    fred = (double)red;
    fir = (double)ir;
    avered = avered * frate + (double)red * (1.0 - frate);//average red level by low pass filter
    aveir = aveir * frate + (double)ir * (1.0 - frate); //average IR level by low pass filter
    sumredrms += (fred - avered) * (fred - avered); //square sum of alternate component of red level
    sumirrms += (fir - aveir) * (fir - aveir);//square sum of alternate component of IR level
/*    if ((i % SAMPLING) == 0) {//slow down graph plotting speed for arduino IDE toos menu by thin out
      //#if 0
      
      if ( millis() > TIMETOBOOT) {
          if (checkForBeat(irValue) == true)
  {
    //We sensed a beat!
    long delta = millis() - lastBeat;
    lastBeat = millis();

    beatsPerMinute = 60 / (delta / 1000.0);

    if (beatsPerMinute < 255 && beatsPerMinute > 20)
    {
      rates[rateSpot++] = (byte)beatsPerMinute; //Store this reading in the array
      rateSpot %= RATE_SIZE; //Wrap variable

      //Take average of readings
      beatAvg = 0;
      for (byte x = 0 ; x < RATE_SIZE ; x++)
        beatAvg += rates[x];
      beatAvg /= RATE_SIZE;
    }
  }
       /* float ir_forGraph = (2.0 * fir - aveir) / aveir * SCALE;
        float red_forGraph = (2.0 * fred - avered) / avered * SCALE;
        //trancation for Serial plotter's autoscaling
        if ( ir_forGraph > 100.0) ir_forGraph = 100.0;
        if ( ir_forGraph < 80.0) ir_forGraph = 80.0;
        if ( red_forGraph > 100.0 ) red_forGraph = 100.0;
        if ( red_forGraph < 80.0 ) red_forGraph = 80.0;
             //   Serial.print(red); Serial.print(","); Serial.print(ir);Serial.print(".");
        if (ir < FINGER_ON) ESpO2 = MINIMUM_SPO2; //indicator for finger detached  
        Serial.print((2.0 * fir - aveir) / aveir * SCALE); // to display pulse wave at the same time with SpO2 data
        Serial.print(","); Serial.print((2.0 * fred - avered) / avered * SCALE); // to display pulse wave at the same time with SpO2 data
        Serial.print(","); Serial.print(ESpO2); //low pass filtered SpO2 
        Serial.print(", Avg BPM=");
        Bluetooth.print("s"); //start of stream
        Serial.print(beatAvg);
         //Bluetooth.print("s"); //start of stream
        Bluetooth.print(beatAvg);
        Bluetooth.print("e");  //end of stream
      //  Serial.print(","); Serial.print(85.0); //
       // Serial.print(","); Serial.print(90.0); //warning SpO2 line
       // Serial.print(","); Serial.print(95.0); //safe SpO2 line
        //Serial.print(","); Serial.println(100.0); //max SpO2 line
        //#endif
      }
    }
    if ((i % Num) == 0) {
      double R = (sqrt(sumredrms) / avered) / (sqrt(sumirrms) / aveir);
      // Serial.println(R);
      SpO2 = -23.3 * (R - 0.4) + 100; //http://ww1.microchip.com/downloads/jp/AppNotes/00001525B_JP.pdf
      ESpO2 = FSpO2 * ESpO2 + (1.0 - FSpO2) * SpO2;
      //  Serial.print(SpO2);Serial.print(",");Serial.println(ESpO2);
      sumredrms = 0.0; sumirrms = 0.0; i = 0;
      break;
    }
    particleSensor.nextSample(); //We're finished with this sample so move to next sample
    //Serial.println(SpO2);
  }
#endif*/
//delay(10);
}
