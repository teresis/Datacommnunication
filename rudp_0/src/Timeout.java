
import java.util.Timer;
import java.util.TimerTask;


public class Timeout {
    Timer timer= new Timer();
    TimeoutTask[] myTimerTask = new TimeoutTask[16]; //MAXSIZE
    Signaling pp;
    public static int timeoutcount=0;
    boolean DEBUG=true;
    public void Timeoutset (int i, int milliseconds, Signaling p) {
  
    	pp=p;
    	myTimerTask[i]=new TimeoutTask(i);
        timer.schedule(myTimerTask[i], milliseconds);
	}
    public void Timeoutcancel (int i) {

    	if(DEBUG) System.out.println("Time's cancealed! seqNo = "+i);
        myTimerTask[i].cancel();
	}

    class TimeoutTask extends TimerTask {
    	int jj;
    	TimeoutTask(int j) {    		
    		jj=j;
    	}
    	public void run() {
            if(DEBUG) System.out.println("Time's up! "+ ++timeoutcount);
            pp.Timeoutnotifying();
            this.cancel(); //Terminate the timerTask thread
        }
    }
}
