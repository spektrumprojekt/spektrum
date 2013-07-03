package de.spektrumprojekt.i.user.hits;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections15.Transformer;

import de.spektrumprojekt.commons.computer.Computer;
import de.spektrumprojekt.datamodel.message.MessageGroup;
import de.spektrumprojekt.datamodel.user.UserSimilarity;
import de.spektrumprojekt.i.user.UserScore;
import de.spektrumprojekt.persistence.Persistence;
import edu.uci.ics.jung.algorithms.scoring.HITS;
import edu.uci.ics.jung.algorithms.scoring.HITS.Scores;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.graph.util.EdgeType;

public class HITSUserMentionComputer implements Computer {

    public class PerMessageGroupStruct {

        // the vertex is the user global id
        public DirectedSparseGraph<String, UserSimilarity> graph;
        // the vertex is the user global id
        public HITS<String, UserSimilarity> pageRank;
    }

    public enum ScoreToUse {
        HUB,
        AUTHORITY;
    }

    private Map<String, PerMessageGroupStruct> graphs =
            new HashMap<String, HITSUserMentionComputer.PerMessageGroupStruct>();

    private final Persistence persistence;

    private final ScoreToUse scoreToUse;

    public HITSUserMentionComputer(Persistence persistence, ScoreToUse scoreToUse) {
        if (persistence == null) {
            throw new IllegalArgumentException("persistence cannot be null.");
        }
        if (scoreToUse == null) {
            throw new IllegalArgumentException("scoreToUse cannot be null.");
        }
        this.persistence = persistence;
        this.scoreToUse = scoreToUse;
    }

    private void computeHITS(PerMessageGroupStruct struct) {
        Transformer<UserSimilarity, Double> transformer = new Transformer<UserSimilarity, Double>() {

            @Override
            public Double transform(UserSimilarity input) {
                return (double) input.getNumberOfMentions();
            }
        };
        struct.pageRank = new HITS<String, UserSimilarity>(struct.graph, transformer,
                0);

        struct.pageRank.evaluate();
    }

    private void createGraph(
            PerMessageGroupStruct perMessageGroupStruct, MessageGroup messageGroup) {
        perMessageGroupStruct.graph = new DirectedSparseGraph<String, UserSimilarity>();

        Collection<UserSimilarity> similarities = this.persistence.getUserSimilarities(messageGroup
                .getGlobalId());

        for (UserSimilarity userSim : similarities) {

            perMessageGroupStruct.graph.addEdge(userSim, userSim.getUserGlobalIdFrom(),
                    userSim.getUserGlobalIdTo(), EdgeType.DIRECTED);

        }
    }

    @Override
    public String getConfigurationDescription() {
        return this.getClass().getSimpleName();
    }

    private double getScore(Scores hitsScores) {
        double score;
        switch (scoreToUse) {
        case AUTHORITY:
            score = hitsScores.authority;
            break;
        case HUB:
            score = hitsScores.hub;
            break;
        default:
            throw new IllegalStateException("Unknown scoreToUse: " + scoreToUse);
        }
        return score;
    }

    public List<UserScore> getUserToUserInterest(String messageGroupGlobalId,
            Collection<String> userGlobalIdsToConsider) {

        PerMessageGroupStruct struct = this.graphs.get(messageGroupGlobalId);
        if (struct == null) {
            return Collections.emptyList();
        }

        List<UserScore> scores = new ArrayList<UserScore>(userGlobalIdsToConsider.size());
        users: for (String userGlobalIdToConsider : userGlobalIdsToConsider) {

            Scores hitsScores = struct.pageRank.getVertexScore(userGlobalIdToConsider);
            if (hitsScores == null) {
                continue users;
            }
            UserScore userScore = new UserScore(userGlobalIdToConsider, getScore(hitsScores));
            scores.add(userScore);
        }
        return scores;

    }

    /**
     * This should run periodically and only works if the UserSimilarityComputer runs
     * (incrementally)
     */
    @Override
    public void run() throws Exception {
        graphs.clear();
        Collection<MessageGroup> messageGroups = this.persistence.getAllMessageGroups();
        for (MessageGroup mg : messageGroups) {
            PerMessageGroupStruct struct = runForMessageGroup(mg);

            this.graphs.put(mg.getGlobalId(), struct);

        }

    }

    private PerMessageGroupStruct runForMessageGroup(MessageGroup mg) {

        PerMessageGroupStruct perMessageGroupStruct = new PerMessageGroupStruct();
        createGraph(perMessageGroupStruct, mg);
        computeHITS(perMessageGroupStruct);
        return perMessageGroupStruct;
    }

}
