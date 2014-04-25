package de.spektrumprojekt.i.evaluation.runner.aggregator.compare.gen;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.comparator.LastModifiedFileComparator;

/**
 * Cache that keeps Files of a Directory sorted by the last modification date (youngest first).
 * 
 * @author Torsten
 * 
 */
public class FileCache {

    private final Map<String, List<File>> fileCache = new HashMap<String, List<File>>();

    public List<File> getFiles(File dir) {
        return this.getFiles(dir.toString());
    }

    public List<File> getFiles(String dir) {
        List<File> filesList = fileCache.get(dir);
        if (filesList == null) {

            filesList = loadFiles(dir);

        }
        return filesList;
    }

    private synchronized List<File> loadFiles(String dir) {
        List<File> filesList = fileCache.get(dir);
        if (filesList != null) {
            return filesList;
        }

        File dirFile = new File(dir);
        File[] files = dirFile.listFiles();
        Arrays.sort(files, LastModifiedFileComparator.LASTMODIFIED_REVERSE);

        filesList = Arrays.asList(files);
        fileCache.put(dir, filesList);
        return filesList;
    }

}