package de.spektrumprojekt.aggregator.adapter.rss;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Map;

import de.spektrumprojekt.aggregator.adapter.AdapterException;
import de.spektrumprojekt.aggregator.chain.AggregatorChain;
import de.spektrumprojekt.aggregator.configuration.AggregatorConfiguration;
import de.spektrumprojekt.datamodel.source.SourceStatus;
import de.spektrumprojekt.datamodel.subscription.status.StatusType;

public class FileAdapter extends XMLAdapter {

    /** The key for the access parameter specifying the files path. */
    public static final String ACCESS_PARAMETER_PATH = "path";

    /** The source type of this adapter. */
    public static final String SOURCE_TYPE = "XML_FILE";

    public FileAdapter(AggregatorChain aggregatorChain,
            AggregatorConfiguration aggregatorConfiguration) {
        super(aggregatorChain, aggregatorConfiguration);
        // TODO Auto-generated constructor stub
    }

    @Override
    protected InputStream getInputStream(Map<String, Object> context) throws AdapterException {
        SourceStatus sourceStatus = (SourceStatus) context.get(XMLAdapter.CONTEXT_SOURCE_STATUS);
        String path = sourceStatus.getSource().getAccessParameter(ACCESS_PARAMETER_PATH)
                .getPropertyValue();
        if (path == null || path == "") {
            throw new AdapterException("No path to file provided.", StatusType.ERROR_INVALID_DATA);
        }
        try {
            return new FileInputStream(path);
        } catch (FileNotFoundException e) {
            throw new AdapterException("FileNotFoundException " + e.getMessage(), e,
                    StatusType.ERROR_UNSPECIFIED);
        }
    }

    @Override
    public String getSourceType() {
        return SOURCE_TYPE;
    }

}
