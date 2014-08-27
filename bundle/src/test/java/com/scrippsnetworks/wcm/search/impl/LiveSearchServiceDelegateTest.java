package com.scrippsnetworks.wcm.search.impl;

import static org.junit.Assert.assertTrue;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Map;

import org.junit.After;
import org.junit.Test;

import com.scrippsnetworks.wcm.search.SearchObjectMapper;
import com.scrippsnetworks.wcm.search.SearchRequestHandler;
import com.scrippsnetworks.wcm.search.SearchResponse;
import com.scrippsnetworks.wcm.search.impl.SearchServiceDelegate.ConfigStateCreationException;

public class LiveSearchServiceDelegateTest {

    public LiveSearchServiceDelegate lssd;
    private String serviceHostname = "cook-dev-search.scrippsnetworks.com";
    private int servicePort = 18080;
    private String serviceContext = "/cook-wcm/service";

    @After
    public void afterTest() {
        if (lssd != null) {
            lssd.deactivate();
        }
    }

    /**
     * Tests behavior of the search service / handler when used in multiple threads.
     * The test requires an available endpoint to make requests of, so probably shouldn't remain enabled?
     */
    // @Test
    public void testSuccessfulRequestThreaded() {

        System.out.println("running testSuccessfulRequestThread");

        final int POOL_SIZE = 10;
        final int CONNECTION_TIMEOUT = 10000;
        final int SOCKET_TIMEOUT = 10000;

        Dictionary params = new Hashtable<String,Object>();
        params.put(SearchServiceImpl.SERVICE_HOSTNAME,serviceHostname);
        params.put(SearchServiceImpl.SERVICE_PORT,new Integer(servicePort));
        params.put(SearchServiceImpl.SERVICE_CONTEXT,serviceContext);
        params.put(SearchServiceImpl.POOL_SIZE,new Integer(POOL_SIZE));
        params.put(SearchServiceImpl.CONNECTION_TIMEOUT,new Integer(CONNECTION_TIMEOUT));
        params.put(SearchServiceImpl.SOCKET_TIMEOUT,new Integer(SOCKET_TIMEOUT));
        setUpSearchService(params);


        // Request parameters
        String serviceName = "topRecipes";
        Map<String, String> requestParams;
        requestParams = new Hashtable<String, String>();
        requestParams.put("pageType","SHOW");
        requestParams.put("uid","1693");
        requestParams.put("numOfResults","10");
        requestParams.put("offset","0");

        int maxThreads = 500;

        long timeMillis = 0L;
        long timedThreads = 0L;
        long max = 0L;
        long min = Long.MAX_VALUE;
        SearchRequestThread[] threads;

        try {

            // warm things up
            threads = startRequestThreads(POOL_SIZE, serviceName, requestParams);
            threads = null;

            threads = startRequestThreads(maxThreads, serviceName, requestParams);

            for (int i = 0; i < maxThreads; i++) {
                assertTrue("nonnull response", threads[i].response != null);
                assertTrue("valid response", threads[i].response.isValid());
                assertTrue("200 response code", threads[i].response.getHttpCode() == 200);
                assertTrue("nonnull payload", threads[i].response.getPayload() != null);
                assertTrue("nonzero-length payload", threads[i].response.getPayload().length() > 0);
                Map<String,Object> map = SearchObjectMapper.getAsMap(threads[i].response);
                assertTrue("map created from object is nonnull", map != null);
                Map<String,Object> serviceResponse = (Map<String,Object>) map.get("ServiceResponse");
                assertTrue("service response object is nonnull", serviceResponse != null);
                assertTrue("service name is nonnull",serviceResponse.get("servicename") != null);
                assertTrue("service name matches requested service",serviceResponse.get("servicename").equals("topRecipes"));

                long thisTime = threads[i].response.getRequestTime();
                if (thisTime >= 0) {
                    timedThreads++;
                    timeMillis += thisTime;
                    max = max > thisTime ? max : thisTime;
                    min = min < thisTime ? min : thisTime;
                }
            }
            System.out.println("average request time " + timeMillis / timedThreads + " ms min=" + min + " max=" + max);

        } catch (Exception e) {
            System.out.println("Got unexpected exception " + e.getMessage());
        }
    }

