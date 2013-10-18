package de.spektrumprojekt.datamodel.message;

import java.util.Comparator;

import de.spektrumprojekt.datamodel.message.Message;

/**
 * Comperator that compares the publication date of the messages
 * 
 * @author Communote GmbH - <a href="http://www.communote.de/">http://www.communote.com/</a>
 * 
 */
public class MessagePublicationDateComperator implements Comparator<Message> {

    public final static MessagePublicationDateComperator INSTANCE = new MessagePublicationDateComperator();

    @Override
    public int compare(Message o1, Message o2) {
        long diff = o1.getPublicationDate().getTime() - o2.getPublicationDate().getTime();
        if (diff == 0) {
            diff = o1.getGlobalId().hashCode() - o2.getGlobalId().hashCode();
        }
        if (diff > 0) {
            return 1;
        }
        if (diff < 0) {
            return -1;
        }
        return 0;
    }
}