package org.freeasinbeard.frog4j;

import org.apache.log4j.spi.LocationInfo;
import org.apache.log4j.spi.LoggingEvent;

public class HoptoadNotice {
    
    private Error error;
    
    public HoptoadNotice(LoggingEvent loggingEvent) {
        this.error = new Error(loggingEvent);
    }
    
    public Error error() {
        return error;
    }
    
    public class Error {
        private LoggingEvent event;
        
        private Error(LoggingEvent event) {
            this.event = event;
        }
        
        public String klass() {
            if (event.getThrowableInformation() == null)
                return "";
            
            return event
                .getThrowableInformation()
                .getThrowable()
                .getClass()
                .getName();
        }
        
        public String message() {
            StringBuilder message = new StringBuilder(event.getMessage() + "");
            
            if (event.getThrowableInformation() != null) {
                message
                    .append(": ")
                    .append(event.getThrowableInformation().getThrowable());
            }
            
            return message.toString();
        }
        
        public StackTraceElement[] backtrace() {
            if (event.getThrowableInformation() != null)
                return event
                        .getThrowableInformation()
                        .getThrowable()
                        .getStackTrace();
            
            LocationInfo info = event.getLocationInformation();
            
            return new StackTraceElement[] {
                new StackTraceElement(
                        info.getClassName(),
                        info.getMethodName(),
                        info.getFileName(),
                        Integer.parseInt(info.getLineNumber())
                )
            };
        }
    }
    
}
