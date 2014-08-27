package com.scrippsnetworks.wcm.search.impl;

import com.scrippsnetworks.wcm.search.SearchRequestHandler;
import com.scrippsnetworks.wcm.search.SearchRequestUrlUtility;
import com.scrippsnetworks.wcm.search.SearchRequestException;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.HttpResponse;
import org.apache.http.HttpEntity;
import org.apache.http.util.EntityUtils;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.HttpStatus;
import org.apache.http.HttpHost;

import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.lang.String;
import java.lang.Exception;
import java.lang.RuntimeException;
import java.util.Map;
import java.io.IOException;
import com.scrippsnetworks.wcm.search.SearchResponse;
import com.scrippsnetworks.wcm.search.impl.SearchResponseImpl;

/**
 * Implements a SearchRequestHandler which can make requests external search services using the http client provided in its
 * constructor.
 *
 * @author Scott Everett Johnson
 */
public class SearchRequestHandlerImpl implements SearchRequestHandler {

    private HttpClient httpClient;
    private HttpHost httpHost;
    private String serviceContext;
    private String siteName;

    private Log log = LogFactory.getLog(SearchRequestHandlerImpl.class);

    private SearchRequestHandlerImpl() {};

    /**
     * Initializes a new SearchRequestHandler with the provided HttpClient, HttpHost, and service context path.
     *
     * @param httpClient thread-safe HttpClient to use for requests
     * @param httpHost HttpHost of service endpoint (scheme, hostname, and port)
     * @param serviceContext context path for service requests (e.g., /cook-wcm/service)
     */
    public SearchRequestHandlerImpl(String siteName, HttpClient httpClient, HttpHost httpHost, String serviceContext) {
        this.siteName = siteName;
        this.httpClient = httpClient;
        this.httpHost = httpHost;
        this.serviceContext = serviceContext;
    }

    /**
     * {@inheritDoc}
     */
    public SearchResponse getResponse(String serviceName, Map<String,String> params) {

        HttpGet httpget = null;
        String payload = null;
        int httpCode = 0;
        String reasonPhrase = null;
        SearchResponseImpl retVal = null;
        Exception ex = null;
        String requestUrl = "";
        long clientStart = 0L;
        long clientEnd = 0L;

        try {

            // We'll use our own handler below to catch the exception we throw if we weren't initialized properly, so we
            // can use the same logic to construct our SearchResponse object.
            if (httpHost == null || httpClient == null || serviceContext == null) {
                String exHost = (httpHost == null) ? "" : httpHost.toURI();
                String exClient = (httpClient == null) ? "(null)" : "(initialized)";
                String exContext = (serviceContext == null) ? "(null)" : serviceContext;
                throw new IllegalStateException("handler not initialized properly: httpHost=" + exHost + " httpClient=" + exClient + " serviceContext=" + exContext);
            }
           
            requestUrl = SearchRequestUrlUtility.buildURL(siteName, httpHost.toURI(), serviceContext, serviceName, params);
            httpget = new HttpGet(requestUrl);

            clientStart = System.currentTimeMillis();
            HttpResponse response = httpClient.execute(httpHost, httpget, new BasicHttpContext());
            clientEnd = System.currentTimeMillis();

            // if we're here, we have a response with a status
            httpCode = response.getStatusLine().getStatusCode();
            reasonPhrase = response.getStatusLine().getReasonPhrase();

            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                HttpEntity entity = response.getEntity();

                if (entity != null) {
                    payload = EntityUtils.toString(entity);
                } else {
                    log.warn("SearchRequestHandler: null entity on OK response code");
                    httpget.abort();
                }
            }
        } catch (IllegalStateException ise) {
            ex = ise;
            log.warn("illegal state: " +  ise.getMessage());
        } catch (SearchRequestException sre) {
            ex = sre;
            log.warn("got SearchRequestException " + sre.getMessage(), sre);
        } catch (ClientProtocolException cpe) {
            ex = cpe;
            log.warn("got ClientProtocolException " + cpe.getMessage());
        } catch (IOException ioe) {
            ex = ioe;
            log.warn("got IOException " + ioe.getMessage() + " for " + requestUrl);
        } catch (Exception e) {
            ex = e;
            log.error("unexpected exception " + e.getMessage(), e);
        } finally {
            if (ex != null && httpget != null && !httpget.isAborted()) {
                httpget.abort();
            }
            if (httpget != null) {
                httpget.reset();
            }
            // Let end - start be negative if an exception occurred.
            retVal = new SearchResponseImpl(serviceName, requestUrl, httpCode, reasonPhrase, clientEnd - clientStart, ex, payload);
        }

        return retVal;
    }
}
