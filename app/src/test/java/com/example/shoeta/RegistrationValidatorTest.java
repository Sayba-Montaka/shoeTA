package com.example.shoeta;

import org.junit.Test;
import static org.junit.Assert.*;

public class RegistrationValidatorTest {

    // ---- Worker registration ----

    @Test
    public void workerRegistration_validInput_returnsTrue() {
        assertTrue(RegistrationValidator.isWorkerRegistrationValid("Karim", "01712345678", "secret1"));
    }

    @Test
    public void workerRegistration_missingFields_returnsFalse() {
        assertFalse(RegistrationValidator.isWorkerRegistrationValid("", "01712345678", "secret1"));
        assertFalse(RegistrationValidator.isWorkerRegistrationValid("Karim", "", "secret1"));
        assertFalse(RegistrationValidator.isWorkerRegistrationValid("Karim", "01712345678", ""));
    }

    @Test
    public void workerRegistration_nullFields_returnsFalse() {
        assertFalse(RegistrationValidator.isWorkerRegistrationValid(null, "01712345678", "secret1"));
    }

    // ---- Password rule (shared by worker + factory) ----

    @Test
    public void password_shorterThanSixChars_returnsFalse() {
        assertFalse(RegistrationValidator.isPasswordLongEnough("abc"));
    }

    @Test
    public void password_exactlySixChars_returnsTrue() {
        assertTrue(RegistrationValidator.isPasswordLongEnough("abcdef")); // boundary case
    }

    @Test
    public void password_null_returnsFalse() {
        assertFalse(RegistrationValidator.isPasswordLongEnough(null));
    }

    // ---- Factory registration ----

    @Test
    public void factoryRegistration_validInput_returnsTrue() {
        assertTrue(RegistrationValidator.isFactoryRegistrationValid("ShoeCo Ltd", "01812345678", "pass123"));
    }

    @Test
    public void factoryRegistration_shortPassword_returnsFalse() {
        assertFalse(RegistrationValidator.isFactoryRegistrationValid("ShoeCo Ltd", "01812345678", "abc"));
    }

    // ---- Login ----

    @Test
    public void login_validInput_returnsTrue() {
        assertTrue(RegistrationValidator.isLoginInputValid("01712345678", "secret1"));
    }

    @Test
    public void login_emptyPassword_returnsFalse() {
        assertFalse(RegistrationValidator.isLoginInputValid("01712345678", "  "));
    }

    // ---- Transaction ID (PostJobActivity) ----

    @Test
    public void transactionId_validLength_returnsTrue() {
        assertTrue(RegistrationValidator.isTransactionIdValid("TXN12345"));
    }

    @Test
    public void transactionId_tooShort_returnsFalse() {
        assertFalse(RegistrationValidator.isTransactionIdValid("TX1"));
    }

    @Test
    public void transactionId_emptyOrNull_returnsFalse() {
        assertFalse(RegistrationValidator.isTransactionIdValid(""));
        assertFalse(RegistrationValidator.isTransactionIdValid(null));
    }
}