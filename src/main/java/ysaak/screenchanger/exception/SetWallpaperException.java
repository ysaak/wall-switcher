package ysaak.screenchanger.exception;

public class SetWallpaperException extends Exception {
	public enum ErrorCode {
		CREATE_SCRIPT, EXECUTE_SCRIPT
	}

	private static final long serialVersionUID = 6310780019056576374L;

	private final ErrorCode error;

	public SetWallpaperException(final ErrorCode error, final String message) {
		super(message);
		this.error = error;
	}

	public SetWallpaperException(final ErrorCode error, final String message, final Throwable cause) {
		super(message, cause);
		this.error = error;
	}

	public ErrorCode getError() {
		return error;
	}
}
