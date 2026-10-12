package energy.trolie.client.impl.request.monitoringsets;

import energy.trolie.client.exception.StreamingGetConnectionException;
import energy.trolie.client.exception.StreamingGetHandlingException;
import energy.trolie.client.model.monitoringsets.MonitoringSet;
import energy.trolie.client.request.monitoringsets.MonitoringSetsReceiver;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.jackson.core.JsonParser;
import tools.jackson.core.exc.JacksonIOException;
import tools.jackson.databind.json.JsonMapper;

import java.io.InputStream;

/**
 * Implementation for parsing a monitoring set response shared between subscribed and on-demand requests
 */
@AllArgsConstructor
@Slf4j
public class MonitoringSetsResponseParser {

	private static final Logger logger = LoggerFactory.getLogger(MonitoringSetsResponseParser.class);
	
	MonitoringSetsReceiver receiver;

	public Boolean parseResponse(InputStream inputStream, JsonMapper jsonMapper) {

		try (JsonParser parser = jsonMapper.createParser(inputStream)) {
			MonitoringSet monitoringSet = parser.readValueAs(MonitoringSet.class);
			receiver.monitoringSet(monitoringSet);
			return true;
		} catch (JacksonIOException e) {
			logger.error("I/O error handling response",e);
			receiver.error(new StreamingGetConnectionException(e));
		} catch (Exception e) {
			logger.error("Error handling response data",e);
			receiver.error(new StreamingGetHandlingException(e));
		}

		return false;
	}
	
}
