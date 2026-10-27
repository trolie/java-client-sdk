package energy.trolie.client.model.common;

import lombok.Getter;

/**
 * Enum representing limiting analysis types for scheduled interface limits.
 * This allows ratings providers to indicate what analysis set the limit.
 */
@Getter
public enum LimitingAnalysisType {


    THERMAL_RATING("THERMAL_RATING", "Thermal Rating", 1),
    TRANSIENT_SECURITY_ASSESSMENT("TRANSIENT_SECURITY_ASSESSMENT", "Transient Security Assessment", 2),
    VOLTAGE_SECURITY_ASSESSMENT("VOLTAGE_SECURITY_ASSESSMENT", "Voltage Security Assessment", 3),
    SMALL_SIGNAL_STABILITY_ASSESSMENT("SMALL_SIGNAL_STABILITY_ASSESSMENT", "Small Signal Stability Assessment", 4),
    SYSTEM_SPECIFIC("SYSTEM_SPECIFIC", "System Specific", 5),
    ENVIRONMENTAL_CONDITIONS("ENVIRONMENTAL_CONDITIONS", "Environmental Conditions", 6),
    REGULATORY_REQUIREMENTS("REGULATORY_REQUIREMENTS", "Regulatory Requirements", 7),
    CONTRACT_CAPACITY("CONTRACT_CAPACITY", "Contract Capacity", 8),
    OTHER("OTHER", "Other", 9);

    private final String code;
    private final String displayName;
    private final Integer sortOrder;

    LimitingAnalysisType(String code, String displayName, Integer sortOrder) {
        this.code = code;
        this.displayName = displayName;
        this.sortOrder = sortOrder;
    }

}
