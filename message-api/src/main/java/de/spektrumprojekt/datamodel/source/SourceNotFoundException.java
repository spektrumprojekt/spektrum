package de.spektrumprojekt.datamodel.source;

public class SourceNotFoundException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private String sourceGlobalId;

    public SourceNotFoundException(String sourceGlobalId) {
        super();
        this.sourceGlobalId = sourceGlobalId;
    }

    public SourceNotFoundException(String message, String sourceGlobalId) {
        super(message);
        this.sourceGlobalId = sourceGlobalId;
    }

    public String getSourceGlobalId() {
        return sourceGlobalId;
    }

    public void setSourceGlobalId(String sourceGlobalId) {
        this.sourceGlobalId = sourceGlobalId;
    }

}
