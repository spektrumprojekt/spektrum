package de.spektrumprojekt.aggregator;

import org.junit.Ignore;

@Ignore
public class TestHelper {

    public static String FILE_NAME_VALID_XML = "validXML.txt";

    public static String FILE_NAME_INVALID_XML = "invalidXML.txt";

    public static String FILE_NAME_DC_ONLY = "feed_dc_only.xml";

    public static String FILE_NAME_NO_DC = "feed_no_dc.xml";

    public static String getTestFilePath(String filename) {
        String basePath = System.getProperty("user.dir");
        String folderPath = basePath + "/src/test/resources";
        return folderPath + "/" + filename;
    }

}
