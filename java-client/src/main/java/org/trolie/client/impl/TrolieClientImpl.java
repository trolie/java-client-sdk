package org.trolie.client.impl;

import org.apache.hc.client5.http.classic.HttpClient;
import org.trolie.client.TrolieClient;
import org.trolie.client.ratingproposals.ForecastRatingProposalUpdate;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class TrolieClientImpl implements TrolieClient {

	HttpClient httpClient;
	String baseUrl;
	
	@Override
	public void getInUseLimitForecasts(String monitoringSet) {}

	@Override
	public void subscribeToInUseLimitForecastUpdates() {}

	@Override
	public ForecastRatingProposalUpdate createForecastProposalUpdate() {
		return new ForecastRatingProposalUpdate(httpClient, baseUrl);
	}

	@Override
	public void getInUseLimits() {}

	@Override
	public void subscribeToInUseLimits() {}

	@Override
	public void updateRealTimeProposal() {}

}
