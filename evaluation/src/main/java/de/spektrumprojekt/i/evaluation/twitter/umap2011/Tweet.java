package de.spektrumprojekt.i.evaluation.twitter.umap2011;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

public class Tweet {

    public static final String C_USERNAME = "username";
    public static final String C_CREATION_TIME = "creationTime";
    public static final String TABLE_NAME = "tweets_sample";

    /**
     * 
     <code>
         mysql> show columns from tweets_sample;
            +-----------------------+--------------+------+-----+-------------------+-------+
            | Field                 | Type         | Null | Key | Default           | Extra |
            +-----------------------+--------------+------+-----+-------------------+-------+
            | id                    | bigint(20)   | NO   | PRI | NULL              |       |
            | userId                | bigint(20)   | YES  | MUL | NULL              |       |
            | username              | varchar(255) | NO   | MUL | NULL              |       |
            | content               | varchar(255) | NO   |     | NULL              |       |
            | creationTime          | timestamp    | YES  |     | NULL              |       |
            | favorite              | tinyint(1)   | NO   |     | 0                 |       |
            | replyToPostId         | bigint(20)   | YES  |     | NULL              |       |
            | replyToUsername       | varchar(255) | YES  |     | NULL              |       |
            | retweetedFromPostId   | bigint(20)   | YES  |     | NULL              |       |
            | retweetedFromUsername | varchar(255) | YES  |     | NULL              |       |
            | retweetCount          | int(11)      | YES  |     | NULL              |       |
            | latitude              | double       | YES  |     | NULL              |       |
            | longitude             | double       | YES  |     | NULL              |       |
            | placeCountry          | varchar(100) | YES  |     | NULL              |       |
            | placeCountryCode      | varchar(20)  | YES  |     | NULL              |       |
            | placeStreetAddress    | varchar(255) | YES  |     | NULL              |       |
            | placeURL              | varchar(255) | YES  |     | NULL              |       |
            | placeGeometryType     | varchar(255) | YES  |     | NULL              |       |
            | placeName             | varchar(255) | YES  |     | NULL              |       |
            | placeFullName         | varchar(255) | YES  |     | NULL              |       |
            | placeId               | varchar(255) | YES  |     | NULL              |       |
            | source                | varchar(255) | YES  |     | NULL              |       |
            | json                  | text         | YES  |     | NULL              |       |
            | timeOfCrawl           | timestamp    | YES  |     | CURRENT_TIMESTAMP |       |
            | crawledViaNewsMedia   | tinyint(1)   | YES  | MUL | 0                 |       |
            +-----------------------+--------------+------+-----+-------------------+-------+
        </code>
     */
    public static final String[] COLUMNS = new String[] {

            "id",
            "userId",
            C_USERNAME,
            "content",
            C_CREATION_TIME,
            "favorite",
            "replyToPostId",
            "replyToUsername",
            "retweetedFromPostId",
            "retweetedFromUsername",
            "retweetCount"

    };

    private Long id;
    private Long userId;
    private String username;
    private String content;
    private Date creationTime;
    private boolean favorite;
    private Long replyToPostId;
    private String replyToUsername;
    private Long retweetedFromPostId;
    private String retweetedFromUsername;
    private long retweetCount;

    public Tweet(ResultSet result) throws SQLException {
        int colIndex = 1;

        id = result.getLong(colIndex++);
        userId = result.getLong(colIndex++);
        username = result.getString(colIndex++);
        content = result.getString(colIndex++);
        creationTime = result.getDate(colIndex++);
        favorite = result.getBoolean(colIndex++);
        replyToPostId = result.getLong(colIndex++);
        replyToUsername = result.getString(colIndex++);
        retweetedFromPostId = result.getLong(colIndex++);
        retweetedFromUsername = result.getString(colIndex++);
        retweetCount = result.getLong(colIndex++);
    }

    public String getContent() {
        return content;
    }

    public Date getCreationTime() {
        return creationTime;
    }

    public Long getId() {
        return id;
    }

    public Long getReplyToPostId() {
        return replyToPostId;
    }

    public String getReplyToUsername() {
        return replyToUsername;
    }

    public long getRetweetCount() {
        return retweetCount;
    }

    public Long getRetweetedFromPostId() {
        return retweetedFromPostId;
    }

    public String getRetweetedFromUsername() {
        return retweetedFromUsername;
    }

    public Long getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public boolean isFavorite() {
        return favorite;
    }

    @Override
    public String toString() {
        return "Tweet [id=" + id + ", userId=" + userId + ", username=" + username + ", content="
                + content + ", creationTime=" + creationTime + ", favorite=" + favorite
                + ", replyToPostId=" + replyToPostId + ", replyToUsername=" + replyToUsername
                + ", retweetedFromPostId=" + retweetedFromPostId + ", retweetedFromUsername="
                + retweetedFromUsername + ", retweetCount=" + retweetCount + "]";
    }
}