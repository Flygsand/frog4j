package org.freeasinbeard.frog4j;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertEquals;

import org.freeasinbeard.util.xml.XmlDocument;
import org.junit.Before;
import org.junit.Test;


public class TestHoptoadNotifier {
    private HoptoadNotifier notifier;
    private HoptoadNotice notice;
    
    @Before
    public void setUp() {
        notifier = new HoptoadNotifier("apikey1234", "test");
        
        HoptoadNotice.Error error = createMock(HoptoadNotice.Error.class);
        expect(error.klass()).andStubReturn("java.lang.NullPointerException");
        expect(error.message()).andStubReturn("Lorem ipsum dolor sit amet");
        expect(error.backtrace()).andStubReturn(new StackTraceElement[] {
           new StackTraceElement("org.freeasinbeard.MyClass", "myMethod1", "MyClass.java", 5),
           new StackTraceElement("org.freeasinbeard.MyClass", "myMethod2", "MyClass.java", 10)
        });
        
        HoptoadNotice.Request request = createMock(HoptoadNotice.Request.class);
        expect(request.url()).andStubReturn("MyClass.java");
        expect(request.component()).andStubReturn("org.freeasinbeard.MyClass");
        expect(request.action()).andStubReturn("myMethod1");
        
        notice = createMock(HoptoadNotice.class);
        expect(notice.error()).andStubReturn(error);
        expect(notice.request()).andStubReturn(request);
        
        replay(error, request, notice); 
    }
    
    @Test
    public void testXMLOutput() {
        XmlDocument doc = notifier.buildRequestXml(notice);
        assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?><notice version=\"2.0\"><api-key>apikey1234</api-key><notifier><name>frog4j</name><version>0.9.1</version><url>http://github.com/mtah/frog4j</url></notifier><error><class>java.lang.NullPointerException</class><message>Lorem ipsum dolor sit amet</message><backtrace><line method=\"myMethod1\" file=\"MyClass.java\" number=\"5\"/><line method=\"myMethod2\" file=\"MyClass.java\" number=\"10\"/></backtrace></error><request><url>MyClass.java</url><component>org.freeasinbeard.MyClass</component><action>myMethod1</action></request><server-environment><environment-name>test</environment-name></server-environment></notice>",
                     doc.toString());
    }
}
