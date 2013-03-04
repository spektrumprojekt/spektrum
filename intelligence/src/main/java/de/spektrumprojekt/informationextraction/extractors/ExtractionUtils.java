/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package de.spektrumprojekt.informationextraction.extractors;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import opennlp.tools.tokenize.SimpleTokenizer;

import org.apache.commons.lang.Validate;
import org.apache.log4j.Logger;
import org.tartarus.snowball.SnowballProgram;
import org.tartarus.snowball.ext.EnglishStemmer;
import org.tartarus.snowball.ext.FrenchStemmer;
import org.tartarus.snowball.ext.GermanStemmer;

/**
 * <p>
 * Utility class for text mining tasks like stemming, stopwords, n-gram creation, ...
 * </p>
 * 
 * @author Philipp Katz
 */
public final class ExtractionUtils {

    // XXX introduce language enum.

    /** The logger for this class. */
    private static final Logger LOGGER = Logger.getLogger(ExtractionUtils.class);

    /**
     * Pattern to match urls
     */
    public static final Pattern URL_PATTERN = Pattern
            .compile("(?i)\\b((?:[a-z][\\w-]+:(?:/{1,3}|[a-z0-9%])|www\\d{0,3}[.]|[a-z0-9.\\-]+[.][a-z]{2,4}/)(?:[^\\s()<>]+|\\(([^\\s()<>]+|(\\([^\\s()<>]+\\)))*\\))+(?:\\(([^\\s()<>]+|(\\([^\\s()<>]+\\)))*\\)|[^\\s`!()\\[\\]{};:'\".,<>?Â«Â»â€œâ€�â€˜â€™]))");

    /** Stopwords for English langugage. */
    private static final Set<String> STOPWORDS_EN;
    /** Stopwords for German language. */
    private static final Set<String> STOPWORDS_DE;

    static {
        try {
            STOPWORDS_DE = new HashSet<String>(readLinesFromResource("/stopwords_de.txt"));
            STOPWORDS_EN = new HashSet<String>(readLinesFromResource("/stopwords_en.txt"));
        } catch (IOException e) {
            throw new IllegalStateException("Could not load the stop word lists.");
        }
    }

    /**
     * <p>
     * Create word n-grams of the given length.
     * </p>
     * 
     * @param tokens
     *            the tokens to use
     * @param length
     *            length of ngrams to consider
     * @return the list of created ngrams
     */
    public static List<String> createNGrams(List<String> tokens, int length) {
        Validate.notNull(tokens, "tokens must not be null");
        Validate.isTrue(length > 1, "length must be greater than 1");

        List<String> nGrams = new ArrayList<String>();
        for (int i = 0; i <= tokens.size() - length; i++) {
            StringBuilder nGram = new StringBuilder();
            for (int j = i; j < i + length; j++) {
                if (j > i) {
                    nGram.append(" ");
                }
                nGram.append(tokens.get(j));
            }
            nGrams.add(nGram.toString());
        }
        return nGrams;
    }

    /**
     * <p>
     * Create character-based n-grams of the given length.
     * </p>
     * 
     * @param text The text for which to create the n-grams, not <code>null</code>.
     * @param length The length of the n-grams to create, greater or equal one.
     * @return A list with n-grams in the order as extracted from the text.
     */
    public static List<String> createCharNGrams(String text, int length) {
        Validate.isTrue(length >= 1, "length must be greater or equal 1");
        Validate.notNull(text, "text must not be null");

        List<String> ret = new ArrayList<String>();
        for (int i = 0; i <= text.length() - length; i++) {
            ret.add(text.substring(i, i + length));
        }

        return ret;
    }

    /**
     * <p>
     * Create character-based n-grams in the given length interval.
     * </p>
     * 
     * @param text The text for which to create n-grams, not <code>null</code>.
     * @param minLength The minimum length of the n-grams to create, greater or equal one.
     * @param maxLength The maximum length of the n-grams to create, greater or equal minLength.
     * @return A list with n-grams starting with the min length in the order as extracted from the text.
     */
    public static List<String> createCharNGrams(String text, int minLength, int maxLength) {
        Validate.isTrue(minLength >= 1, "minLength must be greater or equal 1");
        Validate.isTrue(maxLength >= minLength, "maxLength must be greater or equal minLength");
        Validate.notNull(text, "text must not be null");

        List<String> ret = new ArrayList<String>();
        for (int length = minLength; length <= maxLength; length++) {
            ret.addAll(createCharNGrams(text, length));
        }
        return ret;
    }

