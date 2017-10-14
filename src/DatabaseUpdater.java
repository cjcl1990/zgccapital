import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class DatabaseUpdater
{
    private static final Lock lock = new ReentrantLock();

    public static void doUpdate(ArrayList<Review> reviews, String itemID, String itemInfo)
            throws SQLException, ClassNotFoundException, IOException
    {
        lock.lock();
        try {
            Class.forName("com.mysql.jdbc.Driver");

            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/2luv_coco", "root", "root");

            PreparedStatement insertreviewinfo = conn
                    .prepareStatement("insert into amz_reviews (created_at, updated_at,reviewDate,  rating, title, author, link, reviewID, itemID,content,size,color) values (?, ?,?, ?, ?, ?, ?, ?, ?,?,?,?);");

            for (Review areview : reviews)
            {
                DateFormat dateFormat = new SimpleDateFormat(
                        "yyyy/MM/dd HH:mm:ss");
                Date date = new Date();
                String nowtime = dateFormat.format(date);
                insertreviewinfo.setString(1, nowtime);
                insertreviewinfo.setString(2, nowtime);
                DateFormat dateFormat2 = new SimpleDateFormat("yyyyMM");
                String reviewdatestring = dateFormat2
                        .format(areview.reviewDate);
                insertreviewinfo.setString(3, reviewdatestring);

                insertreviewinfo.setInt(4, (int)areview.rating);
                insertreviewinfo.setString(5, areview.title);
                insertreviewinfo.setString(6, areview.author);
                insertreviewinfo.setString(7, areview.link);
                insertreviewinfo.setString(8, areview.reviewID);
                insertreviewinfo.setString(9, areview.itemID);
                insertreviewinfo.setString(10, areview.content);
                insertreviewinfo.setString(11, areview.size);
                insertreviewinfo.setString(12, areview.color);
                insertreviewinfo.addBatch();
            }
            conn.setAutoCommit(false);

            insertreviewinfo.executeBatch();

            conn.commit();
            conn.close();
        } finally {
            lock.unlock();
        }
    }
}