package log;

/**
 * Created by Josh on 23/03/2016.
 */
public enum LogLevel {
    DEBUG(0),
    INFO(1),
    ALERT(2),
    WARN(3),
    ERROR(4);

    int level;

    LogLevel(int value) {
        level = value;
    }
}
