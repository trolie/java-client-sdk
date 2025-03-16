package energy.trolie.client.model.common;

import lombok.Getter;

/**
 * Enumeration of common units supported in TROLIE
 */
@Getter
public enum RatingUnits {

    /**
     * Apparent power in megavolt-amperes
     */
    MVA("mva"),

    /**
     * Current in amperes
     */
    AMPS("amps"),

    /**
     * Active power in megawatts.  In AARs, used in combination with the
     * power factor
     */
    MW("mw"),

    /**
     * Ratio of active to apparent power.  In AARs, used in combination with
     * MW.
     */
    PF("pf"),

    /**
     * Represents the combination of active power and an assumed power factor.
     * Isn't directly represented in JSON, but rather is represented with the
     * presence of both MW and PF simultaneously.
     */
    MWandPF(null);

    /**
     * -- GETTER --
     *  the value to map these units to in JSON.
     *
     */
    private final String jsonValue;

    RatingUnits(String jsonValue) {
        this.jsonValue = jsonValue;
    }

}
