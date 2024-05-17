package pl.chrapatij.backend.exception;

public class userExceptionError401 extends RuntimeException {
    public userExceptionError401(String message) {
        super(message);
    }

    public userExceptionError401(String message, Throwable cause) {
        super(message, cause);
    }

    public userExceptionError401(Throwable cause) {
        super(cause);
    }

    public userExceptionError401(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}