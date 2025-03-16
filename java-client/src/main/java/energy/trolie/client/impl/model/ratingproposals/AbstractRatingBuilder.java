package energy.trolie.client.impl.model.ratingproposals;

import energy.trolie.client.model.common.InputValue;
import energy.trolie.client.model.ratingproposals.RatingBuilder;
import energy.trolie.client.model.common.EmergencyRatingValue;
import energy.trolie.client.model.common.RatingValue;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractRatingBuilder implements RatingBuilder {

    protected RatingValue continuousOperatingLimit = null;

    // Note that a linked hashmap is used to retain consistent order.
    protected Map<String, EmergencyRatingValue> emergencyOperatingLimits =
            new LinkedHashMap<>();

    protected List<InputValue> inputsUsed = null;


    private void validateContinuousRating() {
        if(continuousOperatingLimit != null) {
            throw new IllegalArgumentException("Continuous rating already set to " +
                    continuousOperatingLimit);
        }
    }

    @Override
    public void setContinuousMVA(float mva) {
        validateContinuousRating();
        continuousOperatingLimit = RatingValue.fromMva(mva);
    }

    @Override
    public void setContinuousAmps(float amps) {
        validateContinuousRating();
        continuousOperatingLimit = RatingValue.fromAmps(amps);
    }

    @Override
    public void setContinuousMWandPF(float mw, float pf) {
        validateContinuousRating();
        continuousOperatingLimit = RatingValue.fromMwAndPf(mw, pf);

    }

    /**
     *
     * @return emergency limit values in order.
     */
    protected final List<EmergencyRatingValue> getEmergencyRatings() {
        return new ArrayList<>(emergencyOperatingLimits.values());
    }

    private void validateEmergencyLimit(String duration) {
        if(emergencyOperatingLimits.containsKey(duration)) {
            throw new IllegalArgumentException(duration +
                    " already set to " + emergencyOperatingLimits.get(duration));
        }
    }

    @Override
    public void setEmergencyMVA(String durationName, float mva) {
        validateEmergencyLimit(durationName);
        emergencyOperatingLimits.put(durationName,
                EmergencyRatingValue.of(durationName,
                        RatingValue.fromMva(mva)));
    }

    @Override
    public void setEmergencyAmps(String durationName, float amps) {
        validateEmergencyLimit(durationName);
        emergencyOperatingLimits.put(durationName,
                EmergencyRatingValue.of(durationName,
                        RatingValue.fromAmps(amps)));
    }

    @Override
    public void setEmergencyMWandPF(String durationName, float mw, float pf) {
        validateEmergencyLimit(durationName);
        emergencyOperatingLimits.put(durationName,
                EmergencyRatingValue.of(durationName,
                        RatingValue.fromMwAndPf(mw, pf)));
    }

    @Override
    public void addUsedInput(InputValue input) {
        if(inputsUsed == null) {
            inputsUsed = new ArrayList<>();
        }
        inputsUsed.add(input);
    }
}
