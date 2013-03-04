package de.spektrumprojekt.informationextraction.extractors;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import de.spektrumprojekt.informationextraction.InformationExtractionContext;

public class WordNGramsExtractorCommand extends AbstractTokenExtractorCommand {

    private final int nGramsLength;

    public WordNGramsExtractorCommand(boolean useMessageGroupIdForToken,
            boolean assertMessageGroup,
            int nGramsLength, int minimumTermLength) {
        super(useMessageGroupIdForToken, assertMessageGroup, minimumTermLength);
        this.nGramsLength = nGramsLength;
    }

    @Override
    public String getConfigurationDescription() {
        return super.getConfigurationDescription() + " nGrams: " + nGramsLength;
    }

    @Override
    public void process(InformationExtractionContext context) {

        String text = context.getCleanText();
        if (StringUtils.isEmpty(text)) {
            return;
        }

        List<String> tokens = this.tokenizeAndRemoveStopwords(context);
        tokens = ExtractionUtils.createNGrams(tokens, nGramsLength);
        tokens = cleanTokens(tokens);

        this.addTokensToMessagePart(context, tokens);
    }

}
