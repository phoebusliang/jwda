/**
 * Created by twe on 02/03/2017.
 */

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.util.EntityUtils;

import java.net.URI;

import static org.apache.http.HttpStatus.*;

public class Test {

    public static void main(String args[]) {
        try {
//            CloseableHttpClient httpclient = HttpClients.createDefault();
//            HttpGet httpGet = new HttpGet("http://localhost:3000");
//            CloseableHttpResponse response = httpclient.execute(httpGet);
            URI uri = new URIBuilder()
                    .setScheme("http")
                    .setHost("localhost")
                    .setPort(8101)
                    .setPath("/status")
                    .build();
            HttpGet httpget = new HttpGet(uri);

            CloseableHttpClient httpclient = HttpClients.createDefault();
            HttpResponse response = httpclient.execute(new HttpGet(uri));
            HttpEntity entity = response.getEntity();
            String responseString = EntityUtils.toString(entity, "UTF-8");
            System.out.println(responseString);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
