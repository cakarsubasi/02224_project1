import java.lang.System;
import josx.platform.rcx.*;

class Feeder extends Thread {
 
  static final int BLOCKED = 70, YELLOW  = 60, BLACK   = 48;

  Motor  myMotor;
  Sensor mySensor;

  public Feeder(Motor m, Sensor s) { myMotor = m;  mySensor = s; }
  
  public void run() {
    try {
      final boolean closeToA = (myMotor == Motor.A);
      final int myMask = closeToA ? Poll.SENSOR1_MASK : Poll.SENSOR2_MASK;

      Poll e = new Poll();
      int done = (int) System.currentTimeMillis(); // When last bag is through
      
      myMotor.forward();

      while (true) {
        // Await arrival of a bag
        mySensor.activate();
        while(mySensor.readValue() > BLOCKED) { e.poll(myMask,0); }
	  
        Thread.sleep(800);           // Wait for colour to be valid

        boolean destA = (mySensor.readValue() > BLACK);   // Determine destination
        mySensor.passivate();

        Thread.sleep(2000);          // Advance beyond sensor 
        if (Motor.C.isForward() != destA) {  // Must stop
          myMotor.stop();
          int now = (int) System.currentTimeMillis();
          if (now < done) Thread.sleep(done-now);
          Motor.C.reverseDirection();
          myMotor.forward();
        }

        done = ((int) System.currentTimeMillis()) + 6000;  
        if (Motor.C.isForward() != closeToA) done = done + 6000;  // Long path

        Thread.sleep(1200);                 // Follow to end of feed belt
       }
    } catch (Exception e) { }
  }
}

public class SingleSort {

  static final int BELT_SPEED = 5;          // Do not change

  public static void main (String[] arg) {
      Motor.A.setPower(BELT_SPEED);
      Motor.B.setPower(BELT_SPEED);
      Motor.C.setPower(BELT_SPEED);  Motor.C.forward();
      
      Thread f1 = new Feeder(Motor.A, Sensor.S1);  f1.start();
      Thread f2 = new Feeder(Motor.B, Sensor.S2);  f2.start();

      try{ Button.RUN.waitForPressAndRelease();} catch (Exception e) {}
      System.exit(0);
  }
}