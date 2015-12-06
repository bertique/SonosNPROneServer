package me.michaeldick.sonosnpr;

import static javax.servlet.http.HttpServletResponse.SC_OK;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.*;

import org.apache.cxf.helpers.IOUtils;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;

public class HttpTestServer {
	public static int HTTP_PORT = 50036;
    
    private Server _server;
    private String _responseBody;
    private String _requestBody;
    private String _mockResponseData;
 
    public HttpTestServer(int port) {
    	HTTP_PORT = port;
    }
    
    public HttpTestServer() {
    }
     
    public HttpTestServer(String mockData) {
        setMockResponseData(mockData);
    }
     
    public void start() throws Exception {
        configureServer();
        startServer();
    }
 
    private void startServer() throws Exception {
        _server.start();
    }
 
    protected void configureServer() {
        _server = new Server(HTTP_PORT);
        _server.setHandler(getMockHandler());
    }
 
    /**
     * Creates an {@link AbstractHandler handler} returning an arbitrary String as a response.
     * 
     * @return never <code>null</code>.
     */
    public Handler getMockHandler() {
        Handler handler = new AbstractHandler() {          
            public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {               
                setResponseBody(getMockResponseData());
                setRequestBody(IOUtils.toString(baseRequest.getInputStream()));
                response.setStatus(SC_OK);
                response.setContentType("application/json");
                response.getWriter().write(getResponseBody());
                baseRequest.setHandled(true);
            }
        };
        return handler;
    }
 
    public void stop() throws Exception {
        _server.stop();
    }
 
    public void setResponseBody(String responseBody) {
        _responseBody = responseBody;
    }
 
    public String getResponseBody() {
        return _responseBody;
    }
 
    public void setRequestBody(String requestBody) {
        _requestBody = requestBody;
    }
 
    public String getRequestBody() {
        return _requestBody;
    }
     
    public static void main(String[] args) {
        HttpTestServer server = new HttpTestServer();
        server.setMockResponseData("<xml><testdata>hello, world</testdata></xml>");
        try {
            server.start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
 
    public void setMockResponseData(String mockResponseData) {
        _mockResponseData = mockResponseData;
    }
 
    public String getMockResponseData() {
        return _mockResponseData;
    }
     
    protected Server getServer() {
        return _server;
    }
}
