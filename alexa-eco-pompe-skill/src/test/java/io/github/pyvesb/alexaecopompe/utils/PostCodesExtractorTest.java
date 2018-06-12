package io.github.pyvesb.alexaecopompe.utils;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class PostCodesExtractorTest {

	@Test
	void shouldExtractPostCodes() {
		String[] postCodes = PostCodesExtractor.from("ae1,75001,75002,06130");

		assertArrayEquals(new String[] { "75001", "75002", "06130" }, postCodes);
	}

}
