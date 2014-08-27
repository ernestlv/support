package com.scrippsnetworks.wcm.search;

/**
 * Encapsulates a search response along with information about the request.
 * <ul>
 *  <li>the service name of the request</li>
 *  <li>the url of the request</li>
 *  <li>the http response code of the request</li>
 *  <li>the "reason phrase", or textual representation of the response code</li>
 *  <li>the json payload itself, as a string</li>
 * </ul>
 * In addition, if an exception was encountered during processing it is stored in the response object.
 * A simple method is also provided to determine if the response is valid, where validity means
 * <ul>
 *  <li>no exceptions occurred</li>
 *  <li>the http response code was OK</li>
 *  <li>the payload is present and has nonzero length</li>
 * </ul>
 * If the response is valid, the response should be deserializable. However, if the json is mailformed
 * deserialization may fail.
 * @author Scott Everett Johnson
 */
public interface SearchResponse {
    /**
     * The http response code from the request. Will be 0 if not relevant because a request wasn't made or response recieved.
     *
     * @return A primitive integer representing the http response code, or zero if no request was made or a response wasn't
     * recieved.
     * @see org.apache.http.HttpStatus
     */
    public int getHttpCode();
    
    /**
     * Retrieves the text sent with the response.
     *
     * @return String recieved from the server, or null if the request wasn't made or a response recieved.
     */
    public String getReasonPhrase();

    /**
     * Retrieves the time in milliseconds taken for the http client to get the response.
     *
     * @return Native long value of milliseconds for http client request.
     */
    public long getRequestTime();

    /**
     * Retrieves the string payload of the request.
     *
     * @return String value of service response, or null if an exception was thrown during request processing or the response
     * body was empty.
     */
    public String getPayload();

    /**
     * Retrieves any exceptions encountered during processing.
     *
     * @return Exception, or null if no exceptions occurred.
     */
    public Exception getException();

    /**
     * Retrieves the service name used in the request.
     *
     * @return String value of the service name.
     */
    public String getServiceName();

    /**
     * Retrieves the absolute url used in the request, including the hostname, port, and full path.
     *
     * @return String value of url used in the request, or null if url construction failed. In that case, an exception should
     * indicate why the url couldn't be constructed.
     */
    public String getServiceRequestURL();

    /**
     * Simple boolean property indicating whether the HTTP request was completed successfully.
     *
     * @return true if there were no exceptions, the http response code was OK, and the response length is nonzero.
     */
    public boolean isValid();
}
