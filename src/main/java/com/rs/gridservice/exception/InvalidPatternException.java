package com.rs.gridservice.exception;

/**
 * DataType.pattern alanina gecerli bir regex OLMAYAN bir deger girildiginde
 * firlatilir (bkz. JpaDataTypeServiceImpl.validatePattern).
 */
public class InvalidPatternException extends RuntimeException {
    public InvalidPatternException(String message) {
        super(message);
    }
}
