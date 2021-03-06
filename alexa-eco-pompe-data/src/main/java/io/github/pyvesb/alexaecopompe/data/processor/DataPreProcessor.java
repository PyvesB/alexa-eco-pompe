package io.github.pyvesb.alexaecopompe.data.processor;

import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.ZipInputStream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import io.github.pyvesb.alexaecopompe.data.unmarshal.GasStationMixIn;
import io.github.pyvesb.alexaecopompe.data.unmarshal.PriceMixIn;
import io.github.pyvesb.alexaecopompe.domain.GasStation;
import io.github.pyvesb.alexaecopompe.domain.Price;
import io.protostuff.LinkedBuffer;
import io.protostuff.ProtostuffIOUtil;
import io.protostuff.Schema;
import io.protostuff.runtime.RuntimeSchema;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ObjectCannedACL;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

public class DataPreProcessor implements RequestHandler<Void, Void> {

	private static final String BUCKET_NAME = "alexa-eco-pompe";
	private static final String KEY = "gas-station.data";

	private static final Logger LOGGER = LogManager.getLogger(DataPreProcessor.class);
	private static final Schema<GasStation> SCHEMA = RuntimeSchema.getSchema(GasStation.class);
	private static final LinkedBuffer BUFFER = LinkedBuffer.allocate();
	private static final ObjectReader READER;
	static {
		JavaType gasStationList = TypeFactory.defaultInstance().constructCollectionLikeType(List.class, GasStation.class);
		READER = new XmlMapper()
				.addMixIn(Price.class, PriceMixIn.class)
				.addMixIn(GasStation.class, GasStationMixIn.class)
				.configure(FAIL_ON_UNKNOWN_PROPERTIES, false).readerFor(gasStationList);
	}

	private final S3Client s3Client;
	private final String dataLocation;

	public DataPreProcessor() {
		this(S3Client.create(), System.getenv("DATA_URL"));
	}

	DataPreProcessor(S3Client s3Client, String dataLocation) {
		this.s3Client = s3Client;
		this.dataLocation = dataLocation;
	}

	@Override
	public Void handleRequest(Void nothing, Context context) {
		try {
			URL dataURL = new URL(dataLocation);
			preProcessData(dataURL);
		} catch (IOException e) {
			// All useful information will be logged to CloudWatch.
			throw new RuntimeException(e);
		}
		return null;
	}

	private void preProcessData(URL dataURL) throws IOException {
		LOGGER.info("Retrieving gas station data");
		try (InputStream dataStream = dataURL.openStream(); ZipInputStream zipInputStream = new ZipInputStream(dataStream)) {
			zipInputStream.getNextEntry();
			List<GasStation> gasStations = parse(zipInputStream);
			removeNoPrices(gasStations);
			sortByLatitude(gasStations);
			ByteArrayOutputStream byteArrayOutputStream = serialise(gasStations);
			uploadToS3(byteArrayOutputStream);
		}
	}

	private List<GasStation> parse(InputStream inputStream) throws IOException {
		LOGGER.info("Parsing gas station data");
		return READER.readValue(inputStream);
	}

	private void removeNoPrices(List<GasStation> gasStations) {
		int initialSize = gasStations.size();
		gasStations.removeIf(gs -> gs.getPrices() == null);
		LOGGER.info("Removed {} gas stations which don't have any prices specified", (initialSize - gasStations.size()));
	}

	private void sortByLatitude(List<GasStation> gasStations) {
		LOGGER.info("Sorting gas stations by latitude");
		Collections.sort(gasStations, (gs1, gs2) -> Float.compare(gs1.getLat(), gs2.getLat()));
	}

	private ByteArrayOutputStream serialise(List<GasStation> gasStations) throws IOException {
		LOGGER.info("Serialising {} gas stations", gasStations.size());
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		try (DeflaterOutputStream deflaterOutputStream = new DeflaterOutputStream(byteArrayOutputStream)) {
			ProtostuffIOUtil.writeListTo(deflaterOutputStream, gasStations, SCHEMA, BUFFER);
		} finally {
			BUFFER.clear();
		}
		return byteArrayOutputStream;
	}

	private void uploadToS3(ByteArrayOutputStream byteArrayOutputStream) {
		LOGGER.info("Uploading {} bytes of serialised data to S3", byteArrayOutputStream.size());
		InputStream inputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
		PutObjectRequest putObjectRequest = PutObjectRequest.builder()
				.acl(ObjectCannedACL.PUBLIC_READ)
				.bucket(BUCKET_NAME)
				.key(KEY)
				.build();
		RequestBody requestBody = RequestBody.fromInputStream(inputStream, byteArrayOutputStream.size());
		s3Client.putObject(putObjectRequest, requestBody);
	}

}
