package log;

/**
 * Created by Josh on 17/02/2016.
 */
public class Logger {
    private final int level;

    public Logger(LogLevel logLevel) {
        level = logLevel.level;
    }

    public void debug(String s) {
        if (level > 0) return;
        System.out.println("[DEBUG] " + s);
    }

    public void info(String s) {
        if (level > 1) return;
        System.out.println("[INFO] " + s);
    }

    public void alert(String s) {
        if (level > 2) return;
        System.out.println("[ALERT] " + s);
    }

    public void warn(String s) {
        System.err.println("[WARN] " + s);
    }

    public void error(String s) {
        System.err.println("[ERROR] " + s);
        System.exit(-1);
    }

    public void error(Exception e) {
        System.out.println("[ERROR] " + e.getMessage());
        e.printStackTrace();
        System.exit(-1);
    }
}
