package pl.chrapatij.backend.exception;

public class userExceptionError400 extends RuntimeException {
    public userExceptionError400(String message) {
        super(message);
    }

    public userExceptionError400(String message, Throwable cause) {
        super(message, cause);
    }

    public userExceptionError400(Throwable cause) {
        super(cause);
    }

    public userExceptionError400(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}