/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package de.spektrumprojekt.aggregator.adapter.rss;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.Map;

import javax.net.ssl.SSLException;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.ContentEncodingHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.spektrumprojekt.aggregator.adapter.AccessParameterMissingException;
import de.spektrumprojekt.aggregator.adapter.AccessParameterValidationException;
import de.spektrumprojekt.aggregator.adapter.AdapterException;
import de.spektrumprojekt.aggregator.chain.AggregatorChain;
import de.spektrumprojekt.aggregator.configuration.AggregatorConfiguration;
import de.spektrumprojekt.commons.SpektrumUtils;
import de.spektrumprojekt.commons.encryption.EncryptionException;
import de.spektrumprojekt.commons.encryption.EncryptionUtils;
import de.spektrumprojekt.datamodel.common.Property;
import de.spektrumprojekt.datamodel.source.SourceStatus;
import de.spektrumprojekt.datamodel.subscription.status.StatusType;

/**
 * <p>
 * An adapter handling RSS and Atom feeds.
 * </p>
 * 
 * @author Philipp Katz
 * @author Communote GmbH - <a href="http://www.communote.de/">http://www.communote.com/</a>
 */
public final class FeedAdapter extends XMLAdapter {

    /** The logger for this class. */
    private static final Logger LOGGER = LoggerFactory.getLogger(FeedAdapter.class);

    /** The source type of this adapter. */
    public static final String SOURCE_TYPE = "RSS";

    /**
     * The key for the access parameter specifying the title of the source.
     * 
     * TODO it is not an access parameter, it is a property read by the souce. So we need to mark it
     * somewhow, either give access params a property or add "properties" to the source addtional
     * the access params
     */
    public static final String ACCESS_PARAMETER_TITLE = "title";

    /** The key for the access parameter specifying the feed's URL. */
    public static final String ACCESS_PARAMETER_URI = "feeduri";

    /**
     * The key for the access parameter specifying the login, if authentication is necessary.
     */
    public static final String ACCESS_PARAMETER_CREDENTIALS_LOGIN = "credentials_login";

    /**
     * The key for the access parameter specifying the password, if authentication is necessary.
     */
    public static final String ACCESS_PARAMETER_CREDENTIALS_PASSWORD = "credentials_password";
    /**
     * The key for the access parameter specifying a cleartext password, if authentication is
     * necessary.
     */
    public static final String ACCESS_PARAMETER_CREDENTIALS_CLEARTEXT_PASSWORD = "credentials_cleartext_password";

    /** The key for the id property **/
    public static final String MESSAGE_PROPERTY_ID = "id";

    /** the key in the context to identify the Http Get Object */
    public static final String CONTEXT_HTTTP_GET = "http_get";

    /**
     * Encode the login and password as base 64
     * 
     * @param login
     *            the login
     * @param password
     *            the password
     * @return the credentials string for the base authentication, that is login + ":" + password
     *         and encoded as Base64, using utf8
     * @throws UnsupportedEncodingException
     */
    public static String getBaseAuthenticationCredentials(String login, String password)
            throws UnsupportedEncodingException {
        String auth = login + ":" + password;
        String base64EncodedCredentials = new String(Base64.encodeBase64(auth.getBytes("UTF-8")),
                "UTF-8");
        return base64EncodedCredentials;
    }

    public FeedAdapter(AggregatorChain aggregatorChain,
            AggregatorConfiguration aggregatorConfiguration) {
        super(aggregatorChain, aggregatorConfiguration);

    }

