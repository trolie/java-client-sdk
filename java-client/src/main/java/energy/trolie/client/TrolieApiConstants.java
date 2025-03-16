package energy.trolie.client;

/**
 * Series of static constants against the TROLIE OpenAPI specification
 */
public class TrolieApiConstants {

	private TrolieApiConstants() {
	}

	/**
     * Path to <a href="https://trolie.energy/spec-1.0#tag/Real-Time/operation/getRealTimeLimits">getRealTimeLimits</a>
     */
	public static final String PATH_REALTIME_SNAPSHOT = "/limits/realtime-snapshot";

	/**
     * Path to <a href="https://trolie.energy/spec-1.0#tag/Real-Time/operation/getRegionalRealTimeLimits">getRegionalRealTimeLimits</a>
     */
	public static final String PATH_REGIONAL_REALTIME_SNAPSHOT = "/limits/regional/realtime-snapshot";

	/**
     * Path to <a href="https://trolie.energy/spec-1.0#tag/Real-Time/operation/postRealTimeProposal">postRealTimeProposal</a>
     */
	public static final String PATH_REALTIME_PROPOSAL = "/rating-proposals/realtime";

	/**
     * Path to <a href="https://trolie.energy/spec-1.0#tag/Forecasting/operation/getLimitsForecastSnapshot">getLimitsForecastSnapshot</a>
     */
	public static final String PATH_FORECAST_SNAPSHOT = "/limits/forecast-snapshot";

	/**
     * Path to <a href="https://trolie.energy/spec-1.0#tag/Forecasting/operation/getRegionalLimitsForecastSnapshot">getRegionalForecastSnapshot</a>
     */
	public static final String PATH_REGIONAL_FORECAST_SNAPSHOT = "/limits/regional/forecast-snapshot";

	/**
     * Path to <a href="https://trolie.energy/spec-1.0#tag/Forecasting/operation/patchRatingForecastProposal">patchRatingForecastProposal</a>
     */
	public static final String PATH_FORECAST_PROPOSAL = "/rating-proposals/forecast";

	/**
     * Path to <a href="https://trolie.energy/spec-1.0#tag/Monitoring-Sets/operation/getMonitoringSet">getMonitoringSet</a>
     */
	public static final String PATH_MONITORING_SET_ID = "/monitoring-sets";

	/**
	 * Path to <a href="https://trolie.energy/spec-1.0#tag/Monitoring-Sets/operation/getDefaultMonitoringSet">getDefaultMonitoringSet</a>
	 */
	public static final String PATH_DEFAULT_MONITORING_SET = "/default-monitoring-set";

	/**
	 * Content type for real-time limit snapshots
	 */
	public static final String CONTENT_TYPE_REALTIME_SNAPSHOT = "application/vnd.trolie.realtime-limits-snapshot.v1+json";

	/**
	 * Content type for real-time proposal status.
	 */
	public static final String CONTENT_TYPE_REALTIME_PROPOSAL = "application/vnd.trolie.rating-realtime-proposal-status.v1+json";

	/**
	 * Content type for forecast limit snapshots
	 */
	public static final String CONTENT_TYPE_FORECAST_SNAPSHOT = "application/vnd.trolie.forecast-limits-snapshot.v1+json";

	/**
	 * Content type for forecast proposals
	 */
	public static final String CONTENT_TYPE_FORECAST_PROPOSAL = "application/vnd.trolie.rating-forecast-proposal.v1+json";

	/**
	 * Content type for monitoring sets.
	 */
	public static final String CONTENT_TYPE_MONITORING_SET = "application/vnd.trolie.monitoring-set.v1+json";

	/**
	 * Common query parameter name for monitoring set filter.
	 */
	public static final String PARAM_MONITORING_SET = "monitoring-set";

	/**
	 * Common query parameter name for a resource filter.
	 */
	public static final String PARAM_RESOURCE_ID = "resource-id";

	/**
	 * Common query parameter name for period offset.
	 */
	public static final String PARAM_OFFSET_PERIOD_START = "offset-period-start";

	/**
	 * Common query parameter name for the period end.
	 */
	public static final String PARAM_PERIOD_END = "period-end";
	
}
