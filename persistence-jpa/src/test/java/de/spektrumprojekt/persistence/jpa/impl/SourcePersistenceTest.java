package de.spektrumprojekt.persistence.jpa.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import de.spektrumprojekt.configuration.properties.SimpleProperties;
import de.spektrumprojekt.datamodel.common.Property;
import de.spektrumprojekt.datamodel.source.Source;
import de.spektrumprojekt.persistence.jpa.JPAConfiguration;

public class SourcePersistenceTest {

    private static final String VAL1 = "val1";
    private static final String VAL2 = "val2";

    private static final String ACCESS_PARAM1 = "accessParam1";

    /**
     * The name of the persistence unit for testing purposes, as configured in
     * META-INF/persistence.xml.
     */
    private static final String PERSISTENCE_UNIT_NAME = "de.spektrumprojekt.datamodel.test";

    private SourcePersistence persistence;

    private void compare(Source source, Source persistedSource, String val) {
        Assert.assertNotNull(persistedSource);
        Assert.assertEquals(source.getGlobalId(), persistedSource.getGlobalId());
        Assert.assertEquals(source.getAccessParameters().size(), persistedSource
                .getAccessParameters().size());
        Assert.assertNotNull(persistedSource.getAccessParameter(ACCESS_PARAM1).getPropertyValue());
        Assert.assertEquals(val,
                persistedSource.getAccessParameter(ACCESS_PARAM1).getPropertyValue());
    }

    @Before
    public void setup() {
        Map<String, String> properties = new HashMap<String, String>();
        properties.put("persistenceUnit", PERSISTENCE_UNIT_NAME);

        JPAConfiguration jpaConfiguration = new JPAConfiguration(new SimpleProperties(properties));

        persistence = new SourcePersistence(jpaConfiguration);
    }

    @Test
    public void testCreate() {
        Source source = new Source("connectorType");
        source.addAccessParameter(new Property(ACCESS_PARAM1, VAL1));

        Source persistedSource = persistence.saveSource(source);

        compare(source, persistedSource, VAL1);

        persistedSource = persistence.getSourceByGlobalId(source.getGlobalId());

        source.getAccessParameters().clear();
        source.addAccessParameter(new Property(ACCESS_PARAM1, VAL2));

        persistence.updateSource(source);
        persistedSource = persistence.getSourceByGlobalId(source.getGlobalId());

        compare(source, persistedSource, VAL2);

        persistence.deleteSource(source.getGlobalId());

        persistedSource = persistence.getSourceByGlobalId(source.getGlobalId());

        Assert.assertNull(persistedSource);

    }

    @Test
    public void testFindSource() {
        Source source = new Source("newConnectorType");

        Assert.assertNull(this.persistence.findSource(source.getConnectorType(), null));
        Assert.assertNull(this.persistence.findSource(source.getConnectorType(),
                new ArrayList<Property>()));
        Assert.assertNull(this.persistence.findSource(source.getConnectorType(),
                Arrays.asList(new Property[] { new Property("key", "val") })));

        this.persistence.saveSource(source);

        Source foundSource = this.persistence.findSource(source.getConnectorType(),
                source.getAccessParameters());
        Assert.assertNotNull(foundSource);
        Assert.assertEquals(source.getGlobalId(), foundSource.getGlobalId());

        source = new Source("newConnectorType");
        source.addAccessParameter(new Property("key", "val"));

        this.persistence.saveSource(source);

        foundSource = this.persistence.findSource(source.getConnectorType(),
                source.getAccessParameters());
        Assert.assertNotNull(foundSource);
        Assert.assertEquals(source.getGlobalId(), foundSource.getGlobalId());
        Assert.assertEquals(source.getAccessParameters().size(), foundSource.getAccessParameters()
                .size());
        Assert.assertEquals(source.getAccessParameter("key").getPropertyValue(), foundSource
                .getAccessParameter("key").getPropertyValue());

        source = new Source("newConnectorType");
        source.addAccessParameter(new Property("key", "val"));
        source.addAccessParameter(new Property("key2", "val"));

        foundSource = this.persistence.findSource(source.getConnectorType(),
                source.getAccessParameters());
        Assert.assertNull(foundSource);
    }
}
