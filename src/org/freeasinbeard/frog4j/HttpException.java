package org.freeasinbeard.frog4j;

public class HttpException extends Exception {
    private static final long serialVersionUID = 1928801752108434461L;

    public HttpException(int responseCode, String responseMessage) {
        super(String.format("%d: %s", responseCode, responseMessage));
    }
}
