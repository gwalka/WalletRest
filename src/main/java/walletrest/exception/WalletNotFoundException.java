package walletrest.exception;

public class WalletNotFoundException extends RuntimeException {
    public WalletNotFoundException(String message, Object... args) {
        super(String.format(message, args));
    }
}
