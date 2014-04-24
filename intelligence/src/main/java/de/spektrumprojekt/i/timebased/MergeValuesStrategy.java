package de.spektrumprojekt.i.timebased;

import java.util.Map;

public interface MergeValuesStrategy {

    float merge(Map<String, Float> values);

}
