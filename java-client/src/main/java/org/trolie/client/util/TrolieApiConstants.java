package org.trolie.client.util;

public class TrolieApiConstants {

	public static final String PATH_REALTIME_SNAPSHOT = "/limits/realtime-snapshot";
	public static final String PATH_FORECAST_SNAPSHOT = "/limits/forecast-snapshot";
	
	public static final String CONTENT_TYPE_REALTIME_SNAPSHOT = "application/vnd.trolie.realtime-limits-snapshot.v1+json";
	public static final String CONTENT_TYPE_FORECAST_SNAPSHOT = "application/vnd.trolie.forecast-limits-snapshot.v1+json";
	
	public static final String PARAM_MONITORING_SET = "monitoring-set";
	public static final String PARAM_TRANSMISSION_FACILITY = "transmission-facility";
	public static final String PARAM_PERIOD_START = "period-start";
	public static final String PARAM_PERIOD_END = "period-end";
	public static final String PARAM_OFFSET_PERIOD_START = "offset-period-start";
	public static final String PARAM_OFFSET_PERIOD_END = "offset-period-end";
	
}
