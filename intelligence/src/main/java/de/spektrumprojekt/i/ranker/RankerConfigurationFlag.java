package de.spektrumprojekt.i.ranker;

public enum RankerConfigurationFlag {
    ONLY_USE_TERM_MATCHER_FEATURE("onlyTerms"),

    ONLY_USE_TERM_MATCHER_FEATURE_BUT_LEARN_FROM_FEATURES("onlyTermsButLearn"),

    DISCUSSION_PARTICIPATION_LEARN_FROM_PARENT_MESSAGE("learnParent"),

    DISCUSSION_PARTICIPATION_LEARN_FROM_ALL_PARENT_MESSAGES("learnAllParents"),

    DO_NOT_LEARN_FROM_DISCUSSION_PARTICIPATION("noLearnDiscussion"),

    DO_NOT_USE_DISCUSSION_FEATURES("noDiscussion"),

    DO_NOT_USE_CONTENT_MATCHER_FEATURE("noCMF"),

    USE_DIRECTED_USER_MODEL_ADAPTATION("DUMA"),

    USE_MESSAGE_GROUP_SPECIFIC_USER_MODEL("MG"),

    LEARN_NEGATIVE("learnNegative"),

    USE_HALF_SCORE_ON_NON_PARTICIPATING_ANSWERS("halfOnNonPart"),

    // USE_INVERSE_TERM_FREQUENCY,

    USE_CONTENT_MATCH_FEATURE_OF_SIMILAR_USERS("userSimCMF"),

    // will not infer observations from ratings
    NO_LEARNING_ONLY_RANKING("noLearn"),

    // only usefull for analyzing term uniqness
    NO_USER_SPECIFIC_COMMANDS("noUserSpec"),

    // learns from every message. usefull for validation evaluation purposes
    LEARN_FROM_EVERY_MESSAGE("learnFromAll");

    private final String shortName;

    private RankerConfigurationFlag(String shortName) {
        this.shortName = shortName;
    }

    public String getShortName() {
        return shortName;
    }

}