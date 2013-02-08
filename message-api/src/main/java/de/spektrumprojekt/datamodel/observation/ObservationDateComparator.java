package de.spektrumprojekt.datamodel.observation;

import java.util.Comparator;

public class ObservationDateComparator implements Comparator<Observation> {

    private final boolean descending;

    public ObservationDateComparator() {
        this(false);
    }

    public ObservationDateComparator(boolean descending) {
        this.descending = descending;
    }

    @Override
    public int compare(Observation o1, Observation o2) {

        int result = o1.getObservationDate().compareTo(o2.getObservationDate());
        if (descending) {
            result = -1 * result;
        }
        return result;
    }

}