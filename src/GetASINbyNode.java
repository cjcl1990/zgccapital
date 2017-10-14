import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.joda.time.DateTime;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GetASINbyNode
        implements GetID
{
    private ItemList items;
    private String nodeid;
    private int from;
    private int to;

    public GetASINbyNode(String thenode, int fromPage, int toPage)
    {
        this.nodeid = thenode;
        this.from = fromPage;
        this.to = toPage;
        this.items = new ItemList();
    }

    public void updateCSV(String oldfile) throws MalformedURLException, IOException {
        ItemList oldlist = new ItemList(oldfile);
        oldlist.mergeList(this.items);
        oldlist.writeToCSV(oldfile);
    }

    public void getIDList() throws IOException {
        for (int i = this.from; i <= this.to; i++) {
            String thepage = readWebPage("http://www.amazon.com/gp/aw/s/ref=is_pg_2_1?n=" + this.nodeid + "&p=" + i + "&p_72=1248882011&s=salesrank");

            DateTime dt = new DateTime();
            System.out.println(dt + "Page " + i);
            String patternString = "(<a href=\"/gp/aw/d/)(\\S{10})(/ref=mp_s_a)";
            Pattern pattern = Pattern.compile(patternString);
            Matcher matcher = pattern.matcher(thepage);
            while (matcher.find())
                this.items.addItem(matcher.group(2));
        }
    }

    public void writeIDsToCSV(String filePath)
            throws IOException
    {
        this.items.writeToCSV(filePath);
    }

    public String readWebPage(String weburl)
            throws IOException
    {
        HttpClient httpclient = new DefaultHttpClient();

        HttpGet httpget = new HttpGet(weburl);
        ResponseHandler responseHandler = new BasicResponseHandler();
        String responseBody = (String)httpclient.execute(httpget, responseHandler);

        httpclient.getConnectionManager().shutdown();
        return responseBody;
    }
}