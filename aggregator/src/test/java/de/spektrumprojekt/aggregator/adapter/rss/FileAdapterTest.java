package de.spektrumprojekt.aggregator.adapter.rss;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.commons.configuration.ConfigurationException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.spektrumprojekt.aggregator.Aggregator;
import de.spektrumprojekt.aggregator.TestHelper;
import de.spektrumprojekt.aggregator.adapter.AdapterException;
import de.spektrumprojekt.aggregator.chain.AggregatorChain;
import de.spektrumprojekt.aggregator.configuration.AggregatorConfiguration;
import de.spektrumprojekt.communication.CommunicationMessage;
import de.spektrumprojekt.communication.Communicator;
import de.spektrumprojekt.communication.vm.VirtualMachineCommunicator;
import de.spektrumprojekt.configuration.properties.SimpleProperties;
import de.spektrumprojekt.datamodel.common.Property;
import de.spektrumprojekt.datamodel.message.Message;
import de.spektrumprojekt.datamodel.source.Source;
import de.spektrumprojekt.datamodel.source.SourceStatus;
import de.spektrumprojekt.datamodel.subscription.status.StatusType;
import de.spektrumprojekt.persistence.Persistence;
import de.spektrumprojekt.persistence.jpa.JPAPersistence;
import de.spektrumprojekt.persistence.simple.PersistenceMock;

public class FileAdapterTest {

    private static final String PERSISTENCE_UNIT_NAME = "de.spektrumprojekt.datamodel.test";
    /** The logger for this class. */
    private static final Logger LOGGER = LoggerFactory.getLogger(FileAdapterTest.class);

    private Communicator communicator;
    private final Persistence persistence = new PersistenceMock();

    private Aggregator aggregator;
    private AggregatorConfiguration aggregatorConfiguration;
    private AggregatorChain aggregatorChain;

    private SourceStatus createSourceStatus(String filePath) {
        Source source = new Source(FileAdapter.SOURCE_TYPE);
        SourceStatus sourceStatus = new SourceStatus(source);
        sourceStatus.getSource().addAccessParameter(
                new Property(FileAdapter.ACCESS_PARAMETER_PATH, TestHelper
                        .getTestFilePath(filePath)));
        return sourceStatus;
    }

    private SourceStatus load(SourceStatus sourceStatus) {
        return aggregatorChain.getPersistence().getSourceStatusBySourceGlobalId(
                sourceStatus.getSource().getGlobalId());
    }

    private void persist(SourceStatus sourceStatus) {
        aggregatorChain.getPersistence().saveSourceStatus(sourceStatus);
    }

    @Before
    public void setup() throws ConfigurationException {
        ConcurrentLinkedQueue<CommunicationMessage> queue = new ConcurrentLinkedQueue<CommunicationMessage>();
        communicator = new VirtualMachineCommunicator(queue, queue);

        Map<String, String> properties = new HashMap<String, String>();
        properties.put("persistenceUnit", PERSISTENCE_UNIT_NAME);

        JPAPersistence persistence = new JPAPersistence(new SimpleProperties(properties));
        persistence.initialize();

        aggregatorConfiguration = AggregatorConfiguration.loadXmlConfig();
        Assert.assertNotNull(aggregatorConfiguration);

        aggregator = new Aggregator(communicator, persistence, aggregatorConfiguration);

        aggregatorChain = aggregator.getAggregatorChain();

        communicator.open();
    }

    private List<Message> testFile(SourceStatus sourceStatus) throws AdapterException {
        FileAdapter feedAdapter = new FileAdapter(aggregatorChain, aggregatorConfiguration);
        return feedAdapter.poll(sourceStatus);

    }

    private List<Message> testFileAndCreateSource(String filePath) throws AdapterException {
        return testFile(createSourceStatus(filePath));
    }

    // @Test
    public void testFileFail() {
        AdapterException exception = null;
        try {
            testFileAndCreateSource(TestHelper.FILE_NAME_INVALID_XML);
        } catch (AdapterException e) {
            exception = e;
        }
        Assert.assertNotNull(exception);
        Assert.assertEquals(StatusType.ERROR_PROCESSING_CONTENT, exception.getStatusType());
    }

