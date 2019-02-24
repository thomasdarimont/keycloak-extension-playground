package com.github.thomasdarimont.keycloak.register;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;

public class MobileValidation {

    static boolean isPhoneNumberValid(String phoneNumber) {

        String formattedPhoneNumber = convertInternationalPrefix(phoneNumber);

        String region;
        if (isPossibleNationalNumber(formattedPhoneNumber)) {
            region = "DE";
        } else if (isInternationalNumber(formattedPhoneNumber)) {
            region = null;
        } else {
            return true; // If the number cannot be interpreted as an international or possible DE phone number, do not attempt to validate it.
        }

        try {
            Phonenumber.PhoneNumber parsedPhoneNumber = PhoneNumberUtil.getInstance().parse(formattedPhoneNumber, region);
            return PhoneNumberUtil.getInstance().isValidNumber(parsedPhoneNumber);
        } catch (NumberParseException e) {
            return false;
        }
    }

    static String convertInternationalPrefix(String phoneNumber) {
        String trimmedPhoneNumber = phoneNumber.trim();
        if (trimmedPhoneNumber.startsWith("00")) {
            return trimmedPhoneNumber.replaceFirst("00", "+");
        }
        return trimmedPhoneNumber;
    }

    static boolean isPossibleNationalNumber(String phoneNumber) {
        return phoneNumber.trim().startsWith("+49");
    }

    static boolean isInternationalNumber(String phoneNumber) {
        return phoneNumber.trim().startsWith("+");
    }
}
