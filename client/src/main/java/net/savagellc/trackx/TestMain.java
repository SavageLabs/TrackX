package net.savagellc.trackx;

public class TestMain {
    public static void main(String[] args) {
     //   TrackX.startTracking("test-plugin", "1.0", "savagellc");
        new Thread(() -> {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            throw new RuntimeException("this is a error");
        }).start();
    }
}
