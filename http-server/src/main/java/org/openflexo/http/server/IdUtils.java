package org.openflexo.http.server;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Base64.Decoder;
import java.util.Base64.Encoder;

/**
 * Created by charlie on 07/02/2017.
 */
public class IdUtils {

	private final static Encoder encoder = Base64.getUrlEncoder().withoutPadding();
	private final static Decoder urlDecoder = Base64.getUrlDecoder();

	public static String encoreUri(String uri) {
		return encoder.encodeToString(uri.getBytes(StandardCharsets.ISO_8859_1));
	}

	public static String decodeId(String encoded) {
		return new String(urlDecoder.decode(encoded), StandardCharsets.ISO_8859_1);
	}

}
