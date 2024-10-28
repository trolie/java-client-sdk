package org.trolie.client.request.monitoringsets;

import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.function.Function;

import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.classic.methods.HttpPatch;
import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.HttpHost;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.trolie.client.TrolieException;
import org.trolie.client.model.monitoringsets.MonitoringSet;
import org.trolie.client.request.streaming.AbstractStreamingUpdate;
import org.trolie.client.util.TrolieApiConstants;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.trolie.client.util.CommonConstants.TAG_SOURCE;
import static org.trolie.client.util.CommonConstants.TAG_ID;
import static org.trolie.client.util.CommonConstants.TAG_DESCRIPTION;
import static org.trolie.client.util.CommonConstants.TAG_POWER_SYSTEM_RESOURCES;

public class MonitoringSetsGet extends AbstractStreamingUpdate<MonitoringSetsGet> {

	private static final Logger logger = LoggerFactory.getLogger(MonitoringSetsGet.class);


	public MonitoringSetsGet(HttpClient httpClient, HttpHost host, RequestConfig requestConfig,
										ThreadPoolExecutor threadPoolExecutor, int bufferSize, ObjectMapper objectMapper, 
										Map<String, String> httpHeader, boolean enableCompression ) {
		super(httpClient, host, requestConfig, threadPoolExecutor, bufferSize, objectMapper, httpHeader, enableCompression);
	}

	public static final String PATH = TrolieApiConstants.PATH_MONITORING_SET_ID;
	public static final String CONTENT_TYPE = TrolieApiConstants.CONTENT_TYPE_MONITORING_SET;

	private enum Scope {
		BEGIN,
		MAIN,
		END
	}

	JsonGenerator jsonGenerator;
	Scope scope = Scope.BEGIN;

	@Override
	protected HttpUriRequestBase getRequest() {
		HttpPatch httpPatch = new HttpPatch(PATH);
		httpPatch.addHeader(HttpHeaders.CONTENT_TYPE, CONTENT_TYPE);
		return httpPatch;
	}

	@Override
	protected ContentType getContentType() {
		return ContentType.create(CONTENT_TYPE);
	}

	@Override
	protected String getPath() {
		return PATH;
	}

	@Override
	protected Function<HttpEntity, MonitoringSetsGet> getResponseHandler() {
		return e -> {
			try {
				return objectMapper.readValue(e.getContent(), MonitoringSetsGet.class);
			} catch (Exception e2) {	
				throw new TrolieException("Failed to parse response",e2);
			}
		};
	}

	public void begin(MonitoringSet monitoringSet) {
		validateScope(Scope.MAIN, Scope.BEGIN);
		try {
			jsonGenerator = new JsonFactory().createGenerator(createRequestOutputStream());
		} catch (Exception e) {
			throw new TrolieException("Error creating request output stream",e);
		}
		
		checkCanWrite();

		try {	
			jsonGenerator.setCodec(objectMapper);
			jsonGenerator.writeStartObject();
			jsonGenerator.writeFieldName(TAG_SOURCE);
			jsonGenerator.writeObject(monitoringSet.getSource());
			jsonGenerator.writeFieldName(TAG_ID);
			jsonGenerator.writeObject(monitoringSet.getId());
			jsonGenerator.writeFieldName(TAG_DESCRIPTION);
			jsonGenerator.writeObject(monitoringSet.getDescription());
			jsonGenerator.writeFieldName(TAG_POWER_SYSTEM_RESOURCES);
			jsonGenerator.writeObject(monitoringSet.getPowerSystemResources());
		} catch (Exception e) {
			handleWriteError(e);
		}

	}

	public MonitoringSetsGet complete() {
		checkCanWrite();
		try {
			validateScope(Scope.END, Scope.MAIN);
			jsonGenerator.writeEndArray();
			jsonGenerator.writeEndObject();
			jsonGenerator.close();
		} catch (Exception e) {
			handleWriteError(e);
		}
		return completeRequest();
	}

	@Override
	public void close() {
		super.close();
		if (jsonGenerator != null && !jsonGenerator.isClosed()) {
			try {
				jsonGenerator.close();
			} catch (Exception e) {
				logger.error("Error closing JSON generator", e);
			}
		}
	}

	private void validateScope(Scope nextScope, Scope... acceptableScopes) {

		boolean acceptable = false;

		for (Scope acceptableScope : acceptableScopes) {
			if (scope == acceptableScope) {
				acceptable = true;
				break;
			}
		}

		if (!acceptable) {
			throw new IllegalStateException(String.format("Illegal transition from scope %s to %s", scope, nextScope));
		}

		scope = nextScope;
	}

}
