package org.trolie.client.impl;

import java.util.Iterator;

import org.trolie.client.TrolieClient;
import org.trolie.client.model.ratingproposals.ForecastProposalHeader;
import org.trolie.client.model.ratingproposals.ForecastRating;
import org.trolie.client.model.ratingproposals.ForecastRatingProposalStatus;
import org.trolie.client.ratingproposals.ForcastRatingProposalUpdate;

import lombok.AllArgsConstructor;
import reactor.netty.http.client.HttpClient;

@AllArgsConstructor
public class TrolieClientImpl implements TrolieClient {

	HttpClient httpClient;
	
	@Override
	public void getInUseLimitForecasts(String monitoringSet) {}

	@Override
	public void subscribeToInUseLimitForecastUpdates() {}

	@Override
	public ForcastRatingProposalUpdate createForecastProposalUpdate() {
		return new ForcastRatingProposalUpdate(httpClient);
	}

	@Override
	public void getInUseLimits() {}

	@Override
	public void subscribeToInUseLimits() {}

	@Override
	public void updateRealTimeProposal() {}

}
