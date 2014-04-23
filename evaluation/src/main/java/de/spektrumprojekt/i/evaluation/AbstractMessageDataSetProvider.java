package de.spektrumprojekt.i.evaluation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import de.spektrumprojekt.datamodel.common.Property;
import de.spektrumprojekt.datamodel.message.Message;
import de.spektrumprojekt.datamodel.message.MessageGroup;
import de.spektrumprojekt.datamodel.message.MessagePart;
import de.spektrumprojekt.datamodel.message.MessagePublicationDateComperator;
import de.spektrumprojekt.datamodel.message.MessageRelation;
import de.spektrumprojekt.i.evaluation.helper.SpektrumRating;
import de.spektrumprojekt.i.informationextraction.InformationExtractionCommand;

public abstract class AbstractMessageDataSetProvider implements MessageDataSetProvider {

    protected boolean initalized;

    protected boolean initalizedSuccess;

    protected Collection<String> userGlobalIds;

    protected Collection<String> usersWithRatingsGlobalIds;

    protected List<Message> messages;

    protected Map<String, Message> messageGlobalIdToMessage;
    protected Map<String, MessageRelation> messageGlobalIdToMessageRelation;
    protected Map<String, MessageGroup> messageGroups;

    private Date startDate;
    private Date endDate;
    protected List<SpektrumRating> ratings;
    private final Collection<String> userIdsToUseForRating = new HashSet<String>();

    private Integer topNRatersToUse;

    protected void addMessage(Message message) {
        if (message != null) {
            this.messages.add(message);
            this.messageGlobalIdToMessage.put(message.getGlobalId(), message);
        }
    }

    public void addOnlyUseUserIdsForRatings(Collection<String> userIds) {
        if (userIds != null) {
            this.userIdsToUseForRating.addAll(userIds);
        }
    }

    public void addOnlyUseUserIdsForRatings(String... userIds) {
        for (String userId : userIds) {
            this.userIdsToUseForRating.add(userId);
        }
    }

    protected void checkInit() {
        if (!initalized) {
            throw new RuntimeException("Not initalized. call #init !");
        }
        if (!initalizedSuccess) {
            throw new RuntimeException(
                    "There has been an error during initalizing before. Will not continue to work.");
        }
    }

    protected void constructMapsAndLists() {
        this.messages = new ArrayList<Message>();
        this.messageGlobalIdToMessage = new HashMap<String, Message>();
        this.ratings = new ArrayList<SpektrumRating>();
        this.messageGroups = new HashMap<String, MessageGroup>();
        this.userGlobalIds = new HashSet<String>();

        this.messageGlobalIdToMessageRelation = new HashMap<String, MessageRelation>();
        this.usersWithRatingsGlobalIds = new HashSet<String>();
    }

    protected abstract void doInitalization() throws Exception;

    public String getConfigurationDescription() {
        return this.getClass().getSimpleName()
                + " startDate: " + startDate
                + " endDate: " + endDate
                + " userIdsForRatings: " + this.userIdsToUseForRating
                + " topNRatersToUse: " + this.topNRatersToUse;
    }

    public Date getEndDate() {
        return endDate;
    }

    public Date getFirstMessageDate() {
        if (!initalized) {
            throw new RuntimeException("Not initalized. call #init !");
        }
        return this.messages == null || this.messages.size() == 0 ? new Date(0) : this.messages
                .get(0).getPublicationDate();
    }

    public Collection<MessageGroup> getMessageGroups() {
        if (this.messageGroups == null) {
            loadMessages();
        }
        return this.messageGroups.values();
    }

    public Iterator<Message> getMessageIterator() {
        checkInit();
        List<Message> messages = new ArrayList<Message>(this.messages);

        Collections.sort(messages, MessagePublicationDateComperator.INSTANCE);

        return messages.iterator();
    }

    public MessageRelation getMessageRelation(Message spektrumMessage) {
        if (messageGlobalIdToMessageRelation == null) {
            loadMessages();
        }
        return this.messageGlobalIdToMessageRelation.get(spektrumMessage.getGlobalId());
    }

    public List<Message> getMessages() {
        if (messages == null) {
            loadMessages();
        }
        return messages;
    }

    public int getMessageSize() {
        checkInit();
        return this.messages.size();
    }

    public List<SpektrumRating> getRatings() {
        if (ratings == null) {
            loadRatings();
        }
        return ratings;
    }

    public Date getStartDate() {
        return startDate;
    }

    public Integer getTopNRatersToUse() {
        return topNRatersToUse;
    }

    public Collection<String> getUserGlobalIds() {
        if (userGlobalIds == null) {
            loadUsers();
        }

        return userGlobalIds;
    }

    public Collection<String> getUsersWithRatingsGlobalIds() {
        return usersWithRatingsGlobalIds;
    }

    public synchronized void init() throws Exception {
        if (initalizedSuccess) {
            return;
        }
        if (initalized && !initalizedSuccess) {
            throw new RuntimeException(
                    "There has been an error during initalizing before. Will not continue to work.");
        }
        initalized = true;

        doInitalization();

        initalizedSuccess = true;
    }

    public boolean isInRage(Date creationDate) {
        if (startDate != null && startDate.after(creationDate)) {
            return false;
        }
        if (endDate != null && endDate.before(creationDate)) {
            return false;
        }
        return true;
    }

    protected abstract void loadMessages();

    protected abstract void loadRatings();

    protected abstract void loadUsers();

    public void reset() {
        checkInit();
        for (Message mes : messages) {
            for (MessagePart mp : mes.getMessageParts()) {
                mp.getScoredTerms().clear();
            }
            Property prop = mes.getPropertiesAsMap().get(
                    InformationExtractionCommand.PROPERTY_INFORMATION_EXTRACTION_EXECUTION_DATE);
            if (prop != null) {
                mes.getProperties().remove(prop);
            }
        }
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public void setTopNRatersToUse(Integer topNRatersToUse) {
        this.topNRatersToUse = topNRatersToUse;
    }

    protected boolean useUserIdForRating(String userId) {
        return userIdsToUseForRating.isEmpty() || userIdsToUseForRating.contains(userId);
    }

}
