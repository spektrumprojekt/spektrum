package de.spektrumprojekt.i.similarity.messagegroup;

import de.spektrumprojekt.configuration.ConfigurationDescriptable;

public interface MessageGroupSimilarityRetriever extends ConfigurationDescriptable {

    public double getMessageGroupSimilarity(
            Long messageGroupId1, Long messageGroupId2);

}
