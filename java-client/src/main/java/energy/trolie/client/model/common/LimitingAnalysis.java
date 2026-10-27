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


    /**
     * Convert from domain LimitingAnalysisType to DTO LimitingAnalysis.
     *
     * @param domainType the domain enum value
     * @return the corresponding DTO enum value, or null if input is null
     */
    public static LimitingAnalysis fromDomain(LimitingAnalysisType domainType) {
        if (domainType == null) {
            return null;
        }

        return switch (domainType) {
            case THERMAL_RATING -> THERMAL_RATING;
            case TRANSIENT_SECURITY_ASSESSMENT -> TRANSIENT_SECURITY_ASSESSMENT;
            case VOLTAGE_SECURITY_ASSESSMENT -> VOLTAGE_SECURITY_ASSESSMENT;
            case SMALL_SIGNAL_STABILITY_ASSESSMENT -> SMALL_SIGNAL_STABILITY_ASSESSMENT;
            case SYSTEM_SPECIFIC -> SYSTEM_SPECIFIC;
            case ENVIRONMENTAL_CONDITIONS -> ENVIRONMENTAL_CONDITIONS;
            case REGULATORY_REQUIREMENTS -> REGULATORY_REQUIREMENTS;
            case CONTRACT_CAPACITY -> CONTRACT_CAPACITY;
            case OTHER -> OTHER;
        };
    }

    /**
     * Convert to domain LimitingAnalysisType from DTO LimitingAnalysis.
     *
     * @return the corresponding domain enum value, or null if this is null
     */
    public LimitingAnalysisType toDomain() {
        return switch (this) {
            case THERMAL_RATING -> LimitingAnalysisType.THERMAL_RATING;
            case TRANSIENT_SECURITY_ASSESSMENT -> LimitingAnalysisType.TRANSIENT_SECURITY_ASSESSMENT;
            case VOLTAGE_SECURITY_ASSESSMENT -> LimitingAnalysisType.VOLTAGE_SECURITY_ASSESSMENT;
            case SMALL_SIGNAL_STABILITY_ASSESSMENT -> LimitingAnalysisType.SMALL_SIGNAL_STABILITY_ASSESSMENT;
            case SYSTEM_SPECIFIC -> LimitingAnalysisType.SYSTEM_SPECIFIC;
            case ENVIRONMENTAL_CONDITIONS -> LimitingAnalysisType.ENVIRONMENTAL_CONDITIONS;
            case REGULATORY_REQUIREMENTS -> LimitingAnalysisType.REGULATORY_REQUIREMENTS;
            case CONTRACT_CAPACITY -> LimitingAnalysisType.CONTRACT_CAPACITY;
            case OTHER -> LimitingAnalysisType.OTHER;
        };
    }
}
