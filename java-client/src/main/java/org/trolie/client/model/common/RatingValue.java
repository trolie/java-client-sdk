package org.trolie.client.model.common;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.apache.commons.lang3.tuple.Pair;
import org.trolie.client.impl.model.common.RatingValueDeserializer;
import org.trolie.client.impl.model.common.RatingValueSerializer;

import java.util.Collections;
import java.util.Map;

/**
 * <p>Representation of a rating value, given the various units
 * that are possible under the TROLIE specification.</p>
 * <p>In most cases, users may be able to assume the units used
 * for ratings in a particular exchange.  For example,
 * ISO New England, SPP and MISO all normalize ratings to MVA.
 * However, if this is not the case, the rating units are introspectable.
 * Clients may wish to write defensive code as appropriate to the actual units
 * of a given exchange.  </p>
 */
@JsonSerialize(using = RatingValueSerializer.class)
@JsonDeserialize(using = RatingValueDeserializer.class)
@ToString
@EqualsAndHashCode
@Getter
public class RatingValue {

    /**
     * Gets the raw value of the rating as would be marshalled to JSON.
     */
    private final Map<String,Float> values;

    private RatingValue(Map<String,Float> values) {
        this.values = values;
    }

    // Constructors.

    /**
     * Construct from raw Json
     * @param values map representing a JSON map as defined in the TROLIE specification
     * @return new instance
     */
    public static RatingValue ofMappedJson(Map<String,Float> values) {
        return new RatingValue(values);
    }

    /**
     * Construct from an MVA value
     * @param mva rating value in megavolt amperes
     * @return new instance
     */
    public static RatingValue fromMva(float mva) {
        return new RatingValue(Collections.singletonMap(
                RatingUnits.MVA.getJsonValue(), mva)
        );
    }

    /**
     * Construct from an amps value
     * @param amps rating value in amperes
     * @return new instance
     */
    public static RatingValue fromAmps(float amps) {
        return new RatingValue(Collections.singletonMap(
                RatingUnits.AMPS.getJsonValue(), amps)
        );
    }

    /**
     * Construct from a megawatt and power factor ratio combination
     * @param mw rating value in megawatts
     * @param pf assumed power factor ratio from 0 to 1
     * @return new instance
     */
    public static RatingValue fromMwAndPf(float mw, float pf) {
        return new RatingValue(Map.of(
                RatingUnits.MW.getJsonValue(), mw,
                RatingUnits.PF.getJsonValue(), pf
        ));
    }

    /**
     * Determines the units of this rating value
     * @return the units used to represent this particular rating value.
     * Will return null if the units are unknown to the SDK.
     */
    public RatingUnits getUnits() {
        if(values.containsKey(RatingUnits.MVA.getJsonValue())) {
            return RatingUnits.MVA;
        } else if (values.containsKey(RatingUnits.AMPS.getJsonValue())) {
            return RatingUnits.AMPS;
        } else if (values.containsKey(RatingUnits.PF.getJsonValue()) &&
                   values.containsKey(RatingUnits.MW.getJsonValue())) {
            return RatingUnits.MWandPF;
        }
        return null;
    }

    /**
     * Gets the rating in MVA
     * @return returns the rating in MVA if that is the units.
     * Otherwise, returns null.
     */
    public Float getMVA() {
        return values.get(RatingUnits.MVA.getJsonValue());
    }

    /**
     * Gets the rating in amps
     * @return returns the rating in amps if that is the units.
     * Otherwise, returns null.
     */
    public Float getAMPS() {
        return values.get(RatingUnits.AMPS.getJsonValue());
    }

    /**
     * Gets the rating in a MW and PF pair
     * @return returns the rating in MW and PF pair if that is the units.
     * Otherwise, returns null.
     */
    public Pair<Float,Float> getMWAndPF() {
        var mw = values.get(RatingUnits.MW.getJsonValue());
        var pf = values.get(RatingUnits.PF.getJsonValue());
        if(mw == null || pf == null) {
            return null;
        } else {
            return Pair.of(mw, pf);
        }
    }

}
