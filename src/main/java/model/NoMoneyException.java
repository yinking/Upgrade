package model;

public class NoMoneyException extends IllegalArgumentException {

    public NoMoneyException(Integer account, Double amount) {

        super(String.format("Account %d withdraw %.2f, unable to process!", account, amount));
    }
}
