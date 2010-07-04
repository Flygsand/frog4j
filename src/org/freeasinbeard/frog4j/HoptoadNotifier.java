package org.freeasinbeard.frog4j;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.log4j.spi.LoggingEvent;
import org.freeasinbeard.util.xml.XmlDocument;

public class HoptoadNotifier {
    private static final String NAME = "frog4j";
    private static final String VERSION = "0.9.1";
    private static final String URL = "http://github.com/mtah/frog4j";
    
    private String apiKey;
    private String environment;
    
    public HoptoadNotifier() {
        this(null, null);
    }
    
    public HoptoadNotifier(String apiKey, String environment) {
        this.apiKey = apiKey;
        this.environment = environment;
    }
    
    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }
    
    public void setEnvironment(String environment) {
        this.environment = environment;
    }
    
    public void notify(LoggingEvent event) throws IOException, HttpException {
        HttpURLConnection conn = null;
        int responseCode;
        String responseMessage;
        try {
            conn = buildAPIConnection();
            XmlDocument requestXml = buildRequestXml(new HoptoadNotice(event));
            requestXml.write(conn.getOutputStream());
            responseCode = conn.getResponseCode();
            responseMessage = conn.getResponseMessage();
        } finally {
            if (conn != null)
                conn.disconnect();
        }
        
        if (responseCode < 200 || responseCode >= 300)
            throw new HttpException(responseCode, responseMessage);
        
        return;
    }
    
    protected XmlDocument buildRequestXml(HoptoadNotice notice) {
        XmlDocument doc = new XmlDocument();
        doc.begin("notice")
            .attr("version", "2.0")
            .begin("api-key").cdata(apiKey).end()
            .begin("notifier")
                .begin("name").cdata(NAME).end()
                .begin("version").cdata(VERSION).end()
                .begin("url").cdata(URL).end()
            .end()
            .begin("error")
                .begin("class").cdata(notice.error().klass()).end()
                .begin("message").cdata(notice.error().message()).end()
                .begin("backtrace");
                for (StackTraceElement e : notice.error().backtrace()) {
                    doc.begin("line")
                        .attr("method", e.getMethodName())
                        .attr("file", e.getFileName())
                        .attr("number", e.getLineNumber())
                    .end();
                }
                doc.end() // end backtrace
            .end() // end error
            .begin("server-environment")
                .begin("environment-name").cdata(environment).end()
            .end() // end server-environment
        .end(); // end notice
    
        return doc;
    }
    
    private static HttpURLConnection buildAPIConnection() throws IOException { 
        try {
            URL apiURL = new URL("http://hoptoadapp.com/notifier_api/v2/notices");
            HttpURLConnection conn = (HttpURLConnection) apiURL.openConnection();
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-type", "text/xml");
            conn.setRequestProperty("Accept", "text/xml, application/xml");
            conn.setRequestMethod("POST");
            return conn;
        } catch (MalformedURLException e) { 
            assert false: e;
            return null;
        }
    } 
}