    // @Test
    public void testSearch() {

        System.out.println("running testSearch");

        final int POOL_SIZE = 10;
        final int CONNECTION_TIMEOUT = 10000;
        final int SOCKET_TIMEOUT = 10000;

        Dictionary params = new Hashtable<String,Object>();
        params.put(SearchServiceImpl.SERVICE_HOSTNAME,serviceHostname);
        params.put(SearchServiceImpl.SERVICE_PORT,new Integer(servicePort));
        params.put(SearchServiceImpl.SERVICE_CONTEXT,serviceContext);
        params.put(SearchServiceImpl.POOL_SIZE,new Integer(POOL_SIZE));
        params.put(SearchServiceImpl.CONNECTION_TIMEOUT,new Integer(CONNECTION_TIMEOUT));
        params.put(SearchServiceImpl.SOCKET_TIMEOUT,new Integer(SOCKET_TIMEOUT));
        setUpSearchService(params);
 
        // Request parameters
        String serviceName = "search";
        String searchTerm = "chicken";
        Map<String, String> requestParams;
        requestParams = new Hashtable<String, String>();
        requestParams.put("searchTerm",searchTerm);
        requestParams.put("dimensions","european:4294959869,nutrition:4294959457");
        requestParams.put("sortBy","Total Time|0");
        requestParams.put("maxResults","10");
        requestParams.put("offset","0");

        int maxThreads = 10;

        long timeMillis = 0L;
        long timedThreads = 0L;
        long max = 0L;
        long min = Long.MAX_VALUE;
        SearchRequestThread[] threads;

        try {
             
            // warm things up
            threads = startRequestThreads(POOL_SIZE, serviceName, requestParams);
            threads = null;

            threads = startRequestThreads(maxThreads, serviceName, requestParams);

            for (int i = 0; i < maxThreads; i++) {
                System.out.println(threads[i].response.getServiceRequestURL());

                assertTrue("nonnull response", threads[i].response != null);
                assertTrue("valid response", threads[i].response.isValid());
                assertTrue("200 response code", threads[i].response.getHttpCode() == 200);
                assertTrue("nonnull payload", threads[i].response.getPayload() != null);
                assertTrue("nonzero-length payload", threads[i].response.getPayload().length() > 0);
                Map<String,Object> map = SearchObjectMapper.getAsMap(threads[i].response);
                assertTrue("map created from object is nonnull", map != null);
                assertTrue("search term matches requested term",map.get("searchTerm").equals(searchTerm));
                System.out.println("searchTerm=" + map.get("searchTerm"));

                System.out.println(threads[i].response.getServiceRequestURL());

                long thisTime = threads[i].response.getRequestTime();
                if (thisTime >= 0) {
                    timedThreads++;
                    timeMillis += thisTime;
                    max = max > thisTime ? max : thisTime;
                    min = min < thisTime ? min : thisTime;
                }
            }
            System.out.println("average request time " + timeMillis / timedThreads + " ms min=" + min + " max=" + max);

        } catch (Exception e) {
            System.out.println("Got unexpected exception " + e.getMessage());
        }
    }

    /**
     * Tests behavior when a search request handler is passed bad arguments. The handler is expected to return
     * an invalid response (isValid() == false) which contains an exception (getException()) which identifies the
     * error that occurred.
     */
    // @Test
    public void testBadRequestParameters() {
        System.out.println("running testBadRequestParameters");
        Dictionary params = new Hashtable<String,Object>();
        params.put(SearchServiceImpl.SERVICE_HOSTNAME,serviceHostname);
        params.put(SearchServiceImpl.SERVICE_PORT,new Integer(servicePort));
        params.put(SearchServiceImpl.SERVICE_CONTEXT,serviceContext);
        params.put(SearchServiceImpl.POOL_SIZE,new Integer(1));
        params.put(SearchServiceImpl.CONNECTION_TIMEOUT,new Integer(5000));
        params.put(SearchServiceImpl.SOCKET_TIMEOUT,new Integer(5000));
        setUpSearchService(params);

        // Request parameters
        Map<String, String> requestParams;
        String serviceName = "topRecipes";
        requestParams = new Hashtable<String, String>();
        requestParams.put("pageType","SHOW");
        // requestParams.put("assetId","1693");

        int maxThreads = 1;

        SearchRequestThread[] threads;

        try {
            threads = startRequestThreads(maxThreads, serviceName, requestParams);

            for (int i = 0; i < maxThreads; i++) {
                // should always get a response object
                assertTrue("nonnull response", threads[i].response != null);
                // this response object should be flagged invalid (no request was even made)
                assertTrue("invalid response", threads[i].response.isValid() == false);
                // the response should carry the exception that was thrown
                assertTrue("exception in response", threads[i].response.getException() != null);
                // the exception should be informative
                assertTrue("exception message says what problem is",
                        threads[i].response.getException().getMessage().contains("required parameter assetId not provided"));
            }

        } catch (Exception e) {
            System.out.println("Got unexpected exception " + e.getMessage());
        }
    }

