package org.freeasinbeard.frog4j;

import static org.easymock.EasyMock.capture;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.anyObject;
import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.spi.ThrowableInformation;
import org.easymock.Capture;
import org.junit.Before;
import org.junit.Test;

public class TestHoptoadAppender {

    private HoptoadAppender appender;
    private LoggingEvent eventWithException;
    private LoggingEvent eventWithoutException;
    private HoptoadNotifier notifier;

    @Before
    public void setUp() {
        notifier = createMock(HoptoadNotifier.class);

        appender = new HoptoadAppender(notifier);
        appender.setApi_key("76fdb93ab2cf276ec080671a8b3d3866");
        appender.setEnvironment("test");

        eventWithException = createMock(LoggingEvent.class);
        expect(eventWithException.getThrowableInformation()).andStubReturn(new ThrowableInformation(new NullPointerException()));

        eventWithoutException = createMock(LoggingEvent.class);
        expect(eventWithoutException.getThrowableInformation()).andStubReturn(null);

    }

    @Test
    public void testOnlyLogsExceptionsByDefault() throws HttpException, IOException {
        Capture<LoggingEvent> captured = new Capture<LoggingEvent>();
        notifier.notify(capture(captured));
        expectLastCall().once();

        replay(notifier, eventWithException, eventWithoutException);    
        appender.append(eventWithException);
        appender.append(eventWithoutException);

        assertEquals(eventWithException, captured.getValue());
    }

    @Test
    public void testLogsEverythingWhenTold() throws HttpException, IOException {
        appender.setOnly_log_exceptions(false);

        notifier.notify(anyObject(LoggingEvent.class));
        expectLastCall().times(2);

        replay(notifier, eventWithException, eventWithoutException);    
        appender.append(eventWithException);
        appender.append(eventWithoutException);
    }
}