package org.openmrs.module.drools;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import org.openmrs.module.drools.utils.CommonUtils;

public class TestApp {

    public static void main(String[] args) throws IOException, URISyntaxException {
        CommonUtils.convertExcelRulesToDrl(getFilePath("rules/bp_rules.xlsx"),
                getFilePath("rules/age_rules.drl"));
    }

    public static String getFilePath(String fileName) throws URISyntaxException {
        URL resource = TestApp.class.getClassLoader().getResource(fileName);
        if (resource != null) {
            return resource.getPath();
        } else {
            throw new IllegalArgumentException("File not found: " + fileName);
        }
    }

}
