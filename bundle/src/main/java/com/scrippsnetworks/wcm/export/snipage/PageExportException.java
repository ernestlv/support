package com.scrippsnetworks.wcm.export.snipage;

/** Exception thrown when there is a problem writing export XML.
 *
 * This exception wraps any implementation-dependent exceptions so they are not exposed.
 */
public class PageExportException extends Exception {

    PageExportException() {
        super();
    }

    PageExportException(String message) {
        super(message);
    }

    PageExportException(String message, Throwable cause) {
        super(message, cause);
    }

    PageExportException(Throwable cause) {
        super(cause);
    }
}
