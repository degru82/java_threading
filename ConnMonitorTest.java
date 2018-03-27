import java.util.concurrent.TimeUnit;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.*;


class TimerReturn {
    public String vehicleId;
    public int expirationCount;

    public TimerReturn(String id, int count) {
        vehicleId = id;
        expirationCount = count;
    }
}


class TimerObject extends Thread {
    private Thread t;
    public String id;
    public int expirationCount;
    private ConnMonitor mon;

    public TimerObject( String id , ConnMonitor mon) {
        this.id = id;
        expirationCount = 0;
        this.mon = mon;
    }
 
    public void run() {

            try {
                TimeUnit.SECONDS.sleep(5);
            } catch (Exception e ) {
                e.printStackTrace();
            }

            expirationCount++;

            mon.getExpired(id);
    }

    public void start() {
        System.out.println("TimerObject: " + this.id + " starts");
        if (t==null) {
            t = new Thread(this, id);
            t.start();
        }
    }

}


class ConnMonitor extends Thread {
    private Thread t;
    private int default_timer_set;

    private Queue<String> reqQue;
    private List<TimerObject> timerPool;
    private Queue<String> retQue;

    public ConnMonitor (int def_time) {
        this.default_timer_set = def_time;
        reqQue = new LinkedList<String>();
        timerPool = new ArrayList<TimerObject>(100);
        retQue = new LinkedList<String>();

    }

    public void run() {

        while(true) {
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("--" + Integer.toString(reqQue.size()) 
                + "--" + Integer.toString(retQue.size()));

            checkReqQue();
            checkRetQue();
        }
    }

    public void start() {
        System.out.println("Starting Timer Monitor Thread");

        if (t==null) {
            t = new Thread (this, "TimerMonitor");
            t.start();
        }
    }

    public synchronized void getRequest(String newbie) {
        reqQue.add(newbie);
    }

    private synchronized void checkReqQue() {
        for (int i=0; i < reqQue.size(); i++) {
            String vehId = reqQue.remove();

            Boolean found = false;
            for (int j=0; j < timerPool.size(); j++ ) {
                System.out.println(timerPool.get(j).id);
                if (vehId == timerPool.get(j).id) {
                    found = true;
                }
            }

            if (!found) {
                TimerObject tmp = new TimerObject(vehId, this);
                tmp.start();

                timerPool.add(tmp);
            }
        }
    }
    private synchronized void checkRetQue() {
        for (int i=0; i<retQue.size(); i++) {

        }
    }

    public void getExpired(String id ) {
        System.out.println("EXPIRED FOR " + id);
        retQue.add(id);
    }
}

public class ConnMonitorTest extends Thread {
    Thread t;
    ConnMonitor monitor;

    ConnMonitorTest (ConnMonitor monitor) {
        this.monitor = monitor;
    }

    @Override
    public void run() {
        System.out.println("Running Parent");

        try {
            TimeUnit.SECONDS.sleep(3);
            monitor.getRequest("VH_TEST002");
        } catch (Exception e) {
            ;
        }

        while(true) {
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch(Exception e) {
                e.printStackTrace();
            }
            System.out.println("-");
        }
    }


    public static void main(String[] args) throws Exception {

        ConnMonitor monitor = new ConnMonitor(3);
        monitor.start();
        
        new ConnMonitorTest( monitor ).start();

   }
}