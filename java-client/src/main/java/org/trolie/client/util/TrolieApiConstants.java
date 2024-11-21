package org.trolie.client.util;

public class TrolieApiConstants {

	private TrolieApiConstants() {
	}

	public static final String PATH_REALTIME_SNAPSHOT = "/limits/realtime-snapshot";
	public static final String PATH_REALTIME_PROPOSAL = "/rating-proposals/realtime";
	public static final String PATH_FORECAST_SNAPSHOT = "/limits/forecast-snapshot";
	public static final String PATH_FORECAST_PROPOSAL = "/rating-proposals/forecast";
	public static final String PATH_MONITORING_SET_ID = "/monitoring-sets";
	public static final String PATH_DEFAULT_MONITORING_SET = "/default-monitoring-set";
	
	public static final String CONTENT_TYPE_REALTIME_SNAPSHOT = "application/vnd.trolie.realtime-limits-snapshot.v1+json";
	public static final String CONTENT_TYPE_REALTIME_PROPOSAL = "application/vnd.trolie.rating-realtime-proposal-status.v1+json";
	public static final String CONTENT_TYPE_FORECAST_SNAPSHOT = "application/vnd.trolie.forecast-limits-snapshot.v1+json";
	public static final String CONTENT_TYPE_FORECAST_PROPOSAL = "application/vnd.trolie.rating-forecast-proposal.v1+json";
	public static final String CONTENT_TYPE_MONITORING_SET = "application/vnd.trolie.monitoring-set.v1+json";
	
	public static final String PARAM_MONITORING_SET = "monitoring-set";
	public static final String PARAM_RESOURCE_ID = "resource-id";
	public static final String PARAM_OFFSET_PERIOD_START = "offset-period-start";
	public static final String PARAM_PERIOD_END = "period-end";
	
}
