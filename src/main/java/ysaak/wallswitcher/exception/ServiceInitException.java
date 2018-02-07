package ysaak.wallswitcher.exception;

public class ServiceInitException extends Exception {
    public ServiceInitException(String message) {
        super(message);
    }

    public ServiceInitException(String message, Throwable cause) {
        super(message, cause);
    }
}
