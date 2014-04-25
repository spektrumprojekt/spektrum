package de.spektrumprojekt.i.evaluation;

import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import de.spektrumprojekt.configuration.ConfigurationDescriptable;
import de.spektrumprojekt.datamodel.message.Message;
import de.spektrumprojekt.datamodel.message.MessageGroup;
import de.spektrumprojekt.datamodel.message.MessageRelation;
import de.spektrumprojekt.i.evaluation.helper.SpektrumRating;

public interface MessageDataSetProvider extends ConfigurationDescriptable {

    public void addOnlyUseUserIdsForRatings(Collection<String> userIds);

    public void addOnlyUseUserIdsForRatings(String... userIds);

    /**
     * Call this if the data set provider is not longer used for accessing any message or rating
     */
    public void close();

    public Date getEndDate();

    public Date getFirstMessageDate();

    public Collection<MessageGroup> getMessageGroups();

    /**
     * 
     * @return an iterator that returns the oldest message first
     */
    public Iterator<Message> getMessageIterator();

    public MessageRelation getMessageRelation(Message spektrumMessage);

    /**
     * 
     * @return the messages sorted by time, oldest first
     * @deprecated use {@link #getMessageIterator()}
     */
    @Deprecated
    public List<Message> getMessages();

    public int getMessageSize();

    /**
     * 
     * @return a name that can be used as directory name
     */
    public String getName();

    /**
     * 
     * @return the ratings, unsorted
     */
    public List<SpektrumRating> getRatings();

    public Date getStartDate();

    public Collection<String> getUserGlobalIds();

    public Collection<String> getUsersWithRatingsGlobalIds();

    public void init() throws Exception;

    /**
     * Calls this to reuse the data set provider. No new init is necassary.
     */
    public void reset();

    public void setEndDate(Date endDate);

    public void setStartDate(Date startDate);

    public void setTopNRatersToUse(Integer topNRatersToUse);

}
