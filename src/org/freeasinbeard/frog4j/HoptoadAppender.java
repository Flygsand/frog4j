package org.freeasinbeard.frog4j;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Level;
import org.apache.log4j.spi.LoggingEvent;

public class HoptoadAppender extends AppenderSkeleton {
    
    private HoptoadNotifier notifier;
    private boolean onlyLogExceptions;
    
    public HoptoadAppender() {
        this(new HoptoadNotifier());
    }

    public HoptoadAppender(HoptoadNotifier notifier) {
        this.notifier = notifier;
        setThreshold(Level.ERROR);
        setOnly_log_exceptions(true);
    }

    public void setOnly_log_exceptions(boolean bool) {
        onlyLogExceptions = bool;
    }

    public void setApi_key(String apiKey) {
        notifier.setApiKey(apiKey);
    }
    
    public void setEnvironment(String environment) {
        notifier.setEnvironment(environment);
    }

    @Override
    protected void append(LoggingEvent event) {
        try {
            if (!onlyLogExceptions || event.getThrowableInformation() != null) {
                notifier.notify(event);
            }
        } catch (Exception e) { }
    }

    @Override
    public void close() { }

    @Override
    public boolean requiresLayout() {
        return false;
    }

}
