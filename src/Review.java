import org.apache.commons.lang.builder.ReflectionToStringBuilder;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Review
{
    String itemID;
    String reviewID;
    String author;
    String title;
    String link;
    double rating;
    Date reviewDate;
    String content;
    String size;
    String color;

    public Review(String aitemID, String areviewID, String aauthor, String atitle, String alink, int arating, Date aReviewDate, String acontent, String acolor, String asize)
    {
        this.itemID = aitemID;
        this.reviewID = areviewID;
        this.author = aauthor;
        this.title = atitle;
        this.link = alink;
        this.rating = arating;
        this.reviewDate = aReviewDate;
        this.content = acontent;
        this.color = acolor;
        this.size = asize;
    }

    public void writeDatabase(String database)
            throws ClassNotFoundException, SQLException
    {
        Class.forName("org.sqlite.JDBC");
        Connection conn =
                DriverManager.getConnection("jdbc:sqlite:" + database);
        PreparedStatement insertreview = conn
                .prepareStatement("insert into review (reviewid, title, content) values (?, ?, ?);");
        PreparedStatement insertreviewinfo = conn
                .prepareStatement("insert into amz_reviews (created_at, reviewDate, rating, title, author, reviewID, itemID) values (?, ?, ?, ?, ?, ?, ?);");

        insertreview.setString(1, this.reviewID);
        insertreview.setString(2, this.title);
        insertreview.setString(3, this.content);
        insertreview.addBatch();
        insertreview.executeBatch();
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = new Date();
        String nowtime = dateFormat.format(date);
        insertreviewinfo.setString(1, nowtime);
        DateFormat dateFormat2 = new SimpleDateFormat("yyyy/MM/dd");
        String reviewdatestring = dateFormat2.format(this.reviewDate);
        insertreviewinfo.setString(2, reviewdatestring);
        insertreviewinfo.setInt(3, (int)this.rating);
        insertreviewinfo.setString(4, this.title);
        insertreviewinfo.setString(5, this.author);
        insertreviewinfo.setString(6, this.reviewID);
        insertreviewinfo.setString(7, this.itemID);
        insertreviewinfo.addBatch();
        insertreviewinfo.executeBatch();
        conn.close();
    }

    public String toString()
    {
        return ReflectionToStringBuilder.toString(this);
    }

    public void updateReview(String aitemid, String areviewid, String acustomername, String atitle, double arating, Date areviewDate, String acontent, String acolor, String asize)
    {
        this.itemID = aitemid;
        this.reviewID = areviewid;
        this.author = acustomername;
        this.title = atitle;
        this.rating = arating;
        this.reviewDate = areviewDate;
        this.content = acontent;
        this.size = asize;
        this.color = acolor;
    }
}