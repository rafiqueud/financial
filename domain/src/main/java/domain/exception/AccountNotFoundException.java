package domain.exception;

public class AccountNotFoundException extends RuntimeException {
    private final String accountId;

    public AccountNotFoundException(String accountId) {
        super();
        this.accountId = accountId;
    }

    public String getAccountId() {
        return accountId;
    }
}
