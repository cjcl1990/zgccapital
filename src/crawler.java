import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.text.ParseException;

public class crawler
{
    public static void main(String[] args)
            throws IOException, ParseException, ClassNotFoundException, SQLException, InvalidKeyException, NoSuchAlgorithmException, InterruptedException
    {
        Item an_item = new Item(args[0]);
        an_item.fetchReview();
        an_item.writeReviewsToDatabase(false);
    }
}