    /**
     * Tests behavior when a search request handler from an improperly configured search service instance is used.
     */
    // @Test(expected=IllegalStateException.class)
    public void testImproperOSGiConfig() throws IllegalStateException {

        System.out.println("running testImproperOSGiConfig");

        final int POOL_SIZE = 25;
        final int CONNECTION_TIMEOUT = 10000;
        final int SOCKET_TIMEOUT = 10000;

        // Try an improperly set up service. The service should not register properly if the activate failed.
        // However, we want to make sure that when the configuration is wonky the search request handler will
        // fail gracefully, returning an invalid response.
        try {
            Dictionary params = new Hashtable<String,Object>();
            // we expect an exception here, improperly configuring the service...we intend to try using it anyway
            setUpSearchService(params); 
        } catch (Exception e) {
            System.out.println("got expected exception setting up search service " + e.getMessage());
        } 

        // Request parameters
        String serviceName = "topRecipes";
        Map<String, String> requestParams;
        requestParams = new Hashtable<String, String>();
        requestParams.put("pageType","SHOW");
        requestParams.put("uid","1693");

        int maxThreads = 2;

        long timeMillis = 0L;
        long timedThreads = 0L;
        long max = 0L;
        long min = Long.MAX_VALUE;
        
        SearchRequestThread[] threads;

        threads = startRequestThreads(maxThreads, serviceName, requestParams);

        for (int i = 0; i < maxThreads; i++) {
            assertTrue("response object flagged invalid", threads[i].response.isValid() == false);
            assertTrue("response object has exception set", threads[i].response.getException() != null);
            System.out.println(threads[i].response.getException().getMessage());
        }

    }

    /**
     * Utility method to encapsulate search service setup, and translate the implementation-specific checked exception
     * to a runtime exception.
     */
    private void setUpSearchService(Dictionary<String,String> params) {
        lssd = new LiveSearchServiceDelegate();
        try {
            lssd.activate(params);
        } catch (ConfigStateCreationException csce) {
            throw new RuntimeException("error setting up search service", csce);
        }
    }

    private SearchRequestThread[] startRequestThreads(int maxThreads, String serviceName, Map<String, String> requestParams) throws IllegalStateException {
        SearchRequestThread[] threads = new SearchRequestThread[maxThreads];

        for (int i = 0; i < maxThreads; i++) {
            threads[i] = new SearchRequestThread(lssd.getSearchRequestHandler(), i, serviceName, requestParams);
        }

        for (int i = 0; i < maxThreads; i++) {
            threads[i].start();
            // Throttle requests a bit to avoid too many simultaneous threads, since we're probably hitting a single server.
            if (((i + 1) % 5) == 0) {
                try {
                    Thread.sleep(250);
                } catch (InterruptedException ie) {
                    // uh...im...hmm
                }
            }
        }

        for (int i = 0; i < maxThreads; i++) {
            try {
                threads[i].join();
            } catch (InterruptedException ie) {
                // uh...um...hmmm
            }
        }

        return threads;
    }

    static class SearchRequestThread extends Thread {

        private SearchRequestHandler srh;
        public int threadNo;
        public long timer;
        public long start;
        public long end;
        public Exception exception;
        public SearchResponse response = null;
        private Map<String,String> requestParams;
        public String serviceName;

        public SearchRequestThread(SearchRequestHandler srh, int threadNo, String serviceName, Map<String,String> requestParams) {
            this.srh = srh;
            this.threadNo = threadNo;
            this.serviceName = serviceName;
            this.requestParams = requestParams;
        }

        @Override
        public void run() {
            start = System.currentTimeMillis();
            try {
                response = srh.getResponse(serviceName,requestParams);
            } catch (Exception e) {
                System.out.println("caught exception calling srh.getResponse");
                e.printStackTrace();
                exception = e;
            } finally {
                end = System.currentTimeMillis();
                timer = end - start;
            }
        }
    }

}
