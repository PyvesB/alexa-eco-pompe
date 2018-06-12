package io.github.pyvesb.alexaecopompe.utils;

import org.apache.commons.lang3.StringUtils;

public class PostCodesExtractor {

	private static final int HASH_OFFSET = 4;
	private static final char SEPARATOR = ',';

	public static String[] from(String townId) {
		// townId is a short hash followed by post codes, for instance: acc,20600,20200
		return StringUtils.split(townId.substring(HASH_OFFSET), SEPARATOR);
	}

	private PostCodesExtractor() {
		// Not called.
	}

}
