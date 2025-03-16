package energy.trolie.client.impl.model.ratingproposals;

import energy.trolie.client.model.common.InputValue;
import energy.trolie.client.model.ratingproposals.ForecastRatingPeriod;
import energy.trolie.client.request.ratingproposals.ForecastRatingProposalUpdate;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ForecastPeriodBuilderImplTest {

    @Mock
    private ForecastRatingProposalUpdate update;

    @Captor
    private ArgumentCaptor<ForecastRatingPeriod> periodCaptor;

    private ForecastPeriodBuilderImpl builder;
    private final Instant windowStart = Instant.parse("2025-02-01T00:00:00Z");
    private static final String BAND1 = "EMERG";
    private static final String BAND2 = "LDSHD";


    @BeforeEach
    public void setUp() {
        builder = new ForecastPeriodBuilderImpl(windowStart, 60, update);
    }


    private void setMVAVals() {
        builder.setContinuousMVA(700f);
        builder.setEmergencyMVA(BAND1,750f);
        builder.setEmergencyMVA(BAND2,780f);
    }


    @Test
    void complete_typical_success() {
        builder.setPeriodStart(windowStart);
        builder.setPeriodEnd(windowStart.plus(1, ChronoUnit.HOURS));
        setMVAVals();
        builder.complete();

        verify(update).period(periodCaptor.capture());

        var period = periodCaptor.getValue();
        assertNotNull(period);
        assertEquals(windowStart, period.getPeriodStart());
        assertEquals(windowStart.plus(1, ChronoUnit.HOURS), period.getPeriodEnd());
        Assertions.assertEquals(700f, period.getContinuousOperatingLimit()
                .getMVA());
        var emergRatings = period.getEmergencyOperatingLimits();
        assertNotNull(emergRatings);
        assertEquals(2, emergRatings.size());
        Assertions.assertEquals(750f, emergRatings.get(0).getLimit().getMVA());
        Assertions.assertEquals(BAND1, emergRatings.get(0).getDurationName());
        Assertions.assertEquals(780f, emergRatings.get(1).getLimit().getMVA());
        Assertions.assertEquals(BAND2, emergRatings.get(1).getDurationName());
    }

    @Test
    void complete_with_inputs() {
        builder.setPeriodStart(windowStart);
        builder.setPeriodEnd(windowStart.plus(1, ChronoUnit.HOURS));
        setMVAVals();
        // Add some inputs.
        builder.addUsedInput(InputValue.builder()
                        .name("temperature")
                        .value("75")
                        .unit("degreesFahrenheit")
                .build());
        builder.addUsedInput(InputValue.builder()
                .name("windspeed")
                .value("20")
                .unit("mph")
                .build());

        builder.complete();

        verify(update).period(periodCaptor.capture());

        var period = periodCaptor.getValue();
        assertEquals(2, period.getInputsUsed().size());
    }

    @Test
    void complete_numbered_period() {
        builder.setPeriod(1);
        setMVAVals();
        builder.complete();

        verify(update).period(periodCaptor.capture());

        var period = periodCaptor.getValue();
        assertEquals(windowStart.plus(1, ChronoUnit.HOURS), period.getPeriodStart());
        assertEquals(windowStart.plus(2, ChronoUnit.HOURS), period.getPeriodEnd());
    }

    @Test
    void complete_computed_end() {
        builder.setPeriodStart(windowStart);
        setMVAVals();
        builder.complete();

        verify(update).period(periodCaptor.capture());

        var period = periodCaptor.getValue();
        assertNotNull(period);
        assertEquals(windowStart, period.getPeriodStart());
        assertEquals(windowStart.plus(1, ChronoUnit.HOURS), period.getPeriodEnd());
    }

    @Test
    void repeated_continuous_rating() {
        setMVAVals();
        assertThrows(IllegalArgumentException.class, () -> builder.setContinuousMVA(33f));
    }

    @Test
    void repeated_emergency_rating() {
        setMVAVals();
        assertThrows(IllegalArgumentException.class, () -> builder.setEmergencyMVA(BAND1, 33f));
    }

    @Test
    void test_missing_period() {
        setMVAVals();
        assertThrows(IllegalStateException.class, () -> builder.complete());
    }


    @Test
    void amps_units() {
        builder.setPeriod(0);

        builder.setContinuousAmps(500f);
        builder.setEmergencyAmps(BAND1,550f);
        builder.setEmergencyAmps(BAND2,580f);
        builder.complete();

        verify(update).period(periodCaptor.capture());

        var period = periodCaptor.getValue();
        assertNotNull(period);
        Assertions.assertEquals(500f, period.getContinuousOperatingLimit()
                .getAMPS());
        var emergRatings = period.getEmergencyOperatingLimits();
        assertNotNull(emergRatings);
        assertEquals(2, emergRatings.size());
        Assertions.assertEquals(550f, emergRatings.get(0).getLimit().getAMPS());
        Assertions.assertEquals(580f, emergRatings.get(1).getLimit().getAMPS());
    }

    @Test
    void mwpf_units() {
        builder.setPeriod(0);

        builder.setContinuousMWandPF(500f, 0.9f);
        builder.setEmergencyMWandPF(BAND1,550f, 0.8f);
        builder.setEmergencyMWandPF(BAND2,580f, 0.8f);
        builder.complete();

        verify(update).period(periodCaptor.capture());

        var period = periodCaptor.getValue();
        assertNotNull(period);
        var continuous = period.getContinuousOperatingLimit().getMWAndPF();
        Assertions.assertEquals(500f, continuous.getLeft());
        Assertions.assertEquals(0.9f, continuous.getRight());
        var emergRatings = period.getEmergencyOperatingLimits();
        assertNotNull(emergRatings);
        assertEquals(2, emergRatings.size());
        var emer1 = emergRatings.get(0).getLimit().getMWAndPF();
        Assertions.assertEquals(550f, emer1.getLeft());
        Assertions.assertEquals(0.8f, emer1.getRight());
        var emer2 = emergRatings.get(1).getLimit().getMWAndPF();
        Assertions.assertEquals(580f, emer2.getLeft());
        Assertions.assertEquals(0.8f, emer2.getRight());
    }

}