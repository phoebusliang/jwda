import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.util.function.Function;
import java.util.function.Predicate;

import static datacollection.DeviceConfig.*;


/**
 * Created by phoebusliang on 02/03/2017.
 */
public class JWda {
    CloseableHttpClient httpclient = HttpClients.createDefault();
    HttpResponse response;
    JsonObject jsonObject = null;
    HttpEntity entity;
    String responseString;
    public String url;
    public String sessionId;

    public JWda() {

    }

    public JsonObject sendRequest(String requestRoute, String requestType, String requestBody) {
        try {
            switch (requestType) {
                case "get":
                    get(requestRoute);
                    break;
                case "post":
                    post(requestRoute, requestBody);
                    break;
                default:
                    return null;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    private JsonObject post(String requestRoute, String requestBody) {
        try {
            HttpPost request = new HttpPost(requestRoute);
            StringEntity requestEntity = new StringEntity(requestBody);
            request.setEntity(requestEntity);
            response = httpclient.execute(request);
            entity = response.getEntity();
            responseString = EntityUtils.toString(entity);
            jsonObject = new JsonParser().parse(responseString).getAsJsonObject();
            return jsonObject;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    private JsonObject get(String requestRoute) {
        try {
            response = httpclient.execute(new HttpGet(requestRoute));
            entity = response.getEntity();
            responseString = EntityUtils.toString(entity);
            jsonObject = new JsonParser().parse(responseString).getAsJsonObject();
            return jsonObject;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public Object waitForResponse(String requestRoute, Predicate<Object> checker, String requestType, String requestBody) {
        return this.waitForResponse(requestRoute, checker, requestType, requestBody, "Some errors happen.");
    }

    public Object waitForResponse(String requestRoute, Predicate<Object> checker, String requestType, String body, String error_template) {
        long start = System.currentTimeMillis();

        while (System.currentTimeMillis() - start < Integer.parseInt(TestProperty.getProperty("timeout")) * 1000) {
            JsonObject response = sendRequest(requestRoute, requestType, body);
            if (checker.test(response)) {
                return response;
            }
            sleepTimeout("interval");
        }
        throw new AssertionError(String.format(error_template, response));
    }

    public JsonObject waitFor(Function<String, JsonObject> action, Predicate<JsonObject> checker, String error_template) {
        long start = System.currentTimeMillis();

        while (System.currentTimeMillis() - start < Integer.parseInt(TestProperty.getProperty("timeout")) * 1000) {
            JsonObject response = action.apply("");
            if (checker.test(response)) {
                return response;
            }
            sleepTimeout("interval");
        }
        throw new AssertionError(String.format(error_template, response));
    }

    public void openApp(String device, String bundleId) {
        String body = "{\"desiredCapabilities\":{\"bundleId\":\"" + bundleId + "\"}}";
        JsonObject response = post(deviceUrl.get(device), body);
        url = deviceUrl.get(device);
        sessionId = response.get("sessionId").getAsString();
    }

    private void waitForNetworkDone() {
        String route = url + "/" + sessionId + "/elements";
        String body = "{\"using\":\"xpath\",\"value\":\"//XCUIElementTypeOther[@label='Network connection in progress']\"}";
        long start = System.currentTimeMillis();

        sleepTimeout("interval");
        while (System.currentTimeMillis() - start < Long.parseLong(TestProperty.getProperty("timeout")) * 1000) {
            JsonObject response = post(route, body);
            if (response.size() == 0) {
                break;
            }
            sleepTimeout("interval");
        }
    }

    public JsonObject findElementsByClass(String classVal, String label) {
        Function<String, JsonObject> action = noUse -> {
            String route = url + "/" + sessionId + "/elements";
            String body = "{\"using\":\"class name\",\"value\":\"" + classVal + "\"}";
            return post(route, body);
        };

        Predicate<JsonObject> checker = jsonResponse -> jsonResponse.has(label);

        return waitFor(action, checker, "Element not found!");
    }

    public JsonObject findElementsByXpath(String xpath) {
        Function<String, JsonObject> action = noUse -> {
            String route = url + "/" + sessionId + "/elements";
            String body = "{\"using\":\"xpath\",\"value\":\"" + xpath + "\"}";
            return post(route, body);
        };

        Predicate<JsonObject> checker = jsonResponse -> jsonResponse.get("value").getAsJsonArray().size() > 0 && !jsonResponse.has("Cannot evaluate results for XPath expression");

        return waitFor(action, checker, "Element not found!");
    }

    private void sleepTimeout(String timeout) {
        try {
            Thread.sleep(Long.parseLong(TestProperty.getProperty(timeout)) * 1000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void cleanKeychain(String device) {
        try {
            Runtime.getRuntime().exec(commandPath + " " + deviceInfo.get(device) + " clear_keychain " + bundleId);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
