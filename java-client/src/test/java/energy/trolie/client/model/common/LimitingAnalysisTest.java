package energy.trolie.client.model.common;

import energy.trolie.client.model.ratingproposals.RealTimeRating;
import energy.trolie.client.model.ratingproposals.ForecastRatingPeriod;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class LimitingAnalysisTest {

    private final JsonMapper jsonMapper = new JsonMapper();

    @Test
    void serializes_all_enum_values() throws IOException {
        assertEquals("\"THERMAL_RATING\"", jsonMapper.writeValueAsString(LimitingAnalysis.THERMAL_RATING));
        assertEquals("\"TRANSIENT_SECURITY_ASSESSMENT\"", jsonMapper.writeValueAsString(LimitingAnalysis.TRANSIENT_SECURITY_ASSESSMENT));
        assertEquals("\"VOLTAGE_SECURITY_ASSESSMENT\"", jsonMapper.writeValueAsString(LimitingAnalysis.VOLTAGE_SECURITY_ASSESSMENT));
        assertEquals("\"SMALL_SIGNAL_STABILITY_ASSESSMENT\"", jsonMapper.writeValueAsString(LimitingAnalysis.SMALL_SIGNAL_STABILITY_ASSESSMENT));
        assertEquals("\"SYSTEM_SPECIFIC\"", jsonMapper.writeValueAsString(LimitingAnalysis.SYSTEM_SPECIFIC));
        assertEquals("\"ENVIRONMENTAL_CONDITIONS\"", jsonMapper.writeValueAsString(LimitingAnalysis.ENVIRONMENTAL_CONDITIONS));
        assertEquals("\"REGULATORY_REQUIREMENTS\"", jsonMapper.writeValueAsString(LimitingAnalysis.REGULATORY_REQUIREMENTS));
        assertEquals("\"CONTRACT_CAPACITY\"", jsonMapper.writeValueAsString(LimitingAnalysis.CONTRACT_CAPACITY));
        assertEquals("\"OTHER\"", jsonMapper.writeValueAsString(LimitingAnalysis.OTHER));
    }

    @Test
    void deserializes_all_enum_values() throws IOException {
        assertEquals(LimitingAnalysis.THERMAL_RATING, jsonMapper.readValue("\"THERMAL_RATING\"", LimitingAnalysis.class));
        assertEquals(LimitingAnalysis.TRANSIENT_SECURITY_ASSESSMENT, jsonMapper.readValue("\"TRANSIENT_SECURITY_ASSESSMENT\"", LimitingAnalysis.class));
        assertEquals(LimitingAnalysis.VOLTAGE_SECURITY_ASSESSMENT, jsonMapper.readValue("\"VOLTAGE_SECURITY_ASSESSMENT\"", LimitingAnalysis.class));
        assertEquals(LimitingAnalysis.SMALL_SIGNAL_STABILITY_ASSESSMENT, jsonMapper.readValue("\"SMALL_SIGNAL_STABILITY_ASSESSMENT\"", LimitingAnalysis.class));
        assertEquals(LimitingAnalysis.SYSTEM_SPECIFIC, jsonMapper.readValue("\"SYSTEM_SPECIFIC\"", LimitingAnalysis.class));
        assertEquals(LimitingAnalysis.ENVIRONMENTAL_CONDITIONS, jsonMapper.readValue("\"ENVIRONMENTAL_CONDITIONS\"", LimitingAnalysis.class));
        assertEquals(LimitingAnalysis.REGULATORY_REQUIREMENTS, jsonMapper.readValue("\"REGULATORY_REQUIREMENTS\"", LimitingAnalysis.class));
        assertEquals(LimitingAnalysis.CONTRACT_CAPACITY, jsonMapper.readValue("\"CONTRACT_CAPACITY\"", LimitingAnalysis.class));
        assertEquals(LimitingAnalysis.OTHER, jsonMapper.readValue("\"OTHER\"", LimitingAnalysis.class));
    }

    @Test
    void fromDomain_converts_all_domain_types() {
        assertEquals(LimitingAnalysis.THERMAL_RATING, LimitingAnalysis.fromDomain(LimitingAnalysisType.THERMAL_RATING));
        assertEquals(LimitingAnalysis.TRANSIENT_SECURITY_ASSESSMENT, LimitingAnalysis.fromDomain(LimitingAnalysisType.TRANSIENT_SECURITY_ASSESSMENT));
        assertEquals(LimitingAnalysis.VOLTAGE_SECURITY_ASSESSMENT, LimitingAnalysis.fromDomain(LimitingAnalysisType.VOLTAGE_SECURITY_ASSESSMENT));
        assertEquals(LimitingAnalysis.SMALL_SIGNAL_STABILITY_ASSESSMENT, LimitingAnalysis.fromDomain(LimitingAnalysisType.SMALL_SIGNAL_STABILITY_ASSESSMENT));
        assertEquals(LimitingAnalysis.SYSTEM_SPECIFIC, LimitingAnalysis.fromDomain(LimitingAnalysisType.SYSTEM_SPECIFIC));
        assertEquals(LimitingAnalysis.ENVIRONMENTAL_CONDITIONS, LimitingAnalysis.fromDomain(LimitingAnalysisType.ENVIRONMENTAL_CONDITIONS));
        assertEquals(LimitingAnalysis.REGULATORY_REQUIREMENTS, LimitingAnalysis.fromDomain(LimitingAnalysisType.REGULATORY_REQUIREMENTS));
        assertEquals(LimitingAnalysis.CONTRACT_CAPACITY, LimitingAnalysis.fromDomain(LimitingAnalysisType.CONTRACT_CAPACITY));
        assertEquals(LimitingAnalysis.OTHER, LimitingAnalysis.fromDomain(LimitingAnalysisType.OTHER));
    }

    @Test
    void fromDomain_returns_null_when_input_is_null() {
        assertNull(LimitingAnalysis.fromDomain(null));
    }

    @Test
    void toDomain_converts_all_enum_values() {
        assertEquals(LimitingAnalysisType.THERMAL_RATING, LimitingAnalysis.THERMAL_RATING.toDomain());
        assertEquals(LimitingAnalysisType.TRANSIENT_SECURITY_ASSESSMENT, LimitingAnalysis.TRANSIENT_SECURITY_ASSESSMENT.toDomain());
        assertEquals(LimitingAnalysisType.VOLTAGE_SECURITY_ASSESSMENT, LimitingAnalysis.VOLTAGE_SECURITY_ASSESSMENT.toDomain());
        assertEquals(LimitingAnalysisType.SMALL_SIGNAL_STABILITY_ASSESSMENT, LimitingAnalysis.SMALL_SIGNAL_STABILITY_ASSESSMENT.toDomain());
        assertEquals(LimitingAnalysisType.SYSTEM_SPECIFIC, LimitingAnalysis.SYSTEM_SPECIFIC.toDomain());
        assertEquals(LimitingAnalysisType.ENVIRONMENTAL_CONDITIONS, LimitingAnalysis.ENVIRONMENTAL_CONDITIONS.toDomain());
        assertEquals(LimitingAnalysisType.REGULATORY_REQUIREMENTS, LimitingAnalysis.REGULATORY_REQUIREMENTS.toDomain());
        assertEquals(LimitingAnalysisType.CONTRACT_CAPACITY, LimitingAnalysis.CONTRACT_CAPACITY.toDomain());
        assertEquals(LimitingAnalysisType.OTHER, LimitingAnalysis.OTHER.toDomain());
    }

    @Test
    void roundtrip_conversion_dto_to_domain_and_back() {
        for (LimitingAnalysis dtoValue : LimitingAnalysis.values()) {
            LimitingAnalysisType domainValue = dtoValue.toDomain();
            LimitingAnalysis roundTrippedDtoValue = LimitingAnalysis.fromDomain(domainValue);
            assertEquals(dtoValue, roundTrippedDtoValue);
        }
    }

    @Test
    void roundtrip_conversion_domain_to_dto_and_back() {
        for (LimitingAnalysisType domainValue : LimitingAnalysisType.values()) {
            LimitingAnalysis dtoValue = LimitingAnalysis.fromDomain(domainValue);
            LimitingAnalysisType roundTrippedDomainValue = dtoValue.toDomain();
            assertEquals(domainValue, roundTrippedDomainValue);
        }
    }

    @Test
    void field_is_optional_on_real_time_rating() throws IOException {
        RealTimeRating withoutLimitingAnalysis = RealTimeRating.builder()
                .resourceId("resource1")
                .continuousOperatingLimit(RatingValue.fromMva(100f))
                .build();

        String json = jsonMapper.writeValueAsString(withoutLimitingAnalysis);
        RealTimeRating roundTripped = jsonMapper.readValue(json, RealTimeRating.class);

        assertNull(roundTripped.getLimitingAnalysis());
    }

    @Test
    void field_round_trips_on_real_time_rating() throws IOException {
        RealTimeRating withLimitingAnalysis = RealTimeRating.builder()
                .resourceId("resource1")
                .continuousOperatingLimit(RatingValue.fromMva(100f))
                .limitingAnalysis(LimitingAnalysis.THERMAL_RATING)
                .build();

        String json = jsonMapper.writeValueAsString(withLimitingAnalysis);
        RealTimeRating roundTripped = jsonMapper.readValue(json, RealTimeRating.class);

        assertEquals(LimitingAnalysis.THERMAL_RATING, roundTripped.getLimitingAnalysis());
    }

    @Test
    void field_is_optional_on_forecast_rating_period() throws IOException {
        ForecastRatingPeriod withoutLimitingAnalysis = ForecastRatingPeriod.builder()
                .periodStart(Instant.parse("2024-01-01T00:00:00Z"))
                .periodEnd(Instant.parse("2024-01-01T01:00:00Z"))
                .continuousOperatingLimit(RatingValue.fromMva(100f))
                .build();

        String json = jsonMapper.writeValueAsString(withoutLimitingAnalysis);
        ForecastRatingPeriod roundTripped = jsonMapper.readValue(json, ForecastRatingPeriod.class);

        assertNull(roundTripped.getLimitingAnalysis());
    }

    @Test
    void field_round_trips_on_forecast_rating_period() throws IOException {
        ForecastRatingPeriod withLimitingAnalysis = ForecastRatingPeriod.builder()
                .periodStart(Instant.parse("2024-01-01T00:00:00Z"))
                .periodEnd(Instant.parse("2024-01-01T01:00:00Z"))
                .continuousOperatingLimit(RatingValue.fromMva(100f))
                .limitingAnalysis(LimitingAnalysis.VOLTAGE_SECURITY_ASSESSMENT)
                .build();

        String json = jsonMapper.writeValueAsString(withLimitingAnalysis);
        ForecastRatingPeriod roundTripped = jsonMapper.readValue(json, ForecastRatingPeriod.class);

        assertEquals(LimitingAnalysis.VOLTAGE_SECURITY_ASSESSMENT, roundTripped.getLimitingAnalysis());
    }
}
