package walletrest.exception;

public class KafkaHandleException extends RuntimeException {
    public KafkaHandleException(String message, Object... args) {

        super(String.format(message, args));
    }
}
