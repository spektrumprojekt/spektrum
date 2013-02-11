package de.spektrumprojekt.helper;

import java.util.Collection;
import java.util.HashSet;

import de.spektrumprojekt.datamodel.identifiable.Identifiable;

public final class IdentifiableHelper {

    public static Collection<String> getGlobalIds(Collection<? extends Identifiable> ids) {
        Collection<String> strings = new HashSet<String>();
        for (Identifiable id : ids) {
            if (id != null) {
                strings.add(id.getGlobalId());
            }
        }
        return strings;
    }

    private IdentifiableHelper() {

    }
}
