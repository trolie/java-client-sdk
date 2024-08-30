package org.trolie.client.ratingproposals;

import java.io.IOException;
import java.io.OutputStream;
import java.time.Instant;
import java.util.Map;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.trolie.client.model.ratingproposals.ForecastProposalHeader;
import org.trolie.client.model.ratingproposals.ForecastRatingProposalStatus;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.PooledByteBufAllocator;
import reactor.netty.ByteBufFlux;
import reactor.netty.http.client.HttpClient;

public class ForcastRatingProposalUpdate implements AutoCloseable {

	private enum Scope {
		header,
		rating,
		period,
	}

	HttpClient httpClient;

	Subscriber<? super ByteBuf> subscriber;
	JsonGenerator jsonGenerator;
	ByteBuf buffer;
	ByteBufOutputStream bufferOutputStream;
	Scope scope = Scope.header;

	public ForcastRatingProposalUpdate(HttpClient httpClient) {
		super();
		this.httpClient = httpClient;
	}

	public void begin() {
		httpClient.post().send(ByteBufFlux.fromInbound(
				new Publisher<ByteBuf>() {
					@Override
					public void subscribe(Subscriber<? super ByteBuf> s) {
						subscriber = s;
					}
				}
				));

		buffer = PooledByteBufAllocator.DEFAULT.buffer();
		bufferOutputStream = new ByteBufOutputStream(buffer,true);

	}

	public void header(ForecastProposalHeader header) throws IOException {

		JsonFactory jfactory = new JsonFactory();
		jsonGenerator = jfactory.createGenerator((OutputStream)bufferOutputStream);

		jsonGenerator.writeStartObject();
		jsonGenerator.writeFieldName("proposal-header");
		jsonGenerator.writeObject(header);
		jsonGenerator.writeArrayFieldStart("ratings");

	}

	public void resource(String resourceId) {

		scope = Scope.rating;
	}

	public void period(Instant periodStart, Instant periodEnd) {

		//validate that we are not in header scope
		
		scope = Scope.period;
		
	}

	public void continousOperatinglimit(Map<String, Float> limit) {

	}

	public void emergencyOperatingLimit(String durationName, Map<String, Float> limit) {

	}

	public void inputUsed(String name, String value, String unit) {

	}

	public ForecastRatingProposalStatus complete() throws IOException {
		jsonGenerator.writeEndArray();
		jsonGenerator.writeEndObject();
		bufferOutputStream.flush();
		subscriber.onComplete();
		
		return null;
	}

	@Override
	public void close() throws Exception {
		bufferOutputStream.close();
	}

}
