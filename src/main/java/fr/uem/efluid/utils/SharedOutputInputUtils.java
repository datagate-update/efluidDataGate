package fr.uem.efluid.utils;

import static fr.uem.efluid.utils.ErrorType.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import fr.uem.efluid.utils.json.LocalDateModule;
import fr.uem.efluid.utils.json.LocalDateTimeModule;

/**
 * <p>
 * Helper with a very basic data model for processing input and output of <tt>Shared</tt>
 * as basic String values
 * </p>
 * <p>
 * Input/output process can be seen as similare to a serialization process
 * </p>
 * 
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
public class SharedOutputInputUtils {

	private static final String SYSTEM_DEFAULT_TMP_DIR = System.getProperty("java.io.tmpdir");

	static final ObjectMapper MAPPER = preparedObjectMapper();

	static final DateTimeFormatter DATE_TIME_FORMATER = DateTimeFormatter.ofPattern(WebUtils.DATE_TIME_FORMAT);

	/**
	 * @param properties
	 * @return
	 */
	public static JsonPropertiesWriter newJson() {
		return new JsonPropertiesWriter();
	}

	/**
	 * @param raw
	 * @return
	 */
	public static OutputJsonPropertiesReader fromJson(String raw) {
		return new OutputJsonPropertiesReader(raw);
	}

	/**
	 * @param extension
	 * @return
	 * @throws IOException
	 */
	public static Path initTmpFile(String id, String extension, boolean create) {
		try {
			if (create) {
				return File.createTempFile(id, extension).toPath();
			}
			return new File(SYSTEM_DEFAULT_TMP_DIR + "/id" + UUID.randomUUID().toString() + extension).toPath();
		} catch (IOException e) {
			throw new ApplicationException(TMP_ERROR, "Cannot create tmp file with extension " + extension, e);
		}
	}

	/**
	 * @param zipped
	 * @return
	 */
	public static Path initTmpFolder() {

		File unzipFolder = new File(SYSTEM_DEFAULT_TMP_DIR + "/" + UUID.randomUUID().toString() + "-work");
		if (!unzipFolder.exists()) {
			unzipFolder.mkdirs();
		}

		return unzipFolder.toPath();
	}

	/**
	 * @return prepared Jackson mapper for JSON production
	 */
	private static ObjectMapper preparedObjectMapper() {
		ObjectMapper objectMapper = new ObjectMapper();

		// TODO : modules not used in direct value deserialization ?

		// LocalDate As formated String (ex : 2015-03-19)
		objectMapper.registerModule(new LocalDateModule());

		// LocalDateTime As formated String (ex : 2015-03-19 08:56)
		objectMapper.registerModule(new LocalDateTimeModule());

		// Exclude empty values
		objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);

		// Allows empty
		objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

		return objectMapper;
	}

	/**
	 * Chained writter
	 * 
	 * @author elecomte
	 * @since v0.0.1
	 * @version 1
	 */
	public static class JsonPropertiesWriter {

		private final Map<String, Object> properties = new HashMap<>();

		/**
		 * @return the properties
		 */
		Map<String, Object> getProperties() {
			return this.properties;
		}

		/**
		 * Dedicated for UUID
		 * 
		 * @param key
		 * @param uuid
		 * @return
		 */
		public JsonPropertiesWriter with(String key, UUID uuid) {
			if (uuid != null) {
				this.properties.put(key, uuid.toString());
			}
			return this;
		}

		/**
		 * Dedicated for LocalDateTime
		 * 
		 * @param key
		 * @param uuid
		 * @return
		 */
		public JsonPropertiesWriter with(String key, LocalDateTime ldt) {
			if (ldt != null) {
				this.properties.put(key, ldt.format(DATE_TIME_FORMATER));
			}
			return this;
		}

		/**
		 * @param key
		 * @param value
		 * @return
		 */
		public JsonPropertiesWriter with(String key, Object value) {
			if (value != null) {
				this.properties.put(key, value.toString());
			}
			return this;
		}

		@Override
		public String toString() {
			try {
				return MAPPER.writeValueAsString(this.properties);
			} catch (JsonProcessingException e) {
				throw new ApplicationException(JSON_WRITE_ERROR, "Cannot serialize to json", e);
			}
		}
	}

	/**
	 * Chained reader (use consumer mode)
	 * 
	 * @author elecomte
	 * @since v0.0.1
	 * @version 1
	 */
	public static class OutputJsonPropertiesReader {

		private final Map<String, Object> jsonProperties;

		@SuppressWarnings("unchecked")
		OutputJsonPropertiesReader(String raw) {
			try {
				this.jsonProperties = MAPPER.readValue(raw, Map.class);
			} catch (IOException e) {
				throw new ApplicationException(JSON_READ_ERROR, "Cannot deserialize from json", e);
			}
		}

		/**
		 * @param name
		 * @return
		 */
		public String getPropertyString(String name) {
			Object jsonProperty = this.jsonProperties.get(name);
			if (jsonProperty == null) {
				return null;
			}
			return jsonProperty.toString();
		}

		/**
		 * @param name
		 * @return
		 */
		public UUID getPropertyUUID(String name) {
			Object jsonProperty = this.jsonProperties.get(name);
			if (jsonProperty == null) {
				return null;
			}
			return UUID.fromString(jsonProperty.toString());
		}

		/**
		 * @param name
		 * @return
		 */
		public LocalDateTime getPropertyLocalDateTime(String name) {
			Object jsonProperty = this.jsonProperties.get(name);
			if (jsonProperty == null) {
				return null;
			}
			return LocalDateTime.parse(jsonProperty.toString().trim(), DATE_TIME_FORMATER);
		}

		/**
		 * @param name
		 * @param apply
		 * @return
		 */
		public OutputJsonPropertiesReader applyString(String name, Consumer<String> apply) {
			String prop = getPropertyString(name);
			if (prop != null) {
				apply.accept(prop);
			}
			return this;
		}

		/**
		 * @param name
		 * @param apply
		 * @return
		 */
		public OutputJsonPropertiesReader applyUUID(String name, Consumer<UUID> apply) {
			UUID prop = getPropertyUUID(name);
			if (prop != null) {
				apply.accept(prop);
			}
			return this;
		}

		/**
		 * @param name
		 * @param apply
		 * @return
		 */
		public OutputJsonPropertiesReader applyLdt(String name, Consumer<LocalDateTime> apply) {
			LocalDateTime prop = getPropertyLocalDateTime(name);
			if (prop != null) {
				apply.accept(prop);
			}
			return this;
		}

	}
}
