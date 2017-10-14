import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

public class ItemList
{
    private Set<String> itemIDs;

    public ItemList()
    {
        this.itemIDs = new HashSet();
    }

    public ItemList(String file)
            throws FileNotFoundException
    {
        this.itemIDs = new HashSet();
        Scanner s = new Scanner(new File(file));
        while (s.hasNext()) {
            this.itemIDs.add(s.next());
        }
        s.close();
    }

    public String toString() {
        return this.itemIDs.toString();
    }

    public void addItem(String newItem) {
        this.itemIDs.add(newItem);
    }

    public void writeReviewsToDatabase(boolean API)
            throws IOException, InvalidKeyException, ClassNotFoundException, NoSuchAlgorithmException, ParseException, SQLException
    {
        for (String id : this.itemIDs) {
            Item item = new Item(id);
            item.fetchReview();
            item.writeReviewsToDatabase(API);
        }
    }

    public void mergeList(ItemList anotherList)
    {
        this.itemIDs.addAll(anotherList.returnIDsAsSet());
    }

    public ArrayList<ItemList> divide(int npartition)
    {
        return null;
    }

    public boolean writeToCSV(String filePath)
            throws IOException
    {
        FileUtils.writeLines(new File(filePath), this.itemIDs);
        return true;
    }

    public Set<String> returnIDsAsSet() {
        return this.itemIDs;
    }
}