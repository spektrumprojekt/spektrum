package de.spektrumprojekt.informationextraction.extractors;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.Bag;
import org.apache.commons.collections.bag.HashBag;
import org.apache.commons.lang3.StringUtils;

import de.spektrumprojekt.commons.chain.Command;
import de.spektrumprojekt.datamodel.message.MessageGroup;
import de.spektrumprojekt.datamodel.message.ScoredTerm;
import de.spektrumprojekt.datamodel.message.Term;
import de.spektrumprojekt.informationextraction.InformationExtractionContext;

public abstract class AbstractTokenExtractorCommand implements
        Command<InformationExtractionContext> {

    private final boolean useMessageGroupIdForToken;
    private final boolean assertMessageGroup;
    private final int minimumTermLength;

    public AbstractTokenExtractorCommand(boolean useMessageGroupIdForToken,
            boolean assertMessageGroup, int minimumTermLength) {
        this.useMessageGroupIdForToken = useMessageGroupIdForToken;
        this.assertMessageGroup = assertMessageGroup;
        this.minimumTermLength = minimumTermLength;
    }

    protected void addTokensToMessagePart(InformationExtractionContext context, List<String> tokens) {
        MessageGroup messageGroup = getMessageGroupForToken(context);

        Bag tokenBag = new HashBag(tokens);
        int highestCount = BagHelper.getHighestCount(tokenBag);
        for (Object tokenObj : tokenBag) {
            String token = (String) tokenObj;
            if (token.length() < minimumTermLength) {
                continue;
            }
            float frequency = (float) tokenBag.getCount(token) / highestCount;
            context.getMessagePart().addScoredTerm(
                    new ScoredTerm(
                            context.getPersistence().getOrCreateTerm(
                                    Term.TermCategory.TERM,
                                    Term.getMessageGroupSpecificTermValue(messageGroup, token)
                                    ),
                            frequency));
        }
    }

    protected List<String> cleanTokens(List<String> tokens) {
        List<String> cleanTokens = new ArrayList<String>();
        for (String token : tokens) {
            if (token.length() < 2) {
                continue;
            }
            cleanTokens.add(token);
        }
        return cleanTokens;
    }

    @Override
    public String getConfigurationDescription() {
        return this.getClass().getSimpleName()
                + " useMessageGroupIdForToken: " + useMessageGroupIdForToken
                + " minimumTermLength: " + minimumTermLength
                + " assertMessageGroup: " + assertMessageGroup;

    }

    private MessageGroup getMessageGroupForToken(InformationExtractionContext context) {
        if (this.useMessageGroupIdForToken) {
            MessageGroup group = context.getMessage().getMessageGroup();
            if (group == null && this.assertMessageGroup) {
                throw new IllegalStateException("messagegroup not set for message="
                        + context.getMessage());

            }
            return group;
        }
        return null;
    }

    private String getTokenPrefix(InformationExtractionContext context) {
        String tokenPrefix = StringUtils.EMPTY;
        if (this.useMessageGroupIdForToken) {
            MessageGroup group = context.getMessage().getMessageGroup();
            if (group == null) {
                if (this.assertMessageGroup) {
                    throw new IllegalStateException("messagegroup not set for message="
                            + context.getMessage());
                }
            } else {
                tokenPrefix = group.getId() + Term.TERM_MESSAGE_GROUP_ID_SEPERATOR;
            }
        }
        return tokenPrefix;
    }

    @Override
    public abstract void process(InformationExtractionContext context);

    protected List<String> removeStopwords(String language, List<String> tokens) {
        List<String> ret = new ArrayList<String>();
        for (String token : tokens) {
            if (!ExtractionUtils.isStopword(language, token.toLowerCase())) {
                ret.add(token);
            }
        }
        return ret;
    }

    protected List<String> tokenizeAndRemoveStopwords(InformationExtractionContext context) {
        String language = LanguageDetectorCommand.getAnnotatedLanguage(context.getMessage());

        String text = context.getCleanText();
        List<String> tokens = ExtractionUtils.tokenize(text);
        tokens = removeStopwords(language, tokens);

        return tokens;
    }

}
