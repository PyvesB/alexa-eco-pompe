package utils;

import org.opentest4j.AssertionFailedError;

public class MissingResponse extends AssertionFailedError {

	private static final long serialVersionUID = 1L;

	public MissingResponse() {
		super("expected non empty optional");
	}

}
