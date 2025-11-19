package org.slf4j;

public interface Logger {
    void info(String msg);
    void info(String format, Object arg);
    void info(String format, Object arg1, Object arg2);
    void warn(String msg);
    void warn(String format, Object arg);
    void error(String msg);
    void debug(String msg);
    void trace(String msg);
    boolean isDebugEnabled();
    boolean isInfoEnabled();
    boolean isTraceEnabled();
}
