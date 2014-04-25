package de.spektrumprojekt.i.ranker;

import java.util.ArrayList;
import java.util.Arrays;

public enum ScorerConfigurationFlag {

    /**
     * @deprecated Use feature weights instead
     */
    @Deprecated
    ONLY_USE_TERM_MATCHER_FEATURE_BUT_LEARN_FROM_FEATURES("onlyTermsButLearn"),

    DISCUSSION_PARTICIPATION_LEARN_FROM_PARENT_MESSAGE("learnParent"),

    DISCUSSION_PARTICIPATION_LEARN_FROM_ALL_PARENT_MESSAGES("learnAllParents"),

    @Deprecated
    DO_NOT_LEARN_FROM_DISCUSSION_PARTICIPATION("noLearnDiscussion"),

    DO_NOT_USE_DISCUSSION_FEATURES("noDiscussion"),

    DO_NOT_USE_CONTENT_MATCHER_FEATURE("noCMF"),

    USE_DIRECTED_USER_MODEL_ADAPTATION("DUMA"),

    USE_MESSAGE_GROUP_SPECIFIC_USER_MODEL("MG"),

    LEARN_NEGATIVE("learnNegative"),

    // USE_INVERSE_TERM_FREQUENCY,

    USE_CONTENT_MATCH_FEATURE_OF_SIMILAR_USERS("userSimCMF"),

    // will not infer observations from ratings
    NO_LEARNING_ONLY_SCORING("noLearn"),

    NO_INFORMATION_EXTRACTION("noIE"),

    // only usefull for analyzing term uniqness
    NO_USER_SPECIFIC_COMMANDS("noUserSpec"),

    // learns from every message. usefull for validation evaluation purposes
    LEARN_FROM_EVERY_MESSAGE("learnFromAll");

    private static ScorerConfigurationFlag[] VALUES_WITH_NULL;

    public static ScorerConfigurationFlag[] valuesWithNull() {
        if (VALUES_WITH_NULL == null) {
            synchronized (ScorerConfigurationFlag.class) {
                if (VALUES_WITH_NULL == null) {
                    ArrayList<ScorerConfigurationFlag> list = new ArrayList<ScorerConfigurationFlag>();
                    list.add(null);
                    list.addAll(Arrays.asList(ScorerConfigurationFlag.values()));
                    VALUES_WITH_NULL = list.toArray(new ScorerConfigurationFlag[] { });
                }
            }
        }
        return VALUES_WITH_NULL;
    }

    private final String shortName;

    private ScorerConfigurationFlag(String shortName) {
        this.shortName = shortName;
    }

    public String getShortName() {
        return shortName;
    }

}