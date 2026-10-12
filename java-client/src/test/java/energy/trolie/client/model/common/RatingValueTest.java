package energy.trolie.client.model.common;

import org.junit.jupiter.api.Test;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class RatingValueTest {


    private final JsonMapper mapper = new JsonMapper();

    @Test
    void read_null() throws JacksonException {
        assertNull(mapper.readValue("""
                null
                """, RatingValue.class));
    }

    @Test
    void read_mva() throws JacksonException {
        var value = mapper.readValue("""
                {
                   "mva":"1000"
                }
                """, RatingValue.class);

        assertNotNull(value);
        assertEquals(RatingUnits.MVA, value.getUnits());
        assertEquals(1000f, value.getMVA());
        assertNull(value.getAMPS());
        assertNull(value.getMWAndPF());
    }

    @Test
    void read_amps() throws JacksonException {
        var value = mapper.readValue("""
                {
                   "amps":"300"
                }
                """, RatingValue.class);

        assertNotNull(value);
        assertEquals(RatingUnits.AMPS, value.getUnits());
        assertEquals(300f, value.getAMPS());
    }

    @Test
    void read_mw_and_pf() throws JacksonException {
        var value = mapper.readValue("""
                {
                   "mw":"800",
                   "pf":"0.9"
                }
                """, RatingValue.class);

        assertNotNull(value);
        assertEquals(RatingUnits.MWandPF, value.getUnits());
        var rating = value.getMWAndPF();
        assertEquals(800f, rating.getLeft());
        assertEquals(0.9f, rating.getRight());
    }

    // Tests recirculation through JSON write, read, then equality.
    private void recirculate(RatingValue value) throws JacksonException {
        String json = mapper.writeValueAsString(value);
        var newValue = mapper.readValue(json, RatingValue.class);
        assertEquals(value, newValue);
    }

    @Test
    void write_mva() throws JacksonException {
        recirculate(RatingValue.fromMva(1100f));
    }

    @Test
    void write_amps() throws JacksonException {
        recirculate(RatingValue.fromAmps(900f));
    }

    @Test
    void write_mw_and_pf() throws JacksonException {
        recirculate(RatingValue.fromMwAndPf(900f, 0.9f));
    }

}
