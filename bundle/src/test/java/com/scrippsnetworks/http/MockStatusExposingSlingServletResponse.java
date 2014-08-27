package com.scrippsnetworks.http;

import java.io.IOException;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.sling.commons.testing.sling.MockSlingHttpServletResponse;
import org.mockito.Mockito;

public class MockStatusExposingSlingServletResponse extends MockSlingHttpServletResponse {

	public int getHttpStatus() {
		return httpStatus;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	private int httpStatus;
	private final HttpServletResponse response;
	private String errorMessage;

	public MockStatusExposingSlingServletResponse(HttpServletResponse response) {
		this.response = response;
	}

	/**
	 * Override the default which throws a NotSupportedOperationException
	 */
	public void sendError(int sc) throws IOException {
		httpStatus = sc;
		response.sendError(sc);
	}

	/**
	 * Override the default which throws a NotSupportedOperationException
	 */

	public void sendError(int sc, String msg) throws IOException {
		httpStatus = sc;
		errorMessage = msg;
		response.sendError(sc, msg);
	}
	
	public ServletOutputStream getOutputStream() throws IOException {
		return Mockito.mock(ServletOutputStream.class);
	}

	@Override
	public String toString() {
		return "Status: <" + httpStatus + "> errorMessage <" + errorMessage + ">.";
	}

}

