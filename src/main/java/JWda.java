import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.IOException;

/**
 * Created by phoebusliang on 02/03/2017.
 */
public class JWda {
    public JWda() {

    }

    public void sendRequest(String requestRoute, String requestType, String requestBody) {


        try {
            switch (requestType) {
                case "post":
                    CloseableHttpClient httpclient = HttpClients.createDefault();
                    HttpGet httpGet = new HttpGet(requestRoute);
                    CloseableHttpResponse response = httpclient.execute(httpGet);
                    break;
                case "get":

                default:
                    break;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
