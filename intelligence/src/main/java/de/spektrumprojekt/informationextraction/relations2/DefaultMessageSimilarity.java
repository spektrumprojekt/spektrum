package de.spektrumprojekt.informationextraction.relations2;

import de.spektrumprojekt.datamodel.message.Message;
import de.spektrumprojekt.helper.MessageHelper;
import de.spektrumprojekt.informationextraction.extractors.ExtractionUtils;

public class DefaultMessageSimilarity implements MessageSimilarity {

    public static final MessageSimilarity INSTANCE = new DefaultMessageSimilarity();

    private DefaultMessageSimilarity() {
        // singleton.
    }

    @Override
    public float getSimilarity(Message msg1, Message msg2) {

        String title1 = MessageHelper.getTitle(msg1);
        String title2 = MessageHelper.getTitle(msg2);
        float titleSim = ExtractionUtils.getLevenshteinSimilarity(title1, title2);

        String link1 = MessageHelper.getLink(msg1);
        String link2 = MessageHelper.getLink(msg2);
        float linkSim = ExtractionUtils.getLevenshteinSimilarity(link1, link2);

        String author1 = MessageHelper.getAuthor(msg1);
        String author2 = MessageHelper.getAuthor(msg2);
        float authorSim = author1.equalsIgnoreCase(author2) ? 1 : 0;

        return (titleSim + linkSim + authorSim) / 3;
    }

}
