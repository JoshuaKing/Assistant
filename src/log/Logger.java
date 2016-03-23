package log;

/**
 * Created by Josh on 17/02/2016.
 */
public class Logger {
    private static final int LEVEL = 0;

    public static void debug(String s) {
        if (LEVEL > 0) return;
        System.out.println("[DEBUG] " + s);
    }
    public static void info(String s) {
        if (LEVEL > 1) return;
        System.out.println("[INFO] " + s);
    }
    public static void alert(String s) {
        if (LEVEL > 2) return;
        System.out.println("[ALERT] " + s);
    }
    public static void warn(String s) {
        System.err.println("[WARN] " + s);
    }
    public static void error(String s) {
        System.err.println("[ERROR] " + s);
        System.exit(-1);
    }
    public static void error(Exception e) {
        System.out.println("[ERROR] " + e.getMessage());
        e.printStackTrace();
        System.exit(-1);
    }
}
