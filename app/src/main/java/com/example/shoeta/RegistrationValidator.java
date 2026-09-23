package com.example.shoeta;
public class RegistrationValidator {

    public static boolean isWorkerRegistrationValid(String name, String phone, String pass) {
        if (name == null || phone == null || pass == null) return false;
        if (name.trim().isEmpty() || phone.trim().isEmpty() || pass.trim().isEmpty()) {
            return false;
        }
        return isPasswordLongEnough(pass);
    }
    public static boolean isFactoryRegistrationValid(String company, String phone, String pass) {
        if (company == null || phone == null || pass == null) return false;
        if (company.trim().isEmpty() || phone.trim().isEmpty() || pass.trim().isEmpty()) {
            return false;
        }
        return isPasswordLongEnough(pass);
    }
    public static boolean isLoginInputValid(String phoneOrUser, String password) {
        if (phoneOrUser == null || password == null) return false;
        return !phoneOrUser.trim().isEmpty() && !password.trim().isEmpty();
    }
    public static boolean isPasswordLongEnough(String pass) {
        return pass != null && pass.trim().length() >= 6;
    }
    public static boolean isTransactionIdValid(String txnId) {
        if (txnId == null || txnId.trim().isEmpty()) return false;
        return txnId.trim().length() >= 6;
    }
}