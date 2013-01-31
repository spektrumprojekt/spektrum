package de.spektrumprojekt.datamodel.observation;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;

import de.spektrumprojekt.datamodel.identifiable.Identifiable;
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
    private static final long serialVersionUID = 1L;;

    private User user;

    @Temporal(TemporalType.TIMESTAMP)
    private Date observationDate;
    private ObservationType observationType;

    private String observation;

    /**
     * for jpa
     */
    protected Observation() {
        // for jpa
    }

    public Observation(User user, ObservationType observationType, String observation) {
        this(user, observationType, observation, null);
    }

    public Observation(User user, ObservationType observationType, String observation,
            Date observationDate) {
        if (user == null) {
            throw new IllegalArgumentException("user cannot be null");
        }
        if (observationType == null) {
            throw new IllegalArgumentException("observationType cannot be null");
        }
        if (observation == null) {
            throw new IllegalArgumentException("observation cannot be null");
        }
        if (observationDate == null) {
            observationDate = new Date();
        }
        this.user = user;
        this.observationType = observationType;
        this.observation = observation;
        this.observationDate = observationDate;
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

    public User getUser() {
        return user;
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

    public void setUser(User user) {
        this.user = user;
    }
}