    /**
     * Abort the request due to an error before
     * 
     * @param get
     */
    private void abortRequest(HttpGet get) {
        try {
            get.abort();
        } catch (Exception e) {
            LOGGER.warn("Error in aborting request: " + e.getMessage());
            LOGGER.debug("Error in aborting request: " + e.getMessage(), e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void cleanUpResources(InputStream in, Map<String, Object> context, boolean success) {
        super.cleanUpResources(in, context, success);
        HttpGet get = (HttpGet) context.get(CONTEXT_HTTTP_GET);
        try {
            if (!success && get != null) {
                this.abortRequest(get);
            }
        } catch (Exception e) {
            LOGGER.warn("Error aborting request " + e.getMessage());
            LOGGER.debug("Error aborting request " + e.getMessage(), e);
        }
    }

    private HttpGet createHttpGetRequest(String uri, String base64EncodedCredentials) {
        HttpGet get = new HttpGet(uri);
        if (SpektrumUtils.notNullOrEmpty(base64EncodedCredentials)) {
            get.setHeader("Authorization", "Basic " + base64EncodedCredentials);
        }
        return get;
    }

    @Override
    protected InputStream getInputStream(Map<String, Object> context) throws AdapterException {
        InputStream result = null;
        String uri = "";
        String base64EncodedCredentials = "";
        String login = "";
        String password = "";
        SourceStatus subscriptionStatus = (SourceStatus) context.get(CONTEXT_SOURCE_STATUS);
        Collection<Property> accParams = subscriptionStatus.getSource().getAccessParameters();
        for (Property accessParam : accParams) {
            if (accessParam.getPropertyKey().equals(ACCESS_PARAMETER_URI)) {
                uri = accessParam.getPropertyValue();
            } else if (accessParam.getPropertyKey().equals(ACCESS_PARAMETER_CREDENTIALS_LOGIN)) {
                login = accessParam.getPropertyValue();
            } else if (accessParam.getPropertyKey().equals(ACCESS_PARAMETER_CREDENTIALS_PASSWORD)) {
                try {
                    password = EncryptionUtils.decrypt(accessParam.getPropertyValue(),
                            getAggregatorConfiguration().getEncryptionPassword());
                } catch (EncryptionException e) {
                    throw new AdapterException("Error during password decryption", e,
                            StatusType.ERROR_INTERNAL_ADAPTER);
                }
            }
        }
        if (uri == null) {
            throw new AdapterException("No URI provided!", StatusType.ERROR_INVALID_DATA);
        }
        // only if login + password were supplied
        if (login.length() > 0 && password.length() > 0) {
            try {
                base64EncodedCredentials = getBaseAuthenticationCredentials(login, password);
            } catch (UnsupportedEncodingException e) {
                throw new AdapterException("Unsupported encoding", e,
                        StatusType.ERROR_INTERNAL_ADAPTER);
            }
        }
        HttpGet get = null;
        HttpResponse httpResult = null;
        HttpClient httpClient = null;
        httpClient = new ContentEncodingHttpClient();
        get = createHttpGetRequest(uri, base64EncodedCredentials);
        context.put(CONTEXT_HTTTP_GET, get);
        try {
            httpResult = httpClient.execute(get);
            int statusCode = httpResult.getStatusLine().getStatusCode();
            if (statusCode >= 400) {
                throw new AdapterException("HTTP error code " + statusCode,
                        StatusType.ERROR_NETWORK);
            }
            HttpEntity httpEntity = httpResult.getEntity();
            if (httpEntity != null) {
                result = httpEntity.getContent();
            }
        } catch (ClientProtocolException e) {
            throw new AdapterException("ClientProtocolException " + e.getMessage(), e,
                    StatusType.ERROR_NETWORK);
        } catch (SSLException e) {
            throw new AdapterException("SSLException: " + e.getMessage(), e, StatusType.ERROR_SSL);
        } catch (IOException e) {
            throw new AdapterException("IOException: " + e.getMessage(), e,
                    StatusType.ERROR_NETWORK);
        }
        return result;
    }

    @Override
    public String getSourceType() {
        return SOURCE_TYPE;
    }

    /**
     * Prepare the source access parameters holding credentials for authenticating against the
     * source. If a login and a cleartext password is contained the cleartext password is removed
     * and an encrypted password is added instead. If no login is contained the password parameters
     * are removed.
     * 
     * @param login
     *            the login/username or null
     * @param clearPassword
     *            the cleartext password or null
     * @param password
     *            the encrypted password or null
     * @param accessParameters
     *            the access parameters
     * @throws AccessParameterValidationException
     *             in case the password encryption failed
     */
    private void prepareCredentialAccessParameters(Property login, Property clearPassword,
            Property password, Collection<Property> accessParameters)
            throws AccessParameterValidationException {
        if (login != null) {
            if (clearPassword != null) {
                accessParameters.remove(clearPassword);
                String encryptedPassword;
                try {
                    encryptedPassword = EncryptionUtils.encrypt(clearPassword.getPropertyValue(),
                            getAggregatorConfiguration().getEncryptionPassword());
                } catch (EncryptionException e) {
                    LOGGER.error("Encryption of source access password failed", e);
                    throw new AccessParameterValidationException(
                            ACCESS_PARAMETER_CREDENTIALS_CLEARTEXT_PASSWORD,
                            "Error during password encryption");
                }
                accessParameters.add(new Property(ACCESS_PARAMETER_CREDENTIALS_PASSWORD,
                        encryptedPassword));
            }
        } else {
            if (password != null) {
                accessParameters.remove(password);
            }
            if (clearPassword != null) {
                accessParameters.remove(clearPassword);
            }
        }
    }

    @Override
    public void processAccessParametersBeforeSubscribing(Collection<Property> accessParameters)
            throws AccessParameterValidationException {
        Property uri = null;
        Property login = null;
        Property clearPassword = null;
        Property password = null;
        if (accessParameters != null) {
            for (Property param : accessParameters) {
                if (param.getPropertyKey().equals(ACCESS_PARAMETER_URI)) {
                    uri = param;
                } else if (param.getPropertyKey().equals(ACCESS_PARAMETER_CREDENTIALS_LOGIN)) {
                    login = param;
                } else if (param.getPropertyKey().equals(ACCESS_PARAMETER_CREDENTIALS_PASSWORD)) {
                    password = param;
                } else if (param.getPropertyKey().equals(
                        ACCESS_PARAMETER_CREDENTIALS_CLEARTEXT_PASSWORD)) {
                    clearPassword = param;
                }
            }
        }
        if (uri == null) {
            throw new AccessParameterMissingException(ACCESS_PARAMETER_URI);
        }
        prepareCredentialAccessParameters(login, clearPassword, password, accessParameters);
    }
}