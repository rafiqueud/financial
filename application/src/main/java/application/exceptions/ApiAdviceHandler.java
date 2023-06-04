package application.exceptions;

import application.dto.ErrorDTO;
import domain.exception.AccountNotFoundException;
import domain.exception.InsufficientBalanceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;

import static application.exceptions.ErrorMessages.ACCOUNT_NOT_FOUND;
import static application.exceptions.ErrorMessages.INSUFFICIENT_BALANCE;

@RestControllerAdvice
public class ApiAdviceHandler {

    @ExceptionHandler(AccountNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<ErrorDTO> handleAccountNotFound(final AccountNotFoundException accountNotFoundException) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND.value())
                .body(new ErrorDTO(
                        HttpStatus.NOT_FOUND.value(),
                        String.format(ACCOUNT_NOT_FOUND, accountNotFoundException.getAccountId())
                ));
    }

    @ExceptionHandler(InsufficientBalanceException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ErrorDTO> handleInsufficientBalanceException(final InsufficientBalanceException insufficientBalanceException) {
        return ResponseEntity.badRequest()
                .body(new ErrorDTO(
                        HttpStatus.BAD_REQUEST.value(),
                        INSUFFICIENT_BALANCE
                ));
    }

    @ExceptionHandler(WebExchangeBindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ErrorDTO> handleWebBindGeneralErrors(final WebExchangeBindException webExchangeBindException) {
        final var message = webExchangeBindException.getFieldErrors().stream()
                .map(fieldError -> "\"" + fieldError.getField() + "\" : " + fieldError.getDefaultMessage())
                .reduce("", (a, b) -> a + b);


        return ResponseEntity.badRequest()
                .body(new ErrorDTO(
                        HttpStatus.BAD_REQUEST.value(),
                        message)
                );
    }

    @ExceptionHandler(Throwable.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<ErrorDTO> handleGeneralErrors(final Throwable throwable) {
        throwable.printStackTrace();
        return ResponseEntity.badRequest()
                .body(new ErrorDTO(
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        "Unexpected Error")
                );
    }
}
