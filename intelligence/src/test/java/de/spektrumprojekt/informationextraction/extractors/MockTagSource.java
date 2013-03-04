package de.spektrumprojekt.informationextraction.extractors;

import java.util.Collection;
import java.util.Collections;

import de.spektrumprojekt.datamodel.message.Term;

public class MockTagSource implements TagSource {

    @Override
    public Collection<Term> getTags() {
        return Collections.emptyList();
    }

}
