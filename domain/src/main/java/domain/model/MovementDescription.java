package domain.model;

public abstract class MovementDescription {
    public static final String DEPOSIT_DESCRIPTION = "Deposit in account";
    public static final String WITHDRAW_DESCRIPTION = "Withdraw on account";

    public static final String DEBIT_TRANSFER_DESCRIPTION = "Transfer to account %s";

    public static final String CREDIT_TRANSFER_DESCRIPTION = "Transfer from account %s";
}
