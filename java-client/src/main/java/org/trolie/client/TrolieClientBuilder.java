package org.trolie.client;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.HttpHost;
import org.trolie.client.exception.TrolieException;
import org.trolie.client.impl.MemoryETagStore;
import org.trolie.client.impl.TrolieClientImpl;
import org.trolie.client.request.monitoringsets.MonitoringSetsSubscribedReceiver;
import org.trolie.client.request.operatingsnapshots.ForecastSnapshotSubscribedReceiver;
import org.trolie.client.request.operatingsnapshots.RealTimeSnapshotSubscribedReceiver;

import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>Builder class used to construct new instances of the TROLIE client.</p>
 * <p>When configuring the TROLIE client, users should specifically be aware
 * of two things:</p>
 * <ul>
 *     <li>The TROLIE client builds on the
 *     <a href="https://hc.apache.org/">Apache Commons HTTP Client.</a>
 *     While a default may be created for naive users, the client implementation
 *     itself is injectable, and users may control most of the advanced
 *     options of the HTTP client (proxies etc) as needed for their
 *     implementations.  </li>
 *     <li>Correct implementation of an {@link ETagStore} is critical to
 *     consuming ratings in a robust way.  This marks the current version of
 *     data that this application (the TROLIE SDK user) has in its knowledge.
 *     The default implementation simply stores in memory.  This may be
 *     fine for many uses, but applications may want to use their own storage
 *     (such as a database) for current data values.</li>
 * </ul>
 *
 */
public class TrolieClientBuilder {

	/**
	 * Default buffer size for BufferedInputStreams and BufferedOutputStreams.
	 */
	public static final int DEFAULT_BUFFER_SIZE = 4096;

	private final HttpHost host;
	private final CloseableHttpClient httpClient;
	private RequestConfig requestConfig;
	private int bufferSize = DEFAULT_BUFFER_SIZE;
	private ObjectMapper objectMapper;
	private ETagStore eTagStore;
	private Map<String, String> httpHeaders = new HashMap<>();
	private int periodLengthMinutes = 60;

	private int realTimeRatingsPollMs = 10000;
	private int forecastRatingsPollMs = 30000;
	private int monitoringSetPollMs = 60000;

	/**
	 * Initializes a new builder with a preconfigured apache HTTP client
	 * @param baseUrl URL to the TROLIE service, such as https:&#47;&#47;trolie.example.com.
	 */
	public TrolieClientBuilder(
			String baseUrl) {
		this(baseUrl, HttpClients.createDefault());
	}

	/**
	 * Initializes a new builder.
	 * @param baseUrl URL to the TROLIE service, such as https:&#47;&#47;trolie.example.com.
	 * @param httpClient a pre-configured Apache HTTP client.
	 *                   The TROLIE client is built on top of the Apache HTTP client, and the full suite
	 *                   of configuration options for it are available.  See examples for more detail.
	 */
	public TrolieClientBuilder(
			String baseUrl, 
			CloseableHttpClient httpClient) {
		super();
		try {
			this.host = HttpHost.create(baseUrl);
		} catch (URISyntaxException e) {
			throw new TrolieException(e);
		}
		this.httpClient = httpClient;
	}

	/**
	 * Override the HTTP client request config
	 * @param config overridden config
	 * @return fluent builder
	 */
	public TrolieClientBuilder requestConfig(RequestConfig config) {
		this.requestConfig = config;
		return this;
	}

	/**
	 * Override the buffer size used in Java BufferedInputStreams and BufferedOutputStreams
	 * to optimize performance.  Defaults to {@link #DEFAULT_BUFFER_SIZE}.
	 * @param bufferSize new buffer size
	 * @return fluent builder.
	 */
	public TrolieClientBuilder bufferSize(int bufferSize) {
		this.bufferSize = bufferSize;
		return this;
	}

	/**
	 * Overrides configuration of the Jackson object mapper using for JSON parsing and serialization
	 * @param objectMapper new object mapper
	 * @return fluent builder
	 */
	public TrolieClientBuilder objectMapper(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
		return this;
	}

	/**
	 * Overrides the ETag Store used to cache the version of data stored by this system.
	 * @param eTagStore new store
	 * @return fluent builder.
	 */
	public TrolieClientBuilder etagStore(ETagStore eTagStore) {
		this.eTagStore = eTagStore;
		return this;
	}

	/**
	 * Allows for additional headers to be passed with every request.
	 * @param httpHeaders Key value pairs of additional header set to be passed with
	 *                    every request.
	 * @return fluent builder
	 */
	public TrolieClientBuilder httpHeaders(Map<String, String> httpHeaders) {
		this.httpHeaders.clear();
		this.httpHeaders.putAll(httpHeaders);
		return this;
	}

	/**
	 * Sets the period length assumed for forecast ratings.  Defaults to 60 minutes.
	 * @param periodLengthMinutes new assumed period length.
	 * @return fluent builder
	 */
	public TrolieClientBuilder periodLength(int periodLengthMinutes) {
		this.periodLengthMinutes = periodLengthMinutes;
		return this;
	}

	/**
	 * Sets the period at which
	 * {@link TrolieClient#subscribeToInUseLimitForecastUpdates(ForecastSnapshotSubscribedReceiver)}
	 * and similar methods for forecast ratings poll for new ratings.  Defaults to 30 seconds
	 * @param forecastRatingsPollMs new poll periodicity in milliseconds
	 * @return fluent builder
	 */
	public TrolieClientBuilder forecastRatingsPollMs(int forecastRatingsPollMs) {
		this.forecastRatingsPollMs = forecastRatingsPollMs;
		return this;
	}

	/**
	 * Sets the period at which
	 * {@link TrolieClient#subscribeToInUseLimits(RealTimeSnapshotSubscribedReceiver)}
	 * and similar methods poll for new real-time ratings.  Defaults to 10 seconds
	 * @param realTimeRatingsPollMs new poll periodicity in milliseconds
	 * @return fluent builder
	 */
	public TrolieClientBuilder realTimeRatingsPollMs(int realTimeRatingsPollMs) {
		this.realTimeRatingsPollMs = realTimeRatingsPollMs;
		return this;
	}

	/**
	 * Sets the period at which
	 * {@link TrolieClient#subscribeToDefaultMonitoringSetUpdates(MonitoringSetsSubscribedReceiver)}
	 * and similar methods poll for monitoring sets.  Defaults to 60 seconds
	 * @param monitoringSetPollMs new poll periodicity in milliseconds
	 * @return fluent builder
	 */
	public TrolieClientBuilder monitoringSetPollMs(int monitoringSetPollMs) {
		this.monitoringSetPollMs = monitoringSetPollMs;
		return this;
	}

	/**
	 * Construct a new client
	 * @return new client
	 */
    public TrolieClient build() {
    	
    	if (requestConfig == null) {
    		requestConfig = RequestConfig.DEFAULT;
    	}
    	
    	if (objectMapper == null) {
    		objectMapper = new ObjectMapper();
    	}

    	if (eTagStore == null) {
    		eTagStore = new MemoryETagStore();
    	}

		if (httpHeaders == null) {
			httpHeaders = new HashMap<>();
		}

    	return new TrolieClientImpl(httpClient, host, requestConfig, bufferSize,
				objectMapper, eTagStore, httpHeaders, periodLengthMinutes,
				realTimeRatingsPollMs,
				forecastRatingsPollMs, monitoringSetPollMs);
    }
}
