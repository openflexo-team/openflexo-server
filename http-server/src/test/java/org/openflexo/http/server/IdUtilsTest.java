package org.openflexo.http.server;

import org.junit.Assert;
import org.junit.Test;
import org.openflexo.http.server.util.IdUtils;

/**
 * Tests for IdUtils
 */
public class IdUtilsTest {

	@Test
	public void testId1() {
		checkUriEncoding("http://openflexo.org//VirtualModel/ViewPoint1.viewpoint/ViewPoint1.xml");
		checkUriEncoding("http://openflexo.org//VirtualModel/lmlkmlkmlkmlk");
		checkUriEncoding("http://openflexo.org//VirtualModel/&é'(§");
		checkUriEncoding("http://openflexo.org//VirtualModel/%%ùù$$");
	}

	private static void checkUriEncoding(String uri) {
		String encoded = IdUtils.encodeuri(uri);
		Assert.assertNotNull(encoded);
		Assert.assertEquals(uri, IdUtils.decodeId(encoded));
	}

	@Test
	public void testSanitized() {
		checkSanitized("FML", "fml");
		checkSanitized("FML@RT", "fmlrt");
		checkSanitized("Hello World for test !?!", "helloworldfortest!!");
	}

	private static void checkSanitized(String id, String expected) {
		String sanitized = IdUtils.sanitiseId(id);
		Assert.assertNotNull(sanitized);
		Assert.assertEquals(sanitized, expected);
	}
}
