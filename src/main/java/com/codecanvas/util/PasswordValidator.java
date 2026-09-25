package com.codecanvas.util;

import java.util.ArrayList;
import java.util.List;

public class PasswordValidator {

    public static class ValidationResult {
        private final boolean minLength;
        private final boolean hasUpper;
        private final boolean hasLower;
        private final boolean hasDigit;
        private final boolean hasSpecial;
        private final List<String> missingRequirements;

        public ValidationResult(boolean minLength, boolean hasUpper, boolean hasLower, 
                                boolean hasDigit, boolean hasSpecial, List<String> missingRequirements) {
            this.minLength = minLength;
            this.hasUpper = hasUpper;
            this.hasLower = hasLower;
            this.hasDigit = hasDigit;
            this.hasSpecial = hasSpecial;
            this.missingRequirements = missingRequirements;
        }

        public boolean isValid() {
            return minLength && hasUpper && hasLower && hasDigit && hasSpecial;
        }

        public boolean isMinLength() { return minLength; }
        public boolean isHasUpper() { return hasUpper; }
        public boolean isHasLower() { return hasLower; }
        public boolean isHasDigit() { return hasDigit; }
        public boolean isHasSpecial() { return hasSpecial; }

        public List<String> getMissingRequirements() {
            return missingRequirements;
        }

        public String getSummaryErrorMessage() {
            if (isValid()) return "";
            return "Password requirements not met:\n • " + String.join("\n • ", missingRequirements);
        }
    }

    public static ValidationResult validate(String password) {
        List<String> missing = new ArrayList<>();
        if (password == null) {
            password = "";
        }

        boolean minLength = password.length() >= 8;
        if (!minLength) {
            missing.add("Must be at least 8 characters long");
        }

        boolean hasUpper = false;
        boolean hasLower = false;
        boolean hasDigit = false;
        boolean hasSpecial = false;

        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) hasUpper = true;
            else if (Character.isLowerCase(c)) hasLower = true;
            else if (Character.isDigit(c)) hasDigit = true;
            else if (!Character.isWhitespace(c)) hasSpecial = true;
        }

        if (!hasUpper) missing.add("Must contain at least one uppercase letter (A-Z)");
        if (!hasLower) missing.add("Must contain at least one lowercase letter (a-z)");
        if (!hasDigit) missing.add("Must contain at least one numeric digit (0-9)");
        if (!hasSpecial) missing.add("Must contain at least one special character (!@#$%^&*...)");

        return new ValidationResult(minLength, hasUpper, hasLower, hasDigit, hasSpecial, missing);
    }
}
