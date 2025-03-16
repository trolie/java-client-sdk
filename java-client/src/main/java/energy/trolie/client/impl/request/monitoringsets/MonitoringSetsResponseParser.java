package energy.trolie.client.impl.request.monitoringsets;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import energy.trolie.client.exception.StreamingGetConnectionException;
import energy.trolie.client.exception.StreamingGetHandlingException;
import energy.trolie.client.model.monitoringsets.MonitoringSet;
import energy.trolie.client.request.monitoringsets.MonitoringSetsReceiver;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;

/**
 * Implementation for parsing a monitoring set response shared between subscribed and on-demand requests
 */
@AllArgsConstructor
@Slf4j
public class MonitoringSetsResponseParser {

	private static final Logger logger = LoggerFactory.getLogger(MonitoringSetsResponseParser.class);
	
	MonitoringSetsReceiver receiver;

	public Boolean parseResponse(InputStream inputStream, JsonFactory jsonFactory) {

		try (JsonParser parser = jsonFactory.createParser(inputStream)) {
			MonitoringSet monitoringSet = parser.readValueAs(MonitoringSet.class);
			receiver.monitoringSet(monitoringSet);
			return true;
		} catch (IOException e) {
			logger.error("I/O error handling response",e);
			receiver.error(new StreamingGetConnectionException(e));
		} catch (Exception e) {
			logger.error("Error handling response data",e);
			receiver.error(new StreamingGetHandlingException(e));
		}

		return false;
	}
	
}
