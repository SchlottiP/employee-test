package de.proeller.applications.employeetest;

import org.apache.commons.lang3.RandomStringUtils;

public class TestUtil {
    public static String createRandomEmailAddress(){
        return RandomStringUtils.randomAlphabetic(5)+ "@" + RandomStringUtils.randomAlphabetic(6) + ".com";
    }
}
