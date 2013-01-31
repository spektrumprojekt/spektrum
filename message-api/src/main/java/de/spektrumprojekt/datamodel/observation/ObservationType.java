package de.spektrumprojekt.datamodel.observation;

/**
 * The type of an observation. The type defines the semantics of the observation itself..
 * 
 * @author Communote GmbH - <a href="http://www.communote.de/">http://www.communote.com/</a>
 * 
 */
public enum ObservationType {
    /**
     * A new message has been created and started a ranking. The observation will hold the global id
     * of the referring message.
     */
    MESSAGE,
    /**
     * A message has been liked. The observation will hold the message that was liked.
     */
    LIKE
}