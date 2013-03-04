package de.spektrumprojekt.informationextraction.extractors;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import de.spektrumprojekt.datamodel.message.Term;

public final class MockTagSource implements TagSource {

    private final Set<Term> tags = new HashSet<Term>();

    @Override
    public Collection<Term> getTags() {
        return tags;
    }

    public void addTag(Term tag) {
        tags.add(tag);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("MockTagSource [tags=");
        builder.append(tags);
        builder.append("]");
        return builder.toString();
    }

}
