import java.io.IOException;
import java.net.MalformedURLException;

public abstract interface GetID
{
    public abstract void getIDList()
            throws MalformedURLException, IOException;

    public abstract void updateCSV(String paramString)
            throws MalformedURLException, IOException;

    public abstract void writeIDsToCSV(String paramString)
            throws IOException;
}