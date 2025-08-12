package walletrest.exception;

public class WrongOperationException extends RuntimeException {
    public WrongOperationException(String message, Object... args) {

        super(String.format(message, args));
    }
}
