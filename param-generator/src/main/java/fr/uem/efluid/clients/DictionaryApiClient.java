package fr.uem.efluid.clients;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import fr.uem.efluid.rest.RestApi;
import fr.uem.efluid.rest.v1.DictionaryApi;
import fr.uem.efluid.rest.v1.model.CreatedDictionaryView;
import fr.uem.efluid.utils.ApplicationException;
import fr.uem.efluid.utils.ErrorType;

/**
 * <p>
 * Basic client using RestTemplate
 * </p>
 * 
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
public class DictionaryApiClient implements DictionaryApi {

	private final String uri;
	private final String token;
	private final RestTemplate template;

	/**
	 * 
	 */
	public DictionaryApiClient(String uri, String token) {
		this.uri = uri;
		this.token = token;
		this.template = new RestTemplate();
		RestApi.configureMessageConverters(this.template);
	}

	/**
	 * @param files
	 * @return
	 * @throws ApplicationException
	 * @see fr.uem.efluid.rest.v1.DictionaryApi#uploadDictionaryPackage(java.util.List)
	 */
	@Override
	public CreatedDictionaryView uploadDictionaryPackage(MultipartFile file) throws ApplicationException {

		try {
			MultiValueMap<String, Object> data = new LinkedMultiValueMap<>();
			ByteArrayResource resource = new ByteArrayResource(file.getBytes()) {
				@Override
				public String getFilename() {
					return file.getName();
				}
			};
			data.add("file", resource);

			HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(data, RestApi.FIXED_UPLOAD_HEADERS);
			return this.template.postForObject(this.uri + "/upload?token=" + this.token, request, CreatedDictionaryView.class);

		} catch (Exception e) {
			throw new ApplicationException(ErrorType.WRONG_CLIENT_CALL, "Cannot process call to /updoad", e);
		}
	}

}