package ee.taltech.inbankbackend.exceptions;

public class AgeRestrictionException extends RuntimeException {
    public AgeRestrictionException(String message) {
        super(message);
    }
}