    /**
     * 
     * @param language
     *            the language
     * @return the snowball stemmer
     */
    private static SnowballProgram getStemmer(String language) {
        SnowballProgram stemmer;
        if (language.equals("de")) {
            stemmer = new GermanStemmer();
        } else if (language.equals("en")) {
            stemmer = new EnglishStemmer();
        } else if (language.equals("fr")) {
            stemmer = new FrenchStemmer();
        } else {
            LOGGER.warn("no stemmer for language \"" + language + "\"");
            stemmer = null;
        }
        return stemmer;
    }

    /**
     * TODO move to some generic helper class
     * 
     * @param resourceFileName
     *            resource name
     * @return the lines read
     * @throws IOException
     *             in case of an error
     */
    private static final List<String> readLinesFromResource(String resourceFileName)
            throws IOException {
        InputStream inputStream = ExtractionUtils.class.getResourceAsStream(resourceFileName);
        if (inputStream == null) {
            throw new FileNotFoundException("Resource file with name \"" + resourceFileName
                    + "\" could not be read.");
        }
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
            String line;
            List<String> ret = new ArrayList<String>();
            while ((line = reader.readLine()) != null) {
                ret.add(line);
            }
            return ret;
        } finally {
            try {
                inputStream.close();
            } catch (IOException ignore) {
            }
        }
    }

    /**
     * Stem
     * 
     * @param stemmer
     *            the stemmer to use
     * @param text
     *            the text to stem
     * @return the stemmed text
     */
    private static String stem(SnowballProgram stemmer, String text) {
        String[] textParts = text.split("\\s");
        StringBuilder stemmedResult = new StringBuilder(text.length());
        for (int i = 0; i < textParts.length; i++) {
            if (i > 0) {
                stemmedResult.append(' ');
            }
            stemmer.setCurrent(textParts[i]);
            stemmer.stem();
            stemmedResult.append(stemmer.getCurrent());
        }
        return stemmedResult.toString();
    }

    /**
     * Stemm with the language
     * 
     * @param language
     *            the langauge
     * @param tokens
     *            the tokens
     * @return the stemmed tokens
     */
    public static List<String> stem(String language, List<String> tokens) {
        Validate.notEmpty(language, "language must not be empty");
        Validate.notNull(tokens, "tokens must not be null");

        SnowballProgram stemmer = getStemmer(language);
        if (stemmer == null) {
            return tokens;
        }

        List<String> stemmedTokens = new ArrayList<String>(tokens.size());
        for (String token : tokens) {
            stemmedTokens.add(stem(stemmer, token));
        }
        return stemmedTokens;
    }

    /**
     * 
     * @param language
     * @param text
     * @return
     */
    public static String stem(String language, String text) {
        SnowballProgram stemmer = getStemmer(language);
        return stem(stemmer, text);
    }

    /**
     * 
     * @param text
     * @return
     */
    public static List<String> tokenize(String text) {
        Validate.notNull(text, "text must not be null");

        List<String> tokens = Arrays.asList(SimpleTokenizer.INSTANCE.tokenize(text));
        return tokens;
    }

    /**
     * <p>
     * Check, whether the given word is a stopword in the specified language.
     * </p>
     * 
     * @param language The language.
     * @param word The word to check.
     * @return <code>true</code> if word is stopword, <code>false</code> otherwise.
     */
    public static boolean isStopword(String language, String word) {
        Set<String> stopwords;
        if (language.equals("de")) {
            stopwords = STOPWORDS_DE;
        } else if (language.equals("en")) {
            stopwords = STOPWORDS_EN;
        } else {
            stopwords = Collections.emptySet();
        }
        return stopwords.contains(word.toLowerCase());
    }

    private ExtractionUtils() {
        // no instances.
    }

}
