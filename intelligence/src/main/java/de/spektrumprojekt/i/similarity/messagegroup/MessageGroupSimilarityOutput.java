package de.spektrumprojekt.i.similarity.messagegroup;

import de.spektrumprojekt.commons.output.SpektrumParseableElementFileOutput;

public class MessageGroupSimilarityOutput extends
        SpektrumParseableElementFileOutput<MessageGroupSimilarity> {

    public MessageGroupSimilarityOutput() {
        super(MessageGroupSimilarity.class);
    }

    @Override
    protected MessageGroupSimilarity createNewElement(String line) {
        return new MessageGroupSimilarity(line);
    }

    @Override
    protected String getHeader() {
        return MessageGroupSimilarity.getColumnHeader();
    }

}