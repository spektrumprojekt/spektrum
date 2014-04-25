package de.spektrumprojekt.i.evaluation.twitter.umap2011;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.spektrumprojekt.i.evaluation.configuration.Configuration;

public class SqlUmap2011TweetAccess {

    private final static Logger LOGGER = LoggerFactory.getLogger(SqlUmap2011TweetAccess.class);

    public final static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-mm-dd");

    public static SqlUmap2011TweetAccess getDefaultInstance() throws SQLException {
        try {
            // The newInstance() call is a work around for some
            // broken Java implementations

            Class.forName("com.mysql.jdbc.Driver").newInstance();
        } catch (Exception ex) {
            throw new RuntimeException(
                    "Cannot find mysql Driver! Included Jar? " + ex.getMessage(), ex);
        }

        Connection connection = null;
        SqlUmap2011TweetAccess tweetAccess = null;
        try {
            String url = Configuration.INSTANCE.getTwitterUmapJdbcUrl();
            LOGGER.info("Using url=" + url);
            connection =
                    DriverManager.getConnection(url);

            tweetAccess = new SqlUmap2011TweetAccess(connection);

        } catch (SQLException ex) {
            LOGGER.error("SQLException: " + ex.getMessage(), ex);
            throw ex;
        }

        return tweetAccess;
    }

    public static void main(String[] args) throws Exception {

        SqlUmap2011TweetAccess tweetAccess = null;
        try {

            tweetAccess = getDefaultInstance();

            TweetFilter tweetFilter = new TweetFilter();

            tweetFilter.minDate = DATE_FORMAT.parse("2010-12-12");
            long count = tweetAccess.getTweetCount(tweetFilter);

            // List<Tweet> tweets = tweetAccess.readTweets(tweetFilter);

            System.out.println("Count: " + count);

        } catch (SQLException ex) {

            // handle any errors
            LOGGER.error("SQLException: " + ex.getMessage());
            LOGGER.error("SQLState: " + ex.getSQLState());
            LOGGER.error("VendorError: " + ex.getErrorCode());

            LOGGER.error("Error", ex);

        } finally {
            if (tweetAccess != null) {
                tweetAccess.close();
            }
        }

    }

    private final Connection connection;

    private final static String AND = " AND ";

    public SqlUmap2011TweetAccess(Connection connection) {
        if (connection == null) {
            throw new IllegalArgumentException("connection cannot be null.");
        }
        try {
            if (connection.isClosed()) {
                throw new IllegalStateException("connection cannot be closed.");
            }
        } catch (SQLException e) {
            throw new IllegalStateException("connection close check error.", e);
        }

        this.connection = connection;

    }

    public void close() throws SQLException {
        if (this.connection != null) {
            this.connection.close();
        }
    }

    public long getTweetCount(TweetFilter tweetFilter) throws SQLException {
        return (Long) readTweets(tweetFilter, true);
    }

    @SuppressWarnings("unchecked")
    public List<Tweet> readTweets(TweetFilter tweetFilter) throws SQLException {
        return (List<Tweet>) readTweets(tweetFilter, false);
    }

    private Object readTweets(TweetFilter tweetFilter, boolean count) throws SQLException {

        StringBuilder sb = new StringBuilder();

        sb.append("select ");
        if (count) {
            sb.append(" count(*) ");
        } else {
            sb.append(org.apache.commons.lang3.StringUtils.join(Tweet.COLUMNS, ", "));
        }
        sb.append(" from ");
        sb.append(Tweet.TABLE_NAME);

        // sb.append(" where id = 22043018324348929");

        String prefix = " where ";
        if (tweetFilter.minDate != null) {
            sb.append(prefix + Tweet.C_CREATION_TIME + " >= ?");
            prefix = AND;
        }
        if (tweetFilter.maxDate != null) {
            sb.append(prefix + Tweet.C_CREATION_TIME + " <= ?");
            prefix = AND;
        }
        if (tweetFilter.usernames != null && tweetFilter.usernames.length > 0) {
            sb.append(prefix + Tweet.C_USERNAME + " in (");
            String s = " ? ";
            for (int i = 0; i < tweetFilter.usernames.length; i++) {
                sb.append(s);
                s = ", ? ";
            }
            sb.append(")");
            prefix = AND;
        }

        java.sql.PreparedStatement statement = null;
        try {
            // LOGGER.debug("Executing query " + sb);
            System.out.println("Executing query " + sb + " tweetFilter: " + tweetFilter);
            statement = connection.prepareStatement(sb.toString());

            int p = 1;
            if (tweetFilter.minDate != null) {
                statement.setDate(p++, new java.sql.Date(tweetFilter.minDate.getTime()));
            }
            if (tweetFilter.maxDate != null) {
                statement.setDate(p++, new java.sql.Date(tweetFilter.maxDate.getTime()));
            }
            if (tweetFilter.usernames != null) {
                for (int i = 0; i < tweetFilter.usernames.length; i++) {
                    statement.setString(p++, tweetFilter.usernames[i]);
                }
            }

            ResultSet resultSet = statement.executeQuery();

            if (count) {
                resultSet.next();
                return resultSet.getLong(1);
            } else {
                List<Tweet> tweets = new ArrayList<Tweet>();

                while (resultSet.next()) {
                    Tweet tweet = new Tweet(resultSet);
                    tweets.add(tweet);

                }
                return tweets;
            }
        } finally {
            if (statement != null) {
                statement.close();
            }
        }

    }
}