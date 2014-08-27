package com.scrippsnetworks.wcm.image;

import java.lang.RuntimeException;
import java.lang.Throwable;

public class RenditionGenerationException extends RuntimeException {
    public RenditionGenerationException() {
        super();
    }

    public RenditionGenerationException(String message) {
        super(message);
    }

    public RenditionGenerationException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public RenditionGenerationException(Throwable cause) {
        super(cause);
    }
}
