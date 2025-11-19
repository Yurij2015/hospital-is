package org.slf4j;

public class LoggerFactory {
    private static final Logger NOOP = new Logger() {
        public void info(String msg) {}
        public void info(String format, Object arg) {}
        public void info(String format, Object arg1, Object arg2) {}
        public void warn(String msg) {}
        public void warn(String format, Object arg) {}
        public void error(String msg) {}
        public void debug(String msg) {}
        public void trace(String msg) {}
        public boolean isDebugEnabled() { return false; }
        public boolean isInfoEnabled() { return false; }
        public boolean isTraceEnabled() { return false; }
    };

    public static Logger getLogger(Class<?> cls) {
        return NOOP;
    }

    public static Logger getLogger(String name) {
        return NOOP;
    }
}
