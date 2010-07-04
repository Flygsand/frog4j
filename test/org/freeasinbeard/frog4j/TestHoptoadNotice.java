package org.freeasinbeard.frog4j;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import org.apache.log4j.spi.LocationInfo;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.spi.ThrowableInformation;
import org.junit.Before;
import org.junit.Test;

public class TestHoptoadNotice {
    
    private LoggingEvent event;
    
    private HoptoadNotice notice;
    
    @Before
    public void setUp() {
        event = createMock(LoggingEvent.class);
        notice = new HoptoadNotice(event);
        
        expect(event.getMessage()).andStubReturn("Log message");
    }
    
    @Test
    public void testLoggingEventWithThrowable() {
        Exception excpt = new NullPointerException("Exception message");
        ThrowableInformation throwableInfo = new ThrowableInformation(excpt);
        expect(event.getThrowableInformation()).andStubReturn(throwableInfo);
        replay(event);
        
        assertEquals(excpt.getClass().getName(), notice.error().klass());
        assertEquals(String.format("Log message: %s: %s", excpt.getClass().getName(), excpt.getMessage()), 
                     notice.error().message());
        assertArrayEquals(excpt.getStackTrace(), notice.error().backtrace());
    }
    
    @Test
    public void testLoggingEventWithoutThrowable() {
        
        LocationInfo locInfo = new LocationInfo("MyClass.java", "org.freeasinbeard.MyClass", "myMethod", "5");
        expect(event.getLocationInformation()).andStubReturn(locInfo);
        expect(event.getThrowableInformation()).andStubReturn(null);
        
        replay(event);
        
        assertEquals("", notice.error().klass());
        assertEquals("Log message", notice.error().message());
        assertArrayEquals(new StackTraceElement[] {
            new StackTraceElement("org.freeasinbeard.MyClass", "myMethod", "MyClass.java", 5)
        }, notice.error().backtrace());
    }
}
