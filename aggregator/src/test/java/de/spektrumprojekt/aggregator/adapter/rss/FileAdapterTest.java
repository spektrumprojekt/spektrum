package de.spektrumprojekt.aggregator.adapter.rss;

import static org.junit.Assume.assumeNotNull;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

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
import de.spektrumprojekt.datamodel.common.Property;
import de.spektrumprojekt.datamodel.message.Message;
import de.spektrumprojekt.datamodel.source.Source;
import de.spektrumprojekt.datamodel.source.SourceStatus;
import de.spektrumprojekt.datamodel.subscription.status.StatusType;
import de.spektrumprojekt.persistence.Persistence;
import de.spektrumprojekt.persistence.simple.PersistenceMock;

public class FileAdapterTest {
    /** The logger for this class. */
    private static final Logger LOGGER = LoggerFactory.getLogger(FileAdapterTest.class);

    private Communicator communicator;
    private final Persistence persistence = new PersistenceMock();

    private Aggregator aggregator;
    private AggregatorConfiguration aggregatorConfiguration;
    private AggregatorChain aggregatorChain;

    @Before
    public void readConfig() throws ConfigurationException {
        aggregatorConfiguration = AggregatorConfiguration.loadXmlConfig();
        assumeNotNull(aggregatorConfiguration);

        communicator = new VirtualMachineCommunicator(
                new LinkedBlockingQueue<CommunicationMessage>(),
                new LinkedBlockingQueue<CommunicationMessage>());

        aggregator = new Aggregator(communicator, persistence, aggregatorConfiguration);

        aggregatorChain = aggregator.getAggregatorChain();
    }

    private List<Message> testFile(String filename) throws AdapterException {
        Source source = new Source(FileAdapter.SOURCE_TYPE);
        SourceStatus sourceStatus = new SourceStatus(source);
        source.addAccessParameter(new Property(FileAdapter.ACCESS_PARAMETER_PATH, TestHelper
                .getTestFilePath(filename)));
        FileAdapter feedAdapter = new FileAdapter(aggregatorChain, aggregatorConfiguration);
        return feedAdapter.poll(sourceStatus);
    }

    @Test
    public void testFileFail() {
        AdapterException exception = null;
        try {
            testFile(TestHelper.FILE_NAME_INVALID_XML);
        } catch (AdapterException e) {
            exception = e;
        }
        Assert.assertNotNull(exception);
        Assert.assertEquals(StatusType.ERROR_PROCESSING_CONTENT, exception.getStatusType());
    }

    @Test
    public void testFilePass() {
        AdapterException exception = null;
        List<Message> messages = new LinkedList<Message>();
        try {
            messages.addAll(testFile(TestHelper.FILE_NAME_VALID_XML));
        } catch (AdapterException e) {
            exception = e;
        }
        Assert.assertNull(exception);
        Assert.assertEquals(60, messages.size());
    }
}
