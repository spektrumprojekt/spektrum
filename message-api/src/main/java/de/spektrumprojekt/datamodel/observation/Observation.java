package de.spektrumprojekt.datamodel.observation;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;

import de.spektrumprojekt.datamodel.identifiable.Identifiable;
import de.spektrumprojekt.datamodel.message.Message;
import de.spektrumprojekt.datamodel.user.User;

/**
 * An object of this class stores some observation made about an user. There is a type of
 * observation {@link ObservationType} defining what kind of observation was made. Based on the type
 * the {@link #observation} itself should store the information, e.g. the id of the message that was
 * stored or liked.
 * 
 * @author Communote GmbH - <a href="http://www.communote.de/">http://www.communote.com/</a>
 * 
 */
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = "globalId"))
public class Observation extends Identifiable {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private String userGlobalId;;

    private String messageGlobalId;

    @Temporal(TemporalType.TIMESTAMP)
    private Date observationDate;

    private ObservationType observationType;

    private String observation;

    // a derived interest, if null it will be determined by observation type
    private Interest interest;

    /**
     * for jpa
     */
    protected Observation() {
        // for jpa
    }

    public Observation(String userGlobalId,
            String messageGlobalId,
            ObservationType observationType,
            String observation,
            Date observationDate,
            Interest interest) {
        if (userGlobalId == null) {
            throw new IllegalArgumentException("userGlobalId cannot be null");
        }
        if (messageGlobalId == null) {
            throw new IllegalArgumentException("messageGlobalId cannot be null");
        }
        if (observationType == null) {
            throw new IllegalArgumentException("observationType cannot be null");
        }
        if (observationDate == null) {
            observationDate = new Date();
        }
        this.userGlobalId = userGlobalId;
        this.messageGlobalId = messageGlobalId;
        this.observationType = observationType;
        this.observation = observation;
        this.observationDate = observationDate;
        this.interest = interest;
    }

    public Observation(User user, Message message, ObservationType observationType,
            String observation,
            Date observationDate, Interest interest) {
        this(user == null ? null : user.getGlobalId(),
                message == null ? null : message.getGlobalId(),
                observationType,
                observation,
                observationDate, interest);
    }

    public Observation(User user, ObservationType observationType, String observation) {
        this(user, null, observationType, observation, null, null);
    }

    public Interest getInterest() {
        return interest;
    }

    public String getMessageGlobalId() {
        return messageGlobalId;
    }

    public String getObservation() {
        return observation;
    }

    public Date getObservationDate() {
        return observationDate;
    }

    public ObservationType getObservationType() {
        return observationType;
    }

    public String getUserGlobalId() {
        return userGlobalId;
    }

    public void setInterest(Interest interest) {
        this.interest = interest;
    }

    public void setMessageGlobalId(String messageGlobalId) {
        this.messageGlobalId = messageGlobalId;
    }

    public void setObservation(String observation) {
        this.observation = observation;
    }

    public void setObservationDate(Date observationDate) {
        this.observationDate = observationDate;
    }

    public void setObservationType(ObservationType observationType) {
        this.observationType = observationType;
    }

    public void setUserGlobalId(String userGlobalId) {
        this.userGlobalId = userGlobalId;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Observation [userGlobalId=");
        builder.append(userGlobalId);
        builder.append(", messageGlobalId=");
        builder.append(messageGlobalId);
        builder.append(", observationDate=");
        builder.append(observationDate);
        builder.append(", observationType=");
        builder.append(observationType);
        builder.append(", observation=");
        builder.append(observation);
        builder.append(", interest=");
        builder.append(interest);
        builder.append("]");
        return builder.toString();
    }
}
