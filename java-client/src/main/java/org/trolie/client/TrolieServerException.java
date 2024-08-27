package org.trolie.client;

/**
 * Exception that indicates a failure on the side of the TROLIE endpoint.
 * Covers any non-functional failures, such as connection-loss, HTTP 500 errors etc.
 */
public class TrolieServerException extends TrolieException {
}
