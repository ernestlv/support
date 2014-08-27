package com.scrippsnetworks.wcm.search;

import java.lang.String;
import java.lang.Exception;
import java.lang.Throwable;

/** Exception used for errors related to search requests. 
 * @see Exception
 */
public class SearchRequestException extends Exception {

    public SearchRequestException(String msg) {
        super(msg);
    }

    public SearchRequestException(String msg, Throwable cause) {
        super(msg, cause);
    }

    private SearchRequestException() {
        // don't allow this
    }

}
