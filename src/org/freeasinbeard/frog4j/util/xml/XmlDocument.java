package org.freeasinbeard.frog4j.util.xml;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.Stack;

import org.freeasinbeard.frog4j.util.StringUtil;

/*  EXAMPLE
 *  ========

    XmlDocument doc = new XmlDocument();
    
    doc.begin("library")
        .begin("books")
            .begin("book")
                .attr("id", 1)
                .attr("author", "William Golding")
                .cdata("Lord of the Flies")
            .end() // end book
            .begin("book")
                .attr("id", 2)
                .attr("author", "J.K. Rowling")
                .cdata("Harry Potter and the Philosopher's Stone")
            .end() // end book
        .end() // end books
        .begin("employees")
            .begin("employee")
                .attr("name", "John Doe")
            .end() // end employee
        .end() // end employees
    .end(); // end library
    
    System.out.println(doc); 
    
*/

public class XmlDocument {
    
    private Charset charset;
    
    private Stack<String> unendedTags;
    private StringBuilder buffer;
    private boolean incompleteBeginTag;
    
    public XmlDocument(Charset charset) {
        this.charset = charset;
        unendedTags = new Stack<String>();
        buffer = new StringBuilder();
        incompleteBeginTag = false;
        append("<?xml version=\"1.0\" encoding=\"", charset.name(), "\" ?>");
    }
    
    public XmlDocument() {
        this(Charset.defaultCharset());
    }
    
    public XmlDocument begin(String tagname) {
        if (tagname.isEmpty())
            throw new IllegalArgumentException("Missing tagname");
        
        if (incompleteBeginTag)
            completeBeginTag(false);
            
        unendedTags.push(tagname);
        incompleteBeginTag = true;
        append("<", tagname);
        return this;
    }
    
    public <T> XmlDocument attr(String name, T value) {
        if (name == null || value == null)
            throw new IllegalArgumentException("Missing attribute name and/or value");
            
        if (!incompleteBeginTag) 
            throw new MalformedXmlDocumentException("Cannot use attr() here");
        
        append(" ", name,  "=", "\"", value, "\"");
        return this;
    }
    
    @SuppressWarnings("unchecked")
    public <T> XmlDocument cdata(T cdata) {
        if (incompleteBeginTag)
            completeBeginTag(false);
        
        append(cdata);
        return this;
    }
    
    public XmlDocument end() {
        if (unendedTags.isEmpty())
            throw new MalformedXmlDocumentException("Trailing end() has no matching begin()");
        
        String tagname = unendedTags.pop();
        if (incompleteBeginTag) {
            completeBeginTag(true);
        } else {
            append("</", tagname, ">");
        }
        
        return this;
    }
    
    public void write(OutputStream os) throws IOException {
        Writer writer = new BufferedWriter(new OutputStreamWriter(os, charset));
        writer.write(toString());
        writer.flush();
        // do not close the stream - that is the responsibility of the caller!
    }
    
    public void write(File file) throws FileNotFoundException, IOException {
        OutputStream os = new FileOutputStream(file);
        write(os);
        os.close();
    }
    
    @Override
    public String toString() {
        if (!unendedTags.isEmpty())
            throw new MalformedXmlDocumentException("Missing end() for " + StringUtil.join(unendedTags, ", "));
            
        return buffer.toString();
    }
    
    /*
     * AUXILIARIES
     */
    
    private <T> void append(T...values) {
        for (T v : values) {
            buffer.append(v);
        }
    }
    
    private void completeBeginTag(boolean shortTag) {
        append(shortTag ? "/>" : ">");
        incompleteBeginTag = false;
    }
}
