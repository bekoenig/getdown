package io.github.bekoenig.getdown.net;

import junit.framework.TestCase;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import static org.mockito.ArgumentMatchers.matches;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class ConnectorTest extends TestCase {

    public void testAddBasicAuthHeaderOnUserInfo() throws MalformedURLException, UnsupportedEncodingException {
        // GIVEN
        URL url = new URL("http://testuser:dJ78wKUHtojqCT4z%26%40jBJSEbCmi3%5Er9FSH%25za72h74Za%26S%40T%2AfDXbxND%2A9ijKan%26L25zD9tNDt3HwAc%25XmYJyFgn8wfCfrZ7%40SwbDx3k7g3%5Ej%21S%5EpRsZAWBJ39o%5EiCnK@testdomain.com/");
        URLConnection conn = mock(URLConnection.class);

        // WHEN
        Connector.addBasicAuthHeaderOnUserInfo(url, conn);

        // THEN
        verify(conn).setRequestProperty(matches("Authorization"), matches("Basic dGVzdHVzZXI6ZEo3OHdLVUh0b2pxQ1Q0eiZAakJKU0ViQ21pM15yOUZTSCV6YTcyaDc0WmEmU0BUKmZEWGJ4TkQqOWlqS2FuJkwyNXpEOXRORHQzSHdBYyVYbVlKeUZnbjh3ZkNmclo3QFN3YkR4M2s3ZzNeaiFTXnBSc1pBV0JKMzlvXmlDbks="));
    }
}
