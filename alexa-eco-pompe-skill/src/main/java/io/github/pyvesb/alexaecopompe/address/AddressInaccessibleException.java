package io.github.pyvesb.alexaecopompe.address;

public class AddressInaccessibleException extends Exception {

	private static final long serialVersionUID = 1L;
	
	public AddressInaccessibleException(String message) {
		super(message);
	}
	
	public AddressInaccessibleException(Throwable cause) {
		super(cause);
	}

}
