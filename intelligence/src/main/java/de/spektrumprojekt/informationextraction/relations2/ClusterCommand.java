package de.spektrumprojekt.informationextraction.relations2;

import java.io.PrintStream;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.spektrumprojekt.commons.chain.Command;
import de.spektrumprojekt.datamodel.message.Message;
import de.spektrumprojekt.datamodel.message.MessageRelation;
import de.spektrumprojekt.datamodel.message.MessageRelation.MessageRelationType;
import de.spektrumprojekt.helper.MessageHelper;
import de.spektrumprojekt.informationextraction.InformationExtractionContext;

/**
 * @author Philipp Katz
 */
public class ClusterCommand implements Command<InformationExtractionContext> {

    /** The logger for this class. */
    private static final Logger LOGGER =
            LoggerFactory.getLogger(ClusterCommand.MessageCluster.class);

    /**
     * <p>
     * Represents a cluster of grouped {@link Message}s.
     * </p>
     */
    private static final class MessageCluster implements Iterable<Message> {

        private final Set<Message> messages = new HashSet<Message>();

        private long lastUpdate;

        public MessageCluster(Message message) {
            add(message);
        }

        public void add(Message message) {
            messages.add(message);
            lastUpdate = System.currentTimeMillis();
        }

        public long getLastUpdate() {
            return lastUpdate;
        }

        @Override
        public Iterator<Message> iterator() {
            return messages.iterator();
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("MessageCluster [size=");
            builder.append(messages.size());
            builder.append("]");
            return builder.toString();
        }

        public String[] getContainedIds() {
            String[] ids = new String[messages.size()];
            int i = 0;
            for (Message msg : messages) {
                ids[i++] = msg.getGlobalId();
            }
            return ids;
        }

    }

    /** Strategy for calculating similarity measure. */
    private final MessageSimilarity similarityMeasure;

    /** This is currently only kept in memory. */
    private final Set<MessageCluster> messageClusters;

    private final float similarityThreshold;

    private final int maxClustersToKeep;

    public ClusterCommand(MessageSimilarity similarity, float similarityThreshold,
            int maxClustersToKeep) {
        this.similarityMeasure = similarity;
        this.similarityThreshold = similarityThreshold;
        this.maxClustersToKeep = maxClustersToKeep;
        this.messageClusters = new HashSet<MessageCluster>();
    }

    @Override
    public String getConfigurationDescription() {
        return getClass().getName();
    }

    @Override
    public void process(InformationExtractionContext context) {
        LOGGER.trace("Process {}", MessageHelper.getTitle(context.getMessage()));

        Message message = context.getMessage();

        float maxSimilarity = 0;
        MessageCluster bestCluster = null;
        for (MessageCluster messageCluster : messageClusters) {
            float clusterSimilarity = getSimilarityToCluster(message, messageCluster);
            if (clusterSimilarity > maxSimilarity) {
                maxSimilarity = clusterSimilarity;
                bestCluster = messageCluster;
            }
        }
        if (maxSimilarity > similarityThreshold && bestCluster != null) {
            bestCluster.add(message);
            LOGGER.debug("Add message {} to cluster {}", message.getGlobalId(), bestCluster);
            // create a relation
            String[] relatedIds = bestCluster.getContainedIds();
            MessageRelation relation = new MessageRelation(MessageRelationType.RELATION,
                    message.getGlobalId(), relatedIds);
            context.getPersistence().storeMessageRelation(message, relation);
            context.add(relation);
        } else {
            MessageCluster messageCluster = new MessageCluster(message);
            messageClusters.add(messageCluster);
            LOGGER.debug("Create new cluster for {}", message.getGlobalId());
            pruneClustersConditionally();
        }

    }

    private void pruneClustersConditionally() {
        if (messageClusters.size() <= maxClustersToKeep) {
            return;
        }
        long currentTimeStamp = System.currentTimeMillis();
        long oldestAge = 0;
        MessageCluster oldestCluster = null;

        for (MessageCluster messageCluster : messageClusters) {
            long age = currentTimeStamp - messageCluster.getLastUpdate();
            if (age > oldestAge) {
                oldestAge = age;
                oldestCluster = messageCluster;
            }
        }
        LOGGER.debug("Removing oldest cluster with age of {}", oldestAge);
        messageClusters.remove(oldestCluster);
    }

    private float getSimilarityToCluster(Message message, MessageCluster messageCluster) {
        float similarity = 0;
        for (Message clusterMessage : messageCluster) {
            float currentSimilarity = similarityMeasure.getSimilarity(message, clusterMessage);
            if (currentSimilarity > similarity) {
                similarity = currentSimilarity;
            }
        }
        return similarity;
    }

    /** package *** For testing purposes only. */
    void dumpClusters(PrintStream printStream) {
        int i = 0;
        for (MessageCluster messageCluster : messageClusters) {
            printStream.println("MessageCluster " + i);
            for (Message message : messageCluster) {
                printStream.println("Message ID '" + message.getGlobalId() + "'");
                printStream.println("Link: " + MessageHelper.getLink(message));
                printStream.println("Title: " + MessageHelper.getTitle(message));
            }
            printStream.println("--------------------");
            i++;
        }
    }

}
