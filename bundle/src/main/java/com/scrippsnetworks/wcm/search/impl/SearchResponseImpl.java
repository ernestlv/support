package com.scrippsnetworks.wcm.search.impl;

import java.lang.String;
import java.lang.Integer;
import java.lang.Exception;
import java.util.Map;

import com.scrippsnetworks.wcm.search.SearchResponse;
import org.codehaus.jackson.map.ObjectMapper;

/**
 * Implements a SearchResponse which bundles together data related to a search request.
 *
 * @author Scott Everett Johnson
 */
public class SearchResponseImpl implements SearchResponse {

    private final String payload;
    private final int httpCode;
    private final String reasonPhrase;
    private final Exception exception;
    private final String serviceName;
    private final String serviceRequestURL;
    private final long requestTime;

    /* use static block to configure object mapper
     * static {
     *   objectMapper.configure(...);
     * }
     */

    /**
     * Constructor for SearchResponseImpl.
     *
     * @param serviceName Name of the service the request was made for.
     * @param serviceRequestURL The url of the request.
     * @param httpCode HTTP response code for request
     * @param reasonPhrase Text message sent by server with the response code
     * @param requestTime total time in milliseconds for the client request
     * @param exception If an exception occurred which prevented the request from being made or completing, it is passed in this argument.
     * @param payload The text of the response.
     */
    public SearchResponseImpl(String serviceName, String serviceRequestURL, Integer httpCode, String reasonPhrase, long requestTime, Exception exception, String payload) {
        this.serviceName = serviceName;
        this.httpCode = httpCode;
        this.reasonPhrase = reasonPhrase;
        this.requestTime = requestTime;
        this.exception = exception;
        this.payload = payload;
        this.serviceRequestURL = serviceRequestURL;
    }

    /* {@inheritDoc}
     */
    public boolean isValid() {
        return (exception == null && httpCode == 200 && payload != null && payload.length() > 0);
    }

    /* {@inheritDoc}
     */
    public String getPayload() {
        return payload;
    }

    /* {@inheritDoc}
     */
    public int getHttpCode() {
        return httpCode;
    }

    /* {@inheritDoc}
     */
    public String getReasonPhrase() {
        return reasonPhrase;
    }

    /* {@inheritDoc}
     */
    public long getRequestTime() {
        return requestTime;
    }

    /* {@inheritDoc}
     */
    public Exception getException() {
        return exception;
    }

    /* {@inheritDoc}
     */
    public String getServiceName() {
        return serviceName;
    }

    /* {@inheritDoc}
     */
    public String getServiceRequestURL() {
        return serviceRequestURL;
    }
}
