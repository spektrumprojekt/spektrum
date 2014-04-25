package de.spektrumprojekt.i.evaluation.stats;

import org.apache.commons.lang3.StringUtils;

import de.spektrumprojekt.datamodel.observation.Interest;

public class SingleCount {
    public int pos;
    public int neg;
    public int normal;

    public void integrate(Interest interest) {
        switch (interest) {
        case EXTREME:
        case HIGH:
            pos++;
            break;
        case NORMAL:
            normal++;
            break;
        case LOW:
        case NONE:
            neg++;
            break;
        default:
            throw new UnsupportedOperationException(interest + "");
        }
    }

    public int overall() {
        return pos + neg + normal;
    }

    @Override
    public String toString() {
        return StringUtils.join(new String[] {
                "" + overall(),
                "" + pos,
                "" + normal,
                "" + neg },
                " ");
    }
}