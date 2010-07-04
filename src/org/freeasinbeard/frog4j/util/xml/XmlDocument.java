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
import java.util.regex.Pattern;

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
    
    private static Pattern identifierPattern = Pattern.compile("^[a-zA-Z_][a-zA-Z0-9\\._-]*$");
    private Stack<String> unendedTags;
    private StringBuilder buffer;
    private boolean incompleteBeginTag;
    
    public XmlDocument(Charset charset) {
        this.charset = charset;
        unendedTags = new Stack<String>();
        buffer = new StringBuilder();
        incompleteBeginTag = false;
    }
    
    public XmlDocument() {
        this(Charset.defaultCharset());
    }
    
    public XmlDocument begin(String tagName) {
        if (tagName == null)
            throw new IllegalArgumentException("Tag name cannot be null");
        
        if (buffer.length() == 0)
            append("<?xml version=\"1.0\" encoding=\"", charset.name(), "\"?>");
        else if (unendedTags.isEmpty())
            throw new IllegalStateException("Multiple root tags are not allowed");
        
        if (!isValidIdentifier(tagName))
            throw new IllegalArgumentException(errorInContext("Invalid tag name \"%s\"", tagName));

        if (incompleteBeginTag)
            completeBeginTag(false);
            
        unendedTags.push(tagName);
        incompleteBeginTag = true;
        append("<", tagName);
        return this;
    }
    
    public <T> XmlDocument attr(String name, T value) {
        if (name == null)
            throw new IllegalArgumentException("Attribute name cannot be null");
        
        if (!isValidIdentifier(name))
            throw new IllegalArgumentException(errorInContext("Invalid attribute name \"%s\"", name));
            
        if (!incompleteBeginTag) 
            throw new IllegalStateException(errorInContext("attr() can only be applied to begin()"));
        
        append(" ", name,  "=", "\"", escape(value), "\"");
        return this;
    }
    
    public <T> XmlDocument cdata(T cdata) {
        if (incompleteBeginTag)
            completeBeginTag(false);
        
        append(escape(cdata));
        return this;
    }
    
    public XmlDocument end() {
        if (unendedTags.isEmpty())
            throw new IllegalStateException("Trailing end() has no matching begin()");
        
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
            throw new IllegalStateException("Missing end() for " + StringUtil.join(unendedTags, ", "));
            
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
    
    private <T> boolean isValidIdentifier(T value) {
        return identifierPattern.matcher(value + "").matches();
    }
    
    private <T> String escape(T value) {
        String str = (value + "")
            .replace("&", "&amp;")
            .replace("\"", "&quot;")
            .replace("'", "&apos;")
            .replace("<", "&lt;")
            .replace(">", "&gt;");
            
        return str;
    }
    
    private <T> String errorInContext(String format, T...args) {
        String context = buffer.substring(Math.max(0, buffer.length() - 10));
        return String.format((context.isEmpty() ? "" : "After \"" + context + "\": ") + format, args);
    }
    
    private void completeBeginTag(boolean shortTag) {
        append(shortTag ? "/>" : ">");
        incompleteBeginTag = false;
    }
}
