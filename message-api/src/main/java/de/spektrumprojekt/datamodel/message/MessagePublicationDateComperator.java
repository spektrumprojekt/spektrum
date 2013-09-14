package de.spektrumprojekt.datamodel.message;

import java.util.Comparator;

import de.spektrumprojekt.datamodel.message.Message;

public class MessagePublicationDateComperator implements Comparator<Message> {

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