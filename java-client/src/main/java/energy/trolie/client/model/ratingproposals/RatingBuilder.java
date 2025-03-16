package energy.trolie.client.model.ratingproposals;

import energy.trolie.client.StreamingUpdate;
import energy.trolie.client.model.common.InputValue;

/**
 * <p>A rating value builder that respects business rules and conventions
 * configured with the client.  Emits rating DTOs that may be passed
 * to send proposals.</p>
 * <p>These are always used in concert with a
 * {@link StreamingUpdate}.  They are created
 * by the update instance,
 * and the call to {@link #complete()} commits them back to the stream</p>
 */
public interface RatingBuilder {


    /**
     * Sets the continuous rating in MVA
     * @param mva value for max apparent power
     */
    void setContinuousMVA(float mva);

    /**
     * Sets the continuous rating in Amps
     * @param amps value for max current
     */
    void setContinuousAmps(float amps);

    /**
     * Sets the continuous rating in a MW and Power Factor combination
     * @param mw value for max active power
     * @param pf value for assumed power factor
     */
    void setContinuousMWandPF(float mw, float pf);

    /**
     * Sets the rating value in MVA for a particular emergency duration.
     * @param durationName duration name to use.  This method assumes a duration name
     *                     is predefined in the proposal header.
     * @param mva value for max apparent power
     */
    void setEmergencyMVA(String durationName, float mva);

    /**
     * Sets the rating value in Amps for a particular emergency duration.
     * @param durationName duration name to use.  This method assumes a duration name
     *                     is predefined in the proposal header.
     * @param amps value for max current
     */
    void setEmergencyAmps(String durationName, float amps);

    /**
     * Sets the rating value using a MW value and assumed power factor for
     * a given emergency duration.
     * @param durationName duration name to use.  This method assumes a duration name
     *                     is predefined in the proposal header.
     * @param mw value for max active power
     * @param pf value for assumed power factor
     */
    void setEmergencyMWandPF(String durationName, float mw, float pf);

    /**
     * Adds an input value used to compute this rating.
     * Most TROLIE server implementations don't require this, but it
     * may be stored in some cases for transparency purposes.
     * @param input filled in input value
     */
    void addUsedInput(InputValue input);

    /**
     * Completes adding the rating to the proposal update.
     */
    void complete();

}
