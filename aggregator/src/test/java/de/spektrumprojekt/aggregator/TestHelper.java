package de.spektrumprojekt.aggregator;

public class TestHelper {

    public static String FILE_NAME_VALID_XML = "validXML.txt";

    public static String FILE_NAME_INVALID_XML = "invalidXML.txt";

    public static String getTestFilePath(String filename) {
        String basePath = System.getProperty("user.dir");
        String folderPath = basePath + "/src/test/resources";
        return folderPath + "/" + filename;
    }

}
