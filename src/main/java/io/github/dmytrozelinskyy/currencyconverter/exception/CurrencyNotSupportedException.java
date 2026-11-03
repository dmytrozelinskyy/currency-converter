package io.github.dmytrozelinskyy.currencyconverter.exception;

public class CurrencyNotSupportedException extends RuntimeException {
   public CurrencyNotSupportedException(String message) {
      super(message);
   }
}
