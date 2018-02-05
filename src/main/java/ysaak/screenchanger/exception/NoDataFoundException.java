package ysaak.screenchanger.exception;

public class NoDataFoundException extends Exception {
	private static final long serialVersionUID = 8164832969279356389L;

	public NoDataFoundException(String arg0, Throwable arg1, boolean arg2, boolean arg3) {
		super(arg0, arg1, arg2, arg3);
	}

	public NoDataFoundException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

	public NoDataFoundException(String arg0) {
		super(arg0);
	}

	public NoDataFoundException(Throwable arg0) {
		super(arg0);
	}
}
