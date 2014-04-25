package de.spektrumprojekt.i.similarity.messagegroup;

import java.util.List;

import de.spektrumprojekt.configuration.ConfigurationDescriptable;

public interface MessageGroupSimilarityRetriever extends ConfigurationDescriptable {

    public float getMessageGroupSimilarity(
            Long messageGroupId1, Long messageGroupId2);

    /**
     * 
     * @param messageGroupId
     * @param topN
     *            only return the mgs
     * @return list of mg sims sorted by similarity, limited by top n
     */
    public List<MessageGroupSimilarity> getTopSimilarities(Long messageGroupId, int topN);

}
