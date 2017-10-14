import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class Item
{
    public String itemID;
    public ArrayList<Review> reviews;

    public Item(String theitemid)
    {
        this.itemID = theitemid;
        this.reviews = new ArrayList();
    }

    public void addReview(Review thereview) {
        this.reviews.add(thereview);
    }

    public org.jsoup.nodes.Document jsoup_load_with_retry(String url)
            throws IOException
    {
        int max_retry = 10;
        int retry = 1;
        int sleep_sec = 2;
        org.jsoup.nodes.Document content = null;

        while (retry <= max_retry)
            try {
                content = Jsoup.connect(url).timeout(10000).get();
            }
            catch (Exception ex)
            {
                System.out.println(ex.getMessage() + " retrying..");
                try {
                    TimeUnit.SECONDS.sleep(sleep_sec);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                retry++;
            }
        return content;
    }

    public void fetchReview()
    {
        String url = "http://www.amazon.com/product-reviews/" + this.itemID +
                "/?showViewpoints=0&sortBy=byRankDescending&pageNumber=" + 1;
        try
        {
            org.jsoup.nodes.Document reviewpage1 = null;

            reviewpage1 = jsoup_load_with_retry(url);
            int maxpage = 1;
            Elements pagelinks = reviewpage1.select("a[href*=pageNumber=]");
            if (pagelinks.size() != 0) {
                ArrayList pagenum = new ArrayList();
                for (Element link : pagelinks)
                    try {
                        pagenum.add(Integer.valueOf(Integer.parseInt(link.text())));
                    }
                    catch (NumberFormatException localNumberFormatException) {
                    }
                maxpage = ((Integer)Collections.max(pagenum)).intValue();
            }

            for (int p = 1; p <= maxpage; p++) {
                url = "http://www.amazon.com/product-reviews/" +
                        this.itemID +
                        "/?sortBy=helpful&pageNumber=" +
                        p;
                org.jsoup.nodes.Document reviewpage = null;

                reviewpage = jsoup_load_with_retry(url);
                if (reviewpage.select("div.a-section.review").isEmpty()) {
                    System.out.println(this.itemID + " " + "no reivew");
                } else {
                    Elements reviewsHTMLs = reviewpage.select(
                            "div.a-section.review");
                    for (Element reviewBlock : reviewsHTMLs) {
                        Review theReview = cleanReviewBlock(reviewBlock);
                        addReview(theReview);
                    }
                }
            }

        }
        catch (Exception e)
        {
            System.out.println(this.itemID + " " + "Exception" + " " + e.toString());
        }
    }

    public Review cleanReviewBlock(Element reviewBlock)
            throws ParseException
    {
        String theitemID = this.itemID;
        String reviewID = "";
        String title = "";
        String author = "";
        int rating = 0;
        String link = "";
        String color = "";
        String size = "";
        Date reviewDate = new SimpleDateFormat("MMMM d, yyyy", Locale.ENGLISH)
                .parse("January 1, 1900");
        String content = "";

        reviewID = reviewBlock.id();
        try
        {
            Element reviewAuthor = reviewBlock.select(".review-byline a").first();
            author = reviewAuthor.text();

            link = reviewBlock.select("a").first().attr("abs:href");

            Element reviewTitle = reviewBlock.select("a.review-title").first();
            title = reviewTitle.text();

            Element star = reviewBlock.select("i.a-icon-star").first();
            String starinfo = star.text();
            rating = Integer.parseInt(starinfo.substring(0, 1));

            Elements date = reviewBlock.select("span.review-date");

            String datetext = date.first().text();
            datetext = datetext.substring(3);
            reviewDate = new SimpleDateFormat("MMMM d, yyyy", Locale.ENGLISH)
                    .parse(datetext);

            Element contentDoc = reviewBlock.select("span.review-text").first();
            content = contentDoc.text();

            Element color_size = reviewBlock.select(".review-format-strip").first();
            String all = color_size.text().replace(" ", "").replace("|", ",");
            color = all.split(",")[1].split(":")[1];

            size = all.split(",")[0].split(":")[1];
        }
        catch (Exception e)
        {
            color = "not found";
            size = "not found";
            System.out.println(reviewID + " " + "Exception" + " " + e.toString());
        }
        Review thereview = new Review(theitemID, reviewID, author, title,
                link, rating, reviewDate, content, color, size);
        return thereview;
    }

    public synchronized void writeReviewsToDatabase(boolean API)
            throws InvalidKeyException, ClassNotFoundException, NoSuchAlgorithmException, ClientProtocolException, SQLException, IOException
    {
        if (API)
            DatabaseUpdater.doUpdate(this.reviews, this.itemID,
                    getXMLLargeResponse());
        else {
            DatabaseUpdater.doUpdate(this.reviews, this.itemID, "");
        }
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = new Date();
        String timenow = dateFormat.format(date);
        System.out.println(this.itemID + " Finished " + timenow);
    }

    public String getXMLLargeResponse()
            throws InvalidKeyException, NoSuchAlgorithmException, ClientProtocolException, IOException
    {
        String responseBody = "";
        String signedurl = signInput();
        try {
            HttpClient httpclient = new DefaultHttpClient();
            HttpGet httpget = new HttpGet(signedurl);
            ResponseHandler responseHandler = new BasicResponseHandler();
            responseBody = (String)httpclient.execute(httpget, responseHandler);

            httpclient.getConnectionManager().shutdown();
        } catch (Exception e) {
            System.out.println("Exception " + this.itemID + " " + e.getClass());
        }
        return responseBody;
    }

    private String signInput()
            throws InvalidKeyException, NoSuchAlgorithmException, UnsupportedEncodingException
    {
        Map variablemap = new HashMap();

        variablemap.put("AssociateTag", "");
        variablemap.put("Operation", "ItemLookup");
        variablemap.put("Service", "AWSECommerceService");
        variablemap.put("ItemId", this.itemID);
        variablemap.put("ResponseGroup", "Large");

        SignedRequestsHelper helper = new SignedRequestsHelper();
        String signedurl = helper.sign(variablemap);
        return signedurl;
    }

    public void getBookSaleInfo()
            throws InvalidKeyException, NoSuchAlgorithmException, UnsupportedEncodingException
    {
        String signedurl = signInput();
        System.out.println(signedurl);

        ArrayList TagNames = new ArrayList();
        TagNames.add("Title");
        TagNames.add("SalesRank");
        TagNames.add("ListPrice");
        TagNames.add("LowestNewPrice");
        TagNames.add("LowestUsedPrice");
        TagNames.add("TotalNew");
        TagNames.add("TotalUsed");
        TagNames.add("PublicationDate");
        TagNames.add("Author");
        TagNames.add("Publisher");
        TagNames.add("EditorialReview");

        Map InfoTagMap = fetchInfo(signedurl, TagNames);
        System.out.println(InfoTagMap.toString());
    }

    private static Map<String, String> fetchInfo(String requestUrl, ArrayList<String> TagNames)
    {
        Map InfoTagMap = new HashMap();
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            org.w3c.dom.Document doc = db.parse(requestUrl);

            if (doc.getElementsByTagName("IsValid").item(0).getTextContent()
                    .equals("True"))
                for (String tag : TagNames) {
                    NodeList titleNode = doc.getElementsByTagName(tag);
                    if (tag.equals("Title")) {
                        InfoTagMap.put(tag, titleNode.item(0).getTextContent());
                    } else {
                        ArrayList infolist = new ArrayList();
                        for (int i = 0; i < titleNode.getLength(); i++) {
                            infolist.add(titleNode.item(i).getTextContent());
                        }
                        InfoTagMap.put(tag, infolist.toString());
                    }
                }
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
        return InfoTagMap;
    }
}