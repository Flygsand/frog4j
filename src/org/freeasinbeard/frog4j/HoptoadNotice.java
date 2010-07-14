package org.freeasinbeard.frog4j;

import org.apache.log4j.spi.LocationInfo;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.spi.ThrowableInformation;

public class HoptoadNotice {
    
    private Error error;
    private Request request;
    
    public HoptoadNotice(LoggingEvent loggingEvent) {
        Object message = loggingEvent.getMessage();
        ThrowableInformation throwableInfo;
        if ((throwableInfo = loggingEvent.getThrowableInformation()) != null) {
            Throwable t = throwableInfo.getThrowable();
            error = new Error(message, t);
            request = new Request(t);
        } else {
            LocationInfo locInfo = loggingEvent.getLocationInformation();
            error = new Error(message, locInfo);
            request = new Request(locInfo);
        }
    }
    
    public Error error() {
        return error;
    }
    
    public Request request() {
        return request;
    }
    
    public class Error {
        private String klass;
        private String message;
        private StackTraceElement[] backtrace;
        
        private Error(Object logMsg, Throwable t) {
            klass = t.getClass().getName();
            message = String.format("%s: %s", logMsg, klass);
            if (t.getMessage() != null) {
            	message = String.format("%s(\"%s\")", message, t.getMessage());
            }
            backtrace = t.getStackTrace();
        }
        
        private Error(Object logMsg, LocationInfo locInfo) {
            this.klass = locInfo.getClassName();
            this.message = logMsg + "";
            this.backtrace = new StackTraceElement[] {
                new StackTraceElement(
                    locInfo.getClassName(),
                    locInfo.getMethodName(),
                    locInfo.getFileName(),
                    Integer.parseInt(locInfo.getLineNumber())
                )
            };
        }
        
        public String klass() {
            return klass;
        }
        
        public String message() {
            return message;
        }
        
        public StackTraceElement[] backtrace() {
            return backtrace;
        }
    }
    
    public class Request {
        private String url;
        private String component;
        private String action;
        
        public Request(Throwable t) {
            StackTraceElement[] trace = t.getStackTrace();
            StackTraceElement top = trace.length > 0 ? trace[0] : null;
            
            if (top == null) {
                url = "";
                component = "";
                action = "";
            } else {
                url = top.getFileName();
                component = top.getClassName();
                action = top.getMethodName();
            }
        }
        
        public Request(LocationInfo locInfo) {
            url = locInfo.getFileName();
            component = locInfo.getClassName();
            action = locInfo.getMethodName();
        }
        
        public String url() {
            return url;
        }
        
        public String component() {
            return component;
        }
        
        public String action() {
            return action;
        }
    }
    
}
