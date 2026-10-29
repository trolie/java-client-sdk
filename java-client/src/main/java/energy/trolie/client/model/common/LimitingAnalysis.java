package energy.trolie.client.model.common;
/**
 * Indicates how a operating limit was determined. This is used to provide context for the ratings proposals
 * and limits snapshots. When defined in the header, this is assumed to apply to any limit that does not specify its
 * own limiting analysis. When defined for a period, it applies to the continuous-operating-limit as well as the
 * associated emergency-operating-limits, unless the emergency-operating-limits specify their own limiting analysis.
 */
public enum LimitingAnalysis {

    /**
     * The limit was determined by the thermal rating of underlying equipment.
     */
    THERMAL_RATING,

    /**
     * Studies of the system's transient stability determined the limit.
     */
    TRANSIENT_SECURITY_ASSESSMENT,

    /**
     * Studies of the system's voltage stability determined the limit.
     */
    VOLTAGE_SECURITY_ASSESSMENT,

    /**
     * Small-signal stability studies determined the limit.
     */
    SMALL_SIGNAL_STABILITY_ASSESSMENT,

    /**
     * This could account for limits imposed by specific system characteristics, such as the presence of sensitive loads,
     * the need to maintain specific power flows, or other reliability/volatility reasons.
     * These include limits defined by Special Protection Schemes and Remedial Action Schemes.
     */
    SYSTEM_SPECIFIC,

    /**
     * The limit was determined by exceptional environmental conditions such as wildfire risk, ice loading,
     * excessive wind/galloping, or other factors.
     */
    ENVIRONMENTAL_CONDITIONS,

    /**
     * The limit is imposed by regulatory requirements.
     */
    REGULATORY_REQUIREMENTS,

    /**
     * The available facility transfer capacity is limited by contractual obligations.
     */
    CONTRACT_CAPACITY,

    /**
     * The limit was determined by some other means not anticipated in this standard.
     */
    OTHER;

}
