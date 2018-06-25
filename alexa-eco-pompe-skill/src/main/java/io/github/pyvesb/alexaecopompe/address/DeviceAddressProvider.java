package io.github.pyvesb.alexaecopompe.address;

import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;
import static java.net.HttpURLConnection.HTTP_FORBIDDEN;
import static java.net.HttpURLConnection.HTTP_OK;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;

/**
 * Amazon seems to provide a similar class in the com.amazon.ask.model.services.deviceAddress package. Nevertheless, as
 * of yet the ask-sdk-model library is not documented anywhere and its source code is not released, which suggests that
 * it's not intended for public use at this point. This class provides a more efficient and easily maintainable
 * alternative.
 */
public class DeviceAddressProvider {

	private static final String PATH = "/v1/devices/%s/settings/address";
	private static final Logger LOGGER = LogManager.getLogger(DeviceAddressProvider.class);
	private static final ObjectReader READER = new ObjectMapper().configure(FAIL_ON_UNKNOWN_PROPERTIES, false)
			.readerFor(Address.class);

	private final int timeout;

	public DeviceAddressProvider(int timeout) {
		this.timeout = timeout;
	}

	public Address fetchAddress(String host, String device, String token)
			throws AddressForbiddenException, AddressInaccessibleException {
		try {
			LOGGER.info("Fetching address for host {}, device {} and token {}", host, device, token);
			URLConnection connection = new URL(host + String.format(PATH, device)).openConnection();
			connection.setRequestProperty("Authorization", "Bearer " + token);
			connection.setRequestProperty("Accept", "application/json");
			connection.setConnectTimeout(timeout);
			connection.setReadTimeout(timeout);
			int responseCode = ((HttpURLConnection) connection).getResponseCode();
			if (responseCode == HTTP_OK) {
				return unmarshallAddress(connection);
			} else if (responseCode == HTTP_FORBIDDEN) {
				throw new AddressForbiddenException();
			} else {
				throw new AddressInaccessibleException("Unexpected response code " + responseCode);
			}
		} catch (IOException e) {
			throw new AddressInaccessibleException(e);
		}
	}

	private Address unmarshallAddress(URLConnection connection) throws IOException {
		try (InputStream inputStream = connection.getInputStream()) {
			return READER.readValue(inputStream);
		}
	}

}