    // @Test
    public void testFilePass() {
        AdapterException exception = null;
        List<Message> messages = new LinkedList<Message>();
        try {
            messages.addAll(testFileAndCreateSource(TestHelper.FILE_NAME_VALID_XML));
        } catch (AdapterException e) {
            exception = e;
        }
        Assert.assertNull(exception);
        Assert.assertEquals(60, messages.size());
    }

    @Test
    public void testProperties() {
        SourceStatus sourceStatus = createSourceStatus(TestHelper.FILE_NAME_NO_DC);
        testPropertiesNull(sourceStatus);
        persist(sourceStatus);
        sourceStatus = load(sourceStatus);
        try {
            testFile(sourceStatus);
        } catch (AdapterException e) {
            e.printStackTrace();
        }
        sourceStatus = load(sourceStatus);
        testPropertiesFilledNoDC(sourceStatus);
        Property path = sourceStatus.getSource().getAccessParameter(
                FileAdapter.ACCESS_PARAMETER_PATH);
        sourceStatus.getSource().getAccessParameters().remove(path);
        sourceStatus.getSource().addAccessParameter(
                new Property(FileAdapter.ACCESS_PARAMETER_PATH, TestHelper
                        .getTestFilePath(TestHelper.FILE_NAME_DC_ONLY)));
        persist(sourceStatus);
        try {
            testFile(sourceStatus);
        } catch (AdapterException e) {
            e.printStackTrace();
        }
        sourceStatus = load(sourceStatus);
        testPropertiesFilledDC(sourceStatus);
    }

    private void testPropertiesFilledDC(SourceStatus sourceStatus) {
        Assert.assertNotNull(sourceStatus.getProperty(Property.SOURCE_PROPERTY_KEY_TITLE));
        Assert.assertNotNull(sourceStatus.getProperty(Property.SOURCE_PROPERTY_KEY_DESCRIPTION));

        // The Framework seems not to support the dc rights TODO change if support is added
        Assert.assertNull(sourceStatus.getProperty(Property.SOURCE_PROPERTY_KEY_COPYRIGHT));
        // Assert.assertEquals("copyright456",
        // sourceStatus.getProperty(XMLAdapter.SOURCE_PROPERTY_KEY_COPYRIGHT)
        // .getPropertyValue());

        Assert.assertEquals("title456",
                sourceStatus.getProperty(Property.SOURCE_PROPERTY_KEY_TITLE).getPropertyValue());
        Assert.assertEquals("description456",
                sourceStatus.getProperty(Property.SOURCE_PROPERTY_KEY_DESCRIPTION)
                        .getPropertyValue());
    }

    private void testPropertiesFilledNoDC(SourceStatus sourceStatus) {
        testPropertiesNotNull(sourceStatus);
        Assert.assertEquals("copyright123",
                sourceStatus.getProperty(Property.SOURCE_PROPERTY_KEY_COPYRIGHT)
                        .getPropertyValue());
        Assert.assertEquals("title123",
                sourceStatus.getProperty(Property.SOURCE_PROPERTY_KEY_TITLE).getPropertyValue());
        Assert.assertEquals("description123",
                sourceStatus.getProperty(Property.SOURCE_PROPERTY_KEY_DESCRIPTION)
                        .getPropertyValue());
    }

    private void testPropertiesNotNull(SourceStatus sourceStatus) {
        Assert.assertNotNull(sourceStatus.getProperty(Property.SOURCE_PROPERTY_KEY_COPYRIGHT));
        Assert.assertNotNull(sourceStatus.getProperty(Property.SOURCE_PROPERTY_KEY_TITLE));
        Assert.assertNotNull(sourceStatus.getProperty(Property.SOURCE_PROPERTY_KEY_DESCRIPTION));
    }

    private void testPropertiesNull(SourceStatus sourceStatus) {
        Assert.assertNull(sourceStatus.getProperty(Property.SOURCE_PROPERTY_KEY_COPYRIGHT));
        Assert.assertNull(sourceStatus.getProperty(Property.SOURCE_PROPERTY_KEY_TITLE));
        Assert.assertNull(sourceStatus.getProperty(Property.SOURCE_PROPERTY_KEY_DESCRIPTION));
    }
}
