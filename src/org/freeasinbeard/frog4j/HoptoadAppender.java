package org.freeasinbeard.frog4j;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Level;
import org.apache.log4j.spi.LoggingEvent;

public class HoptoadAppender extends AppenderSkeleton {
    
    private HoptoadNotifier notifier;
    
    public HoptoadAppender() {
        notifier = new HoptoadNotifier();
        setThreshold(Level.ERROR);
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
            notifier.notify(event);
        } catch (Exception e) { 
            // TODO: log this (avoid recursion)
        }
    }

    @Override
    public void close() { }

    @Override
    public boolean requiresLayout() {
        return false;
    }

}
