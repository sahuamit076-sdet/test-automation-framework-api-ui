package in.zeta.qa.utils.exceptions;

public class TachyonTestException extends RuntimeException {
    public TachyonTestException(String errorMessage) {
        super(errorMessage);
    }
}