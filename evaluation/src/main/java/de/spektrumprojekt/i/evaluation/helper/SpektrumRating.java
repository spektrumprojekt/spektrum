package de.spektrumprojekt.i.evaluation.helper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import de.spektrumprojekt.commons.output.SpektrumParseableElement;
import de.spektrumprojekt.datamodel.message.Message;
import de.spektrumprojekt.datamodel.observation.Interest;

/**
 * TODO move it to spektrum ?
 * 
 * @author Torsten
 * 
 */
public class SpektrumRating implements SpektrumParseableElement {

    public static String getColumnHeader() {
        return StringUtils.join(new String[] { "userGlobalId", "messageGlobalId",
                "interest" }, " ");
    }

    public static String getIdentifier(String userGlobalId, String messageGlobalId) {
        return userGlobalId + "#" + messageGlobalId;
    }

    public static Collection<Message> getMessages(Map<String, List<SpektrumRating>> testRatings) {
        Collection<Message> messages = new HashSet<Message>();

        for (List<SpektrumRating> ratings : testRatings.values()) {
            for (SpektrumRating rating : ratings) {
                messages.add(rating.getMessage());
            }
        }
        return messages;
    }

    public static List<SpektrumRating> sortSpektrumRatings(Map<?, List<SpektrumRating>> mapOfRatings) {
        List<SpektrumRating> allRatings = new ArrayList<SpektrumRating>();
        for (List<SpektrumRating> ratings : mapOfRatings.values()) {
            allRatings.addAll(ratings);
        }
        Collections.sort(allRatings, new SpektrumRatingCreationDateComparator());

        return allRatings;

    }

    public static Map<String, List<SpektrumRating>> splitByMessages(
            Collection<SpektrumRating> ratings) {
        Map<String, List<SpektrumRating>> messageToRatings = new HashMap<String, List<SpektrumRating>>();
        for (SpektrumRating rating : ratings) {
            List<SpektrumRating> userRatings = messageToRatings.get(rating.getMessage()
                    .getGlobalId());
            if (userRatings == null) {
                userRatings = new ArrayList<SpektrumRating>();
                messageToRatings.put(rating.getMessage().getGlobalId(), userRatings);
            }
            userRatings.add(rating);
        }
        return messageToRatings;
    }

    public static Map<String, List<SpektrumRating>> splitByUsers(Collection<SpektrumRating> ratings) {
        Map<String, List<SpektrumRating>> userToRatings = new HashMap<String, List<SpektrumRating>>();
        for (SpektrumRating rating : ratings) {
            List<SpektrumRating> userRatings = userToRatings.get(rating.getUserGlobalId());
            if (userRatings == null) {
                userRatings = new ArrayList<SpektrumRating>();
                userToRatings.put(rating.getUserGlobalId(), userRatings);
            }
            userRatings.add(rating);
        }
        return userToRatings;
    }

    private Interest interest;

    private Message message;

    private String userGlobalId;

    public SpektrumRating() {

    }

    public SpektrumRating(SpektrumRating clone) {
        this.interest = clone.getInterest();
        this.message = clone.getMessage();
        this.userGlobalId = clone.getUserGlobalId();
    }

    public SpektrumRating(String line, Map<String, Message> messageMap) {
        String[] vals = line.split(" ");
        int i = 0;
        this.userGlobalId = vals[i++];
        String msgGlobalId = vals[i++];
        this.interest = Interest.valueOf(vals[i++]);
        this.message = messageMap.get(msgGlobalId);

        if (userGlobalId == null) {
            throw new IllegalArgumentException("userGlobalId cannot be null. line=" + line);
        }
        if (message == null) {
            throw new IllegalArgumentException("message cannot be null. line=" + line
                    + " msgGlobalId=" + msgGlobalId);
        }
        if (interest == null) {
            throw new IllegalArgumentException("interest cannot be null. line=" + line);
        }
    }

    public String getIdentifier() {
        return getIdentifier(this.userGlobalId, this.getMessage().getGlobalId());
    }

    public Interest getInterest() {
        return interest;
    }

    public Message getMessage() {
        return message;
    }

    public String getUserGlobalId() {
        return userGlobalId;
    }

    public void setInterest(Interest interest) {
        this.interest = interest;
    }

    public void setMessage(Message message) {
        this.message = message;
    }

    public void setUserGlobalId(String userGlobalId) {
        this.userGlobalId = userGlobalId;
    }

    public String toParseableString() {
        return StringUtils.join(new String[] { this.userGlobalId, this.message.getGlobalId(),
                this.interest.name() }, " ");
    }

}
