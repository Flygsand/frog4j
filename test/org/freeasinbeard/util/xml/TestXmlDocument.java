package org.freeasinbeard.util.xml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Random;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TestXmlDocument {

    private XmlDocument doc;
    
    @Before
    public void setUp() throws Exception {
        doc = new XmlDocument(Charset.forName("UTF-8"));
    }
    
    @Test
    public void testValidXML() {
        doc
            .begin("root")
                .attr("id", 1)
                .begin("shorttag").end()
                .begin("cdatatag").cdata("\"'<>& are escaped").end()
            .end();
        
        assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?><root id=\"1\"><shorttag/><cdatatag>&quot;&apos;&lt;&gt;&amp; are escaped</cdatatag></root>",
                    doc.toString());
    }
    
    @Test
    public void testUnendedTags() { 
        doc
            .begin("tag1")
                .begin("tag2")
                    .begin("tag3")
                    .end();
        
        try {
            doc.toString();
            failExceptionNotThrown(IllegalStateException.class);
        } catch (IllegalStateException e) {
            assertEquals("Missing end() for tag1, tag2", e.getMessage());
        }
    }
    
    @Test
    public void testEndWithoutMatchingBegin() {
        try {
            doc.begin("tag").end().end();
            failExceptionNotThrown(IllegalStateException.class);
        } catch (IllegalStateException e) {
            assertEquals("Trailing end() has no matching begin()", e.getMessage());
        }
    }
    
    @Test
    public void testIncorrectAttributePlacement() {
        try {
            doc.begin("root").end().attr("id", 1);
            failExceptionNotThrown(IllegalStateException.class);
        } catch (IllegalStateException e) {
            assertEquals("After \"\"?><root/>\": attr() can only be applied to begin()", e.getMessage());
        }
    }
    
    @Test
    public void testTagNameNull() {
        try {
            doc.begin(null).end();
            failExceptionNotThrown(IllegalArgumentException.class);
        } catch (IllegalArgumentException e) {
            assertEquals("Tag name cannot be null", e.getMessage());
        }
    }
    
    @Test
    public void testInvalidTagName1() {
        try {
            doc.begin("escape<characters").end();
            failExceptionNotThrown(IllegalArgumentException.class);
        } catch (IllegalArgumentException e) {
            assertEquals("After \"=\"UTF-8\"?>\": Invalid tag name \"escape<characters\"", e.getMessage());
        }
    }
    
    @Test
    public void testInvalidTagName2() {        
        try {
            doc.begin("white space").end();
            failExceptionNotThrown(IllegalArgumentException.class);
        } catch (IllegalArgumentException e) {
            assertEquals("After \"=\"UTF-8\"?>\": Invalid tag name \"white space\"", e.getMessage());
        }
    }
    
    @Test
    public void testInvalidTagName3() {     
        try {
            doc.begin("0leadingdigit").end();
            failExceptionNotThrown(IllegalArgumentException.class);
        } catch (IllegalArgumentException e) {
            assertEquals("After \"=\"UTF-8\"?>\": Invalid tag name \"0leadingdigit\"", e.getMessage());
        }
    }
    
    @Test
    public void testValidTagNames() {
        doc
            .begin("alphanumeric1234")
                .begin("_leadingunderscore")
                    .begin("tag.name.with.dots")
                        .begin("tag_name_with_underscores")
                        .end()
                    .end()
                .end()
            .end();
    }
    
    @Test
    public void testAttributeNameNull() {
        try {
            doc.begin("tag").attr(null, 1).end();
            failExceptionNotThrown(IllegalArgumentException.class);
        } catch (IllegalArgumentException e) {
            assertEquals("Attribute name cannot be null", e.getMessage());
        }
    }
    
    @Test
    public void testInvalidAttributeName1() {
        try {
            doc.begin("tag").attr("escape<characters", 1).end();
            failExceptionNotThrown(IllegalArgumentException.class);
        } catch (IllegalArgumentException e) {
            assertEquals("After \"F-8\"?><tag\": Invalid attribute name \"escape<characters\"", e.getMessage());
        }
    }
    
    @Test
    public void testInvalidAttributeName2() {
        try {
            doc.begin("tag").attr("white space", 1).end();
            failExceptionNotThrown(IllegalArgumentException.class);
        } catch (IllegalArgumentException e) {
            assertEquals("After \"F-8\"?><tag\": Invalid attribute name \"white space\"", e.getMessage());
        }
    }
    
    @Test
    public void testInvalidAttributeName3() {
        try {
            doc.begin("tag").attr("0leadingdigit", 1).end();
            failExceptionNotThrown(IllegalArgumentException.class);
        } catch (IllegalArgumentException e) {
            assertEquals("After \"F-8\"?><tag\": Invalid attribute name \"0leadingdigit\"", e.getMessage());
        }
    }
    
    @Test
    public void testValidAttributeNames() {
        doc
            .begin("tag").attr("alphanumeric1234", 1)
                .begin("tag").attr("_leadingunderscore", 1)
                    .begin("tag").attr("tag.name.with.dots", 1)
                        .begin("tag").attr("tag_name_with_underscores", 1)
                        .end()
                    .end()
                .end()
            .end();
    }
    
    @Test
    public void testMultipleRootTags() {
        try {
            doc.begin("tag1").end();
            doc.begin("tag2").end();
            failExceptionNotThrown(IllegalStateException.class);
        } catch (IllegalStateException e) {
            assertEquals("Multiple root tags are not allowed", e.getMessage());
        }
    }
    
    @Test
    public void testOutputToFile() throws IOException {
        doc
            .begin("root")
                .attr("id", 1)
                .begin("shorttag").end()
                .begin("cdatatag").cdata("\"'<>& are escaped").end()
            .end();
        
        File tempFile = tempFile();
        try {
            doc.write(tempFile);
            assertTrue("Output file was not created", tempFile.exists());
            assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?><root id=\"1\"><shorttag/><cdatatag>&quot;&apos;&lt;&gt;&amp; are escaped</cdatatag></root>", 
                         readFile(tempFile));
        } finally {
            tempFile.delete();
        }
    }
    
    @Test
    public void testOutputStreamNotClosed() throws IOException {
        doc
            .begin("root")
                .attr("id", 1)
                .begin("shorttag").end()
                .begin("cdatatag").cdata("\"'<>& are escaped").end()
            .end();
        
        doc.write(new OutputStream() {
            @Override
            public void close() throws IOException {
                Assert.fail("OutputStream passed to write() was closed");
            }

            @Override
            public void write(int b) throws IOException {}
            
        });
        
    }
    
    private File tempFile() {
        String filename = Long.toString(Math.abs(new Random().nextLong()), 36);
        return new File(System.getProperty("java.io.tmpdir"), filename);
    }
    
    private String readFile(File file) throws IOException {
        
        BufferedReader reader = null;
        StringBuilder buffer = new StringBuilder();
        
        try {
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            String line;
            while ((line = reader.readLine()) != null)
                buffer.append(line);
        } finally {
            if (reader != null)
                reader.close();
        }
        
        return buffer.toString();
    }
    
    private void failExceptionNotThrown(Class<? extends Exception> klass) {
        Assert.fail(String.format("Exception of class %s was not thrown", klass.getName()));
    }
}