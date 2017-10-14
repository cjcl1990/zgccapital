import org.apache.commons.codec.binary.Base64;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class SignedRequestsHelper
{
    private static final String UTF8_CHARSET = "UTF-8";
    private static final String HMAC_SHA256_ALGORITHM = "HmacSHA256";
    private static final String REQUEST_URI = "/onca/xml";
    private static final String REQUEST_METHOD = "GET";
    private String endpoint = "webservices.amazon.com";

    private String awsAccessKeyId = "";
    private String awsSecretKey = "";

    private SecretKeySpec secretKeySpec = null;
    private Mac mac = null;

    public SignedRequestsHelper() throws InvalidKeyException, NoSuchAlgorithmException, UnsupportedEncodingException
    {
        byte[] secretyKeyBytes = this.awsSecretKey.getBytes("UTF-8");
        this.secretKeySpec =
                new SecretKeySpec(secretyKeyBytes,
                        "HmacSHA256");
        this.mac = Mac.getInstance("HmacSHA256");
        this.mac.init(this.secretKeySpec);
    }

    public String sign(Map<String, String> params) {
        params.put("AWSAccessKeyId", this.awsAccessKeyId);
        params.put("Timestamp", timestamp());

        SortedMap sortedParamMap = new TreeMap(
                params);
        String canonicalQS = canonicalize(sortedParamMap);
        String toSign = "GET\n" + this.endpoint + "\n" + "/onca/xml" +
                "\n" + canonicalQS;

        String hmac = hmac(toSign);
        String sig = percentEncodeRfc3986(hmac);
        String url = "http://" + this.endpoint + "/onca/xml" + "?" + canonicalQS +
                "&Signature=" + sig;

        return url;
    }

    private String hmac(String stringToSign) {
        String signature = null;
        try
        {
            byte[] data = stringToSign.getBytes("UTF-8");
            byte[] rawHmac = this.mac.doFinal(data);
            Base64 encoder = new Base64();
            signature = new String(encoder.encode(rawHmac));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("UTF-8 is unsupported!", e);
        }
        byte[] rawHmac;
        byte[] data;
        return signature;
    }

    private String timestamp() {
        String timestamp = null;
        Calendar cal = Calendar.getInstance();
        DateFormat dfm = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        dfm.setTimeZone(TimeZone.getTimeZone("GMT"));
        timestamp = dfm.format(cal.getTime());
        return timestamp;
    }

    private String canonicalize(SortedMap<String, String> sortedParamMap) {
        if (sortedParamMap.isEmpty()) {
            return "";
        }

        StringBuffer buffer = new StringBuffer();
        Iterator iter = sortedParamMap.entrySet()
                .iterator();

        while (iter.hasNext()) {
            Map.Entry kvpair = (Map.Entry)iter.next();
            buffer.append(percentEncodeRfc3986((String)kvpair.getKey()));
            buffer.append("=");
            buffer.append(percentEncodeRfc3986((String)kvpair.getValue()));
            if (iter.hasNext()) {
                buffer.append("&");
            }
        }
        String cannoical = buffer.toString();
        return cannoical;
    }
    private String percentEncodeRfc3986(String s) {
        String out;
        try {
            out = URLEncoder.encode(s, "UTF-8").replace("+", "%20")
                    .replace("*", "%2A").replace("%7E", "~");
        }
        catch (UnsupportedEncodingException e)
        {
            String out1;
            out = s;
        }
        return out;
    }
}