package io.github.dmytrozelinskyy.currencyconverter.exception;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler
{
   private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

   @Override
   protected ResponseEntity<Object> handleHandlerMethodValidationException(
           HandlerMethodValidationException ex,
           HttpHeaders headers,
           HttpStatusCode status,
           WebRequest request) {
      Map<String, String> errors = new LinkedHashMap<>();
      ex.getParameterValidationResults().forEach(result -> {
         String parameter = result.getMethodParameter().getParameterName();
         result.getResolvableErrors().forEach(error ->
                 errors.putIfAbsent(parameter, error.getDefaultMessage()));
      });

      ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "One or more request parameters are invalid.");
      problem.setTitle("Invalid request");
      problem.setProperty("errors", errors);

      return handleExceptionInternal(ex, problem, headers, HttpStatus.BAD_REQUEST, request);
   }

   @ExceptionHandler(CurrencyNotSupportedException.class)
   public ProblemDetail handleCurrencyNotSupported(CurrencyNotSupportedException ex) {
      ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
      problem.setTitle("Currency not supported.");
      return problem;
   }

   @ExceptionHandler(ExchangeRateUnavailableException.class)
   public ProblemDetail handleRateUnavailable(ExchangeRateUnavailableException ex) {
      log.warn("Exchange rate provider problem: {}", ex.getMessage(), ex.getCause());
      ProblemDetail problem = ProblemDetail.forStatusAndDetail(
              HttpStatus.SERVICE_UNAVAILABLE,
              "Exchange rates are temporarily unavailable. Please try again in a moment.");
      problem.setTitle("Exchange rate service unavailable.");
      return problem;
   }

   @ExceptionHandler(Exception.class)
   public ProblemDetail handleException(Exception ex) {
      log.error("Unexpected error", ex);
      ProblemDetail problem = ProblemDetail.forStatusAndDetail(
              HttpStatus.INTERNAL_SERVER_ERROR, "Something went wrong on our side.");
      problem.setTitle("Internal server error");
      return problem;
   }
}
