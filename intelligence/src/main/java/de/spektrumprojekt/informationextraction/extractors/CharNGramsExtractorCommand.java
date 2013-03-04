package de.spektrumprojekt.informationextraction.extractors;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import de.spektrumprojekt.informationextraction.InformationExtractionContext;

public class CharNGramsExtractorCommand extends AbstractTokenExtractorCommand {

    private final int nGramsLength;
    private final boolean removeStopwords;

    public CharNGramsExtractorCommand(boolean useMessageGroupIdForToken,
            boolean assertMessageGroup,
            int nGramsLength,
            boolean removeStopwords) {
        super(useMessageGroupIdForToken, assertMessageGroup, nGramsLength);
        this.nGramsLength = nGramsLength;
        this.removeStopwords = removeStopwords;
    }

    @Override
    public String getConfigurationDescription() {
        return super.getConfigurationDescription()
                + " nGramsLength: " + nGramsLength
                + " removeStopwords: " + removeStopwords;
    }

    @Override
    public void process(InformationExtractionContext context) {

        String text = context.getCleanText();
        if (StringUtils.isEmpty(text)) {
            return;
        }
        List<String> tokens;

        if (removeStopwords) {
            tokens = this.tokenizeAndRemoveStopwords(context);
            text = StringUtils.join(tokens, " ");
        }

        tokens = ExtractionUtils.createCharNGrams(text, nGramsLength);
        tokens = cleanTokens(tokens);

        this.addTokensToMessagePart(context, tokens);
    }

}
