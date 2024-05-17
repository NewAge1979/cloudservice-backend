package pl.chrapatij.backend.exception;

public class userExceptionError500 extends RuntimeException {
    public userExceptionError500(String message) {
        super(message);
    }

    public userExceptionError500(String message, Throwable cause) {
        super(message, cause);
    }

    public userExceptionError500(Throwable cause) {
        super(cause);
    }

    public userExceptionError500(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}