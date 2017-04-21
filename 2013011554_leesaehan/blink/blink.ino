const int ledPin=13;
char buffer[10];
int bufferindex =0;
int blinkRate=100;
void setup() {
  Serial.begin(9600);
   pinMode(ledPin,OUTPUT);
}

void loop() {
  if(Serial.available()){
    blinkRate = 0;
    bufferindex =0;
    for(int a=0;a<10;a++){
       buffer[a]=NULL;
    }
    while(Serial.available()){
      buffer[bufferindex]=Serial.read();
      bufferindex++; 
    }
    blinkRate = atoi(buffer);
    //blinkRate *=100;
    Serial.println(blinkRate);
  } 
  blink();
}

void blink()
{
   digitalWrite(ledPin, HIGH);
   delay(blinkRate);
   digitalWrite(ledPin, LOW);
   delay(blinkRate);
}

