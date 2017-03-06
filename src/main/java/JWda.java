import com.google.gson.JsonElement;
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
import java.util.function.Predicate;
import java.util.function.Supplier;

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

    public JsonObject waitFor(Supplier<JsonObject> action, Predicate<JsonObject> checker, String error_template) {
        long start = System.currentTimeMillis();

        while (System.currentTimeMillis() - start < Integer.parseInt(TestProperty.getProperty("timeout")) * 1000) {
            JsonObject response = action.get();
            if (checker.test(response)) {
                return response;
            }
            sleepTimeout("interval");
        }
        throw new AssertionError(String.format(error_template, response));
    }

    private JsonObject getSpecificElement(String label, JsonObject response) {
        for (JsonElement entry : response.get("value").getAsJsonArray()) {
            if (entry.toString().contains(label)) {
                return entry.getAsJsonObject();
            }
        }
        return null;
    }

    public void openApp(String device, String bundleId) {
        String body = "{\"desiredCapabilities\":{\"bundleId\":\"" + bundleId + "\"}}";
        JsonObject response = post(deviceUrl.get(device), body);
        url = deviceUrl.get(device);
        sessionId = response.get("sessionId").getAsString();
    }

    public void waitForNetworkDone() {
        String route = url + "/" + sessionId + "/elements";
        String body = "{\"using\":\"xpath\",\"value\":\"//XCUIElementTypeOther[@label='Network connection in progress']\"}";
        long start = System.currentTimeMillis();

        sleepTimeout("interval");
        while (System.currentTimeMillis() - start < Long.parseLong(TestProperty.getProperty("timeout")) * 1000) {
            JsonObject response = post(route, body);
            if (response.get("value").getAsJsonArray().size() == 0) {
                break;
            }
            sleepTimeout("interval");
        }
    }

    public JsonObject findElementsByClass(String classVal, String label) {
        Supplier<JsonObject> action = () -> {
            String route = url + "/" + sessionId + "/elements";
            String body = "{\"using\":\"class name\",\"value\":\"" + classVal + "\"}";
            return post(route, body);
        };

        Predicate<JsonObject> checker = jsonResponse -> jsonResponse.toString().contains(label);

        JsonObject response = waitFor(action, checker, "Element not found!");
        return getSpecificElement(label, response);
    }

    public JsonObject findElementsByXpath(String xpath) {
        Supplier<JsonObject> action = () -> {
            String route = url + "/" + sessionId + "/elements";
            String body = "{\"using\":\"xpath\",\"value\":\"" + xpath + "\"}";
            return post(route, body);
        };

        Predicate<JsonObject> checker = jsonResponse -> jsonResponse.get("value").getAsJsonArray().size() > 0 && !jsonResponse.toString().contains("Cannot evaluate results for XPath expression");

        JsonObject response = waitFor(action, checker, "Element not found!");

        return (JsonObject) response.get("value").getAsJsonArray().get(0);
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

    public void installApp(String device) {
        try {
            Runtime.getRuntime().exec(commandPath + " " + deviceInfo.get(device) + " install " + appPath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void uninstallApp(String device) {
        try {
            Runtime.getRuntime().exec(commandPath + " " + deviceInfo.get(device) + " uninstall " + bundleId);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void tap(String elementId) {
        String route = url + "/" + sessionId + "/element/" + elementId + "/click";
        sendRequest(route, "post", "");
    }

    public void input(String elementId, String val) {
        String routeClear = url + "/" + sessionId + "/element/" + elementId + "/clear";
        sendRequest(routeClear, "post", "");
        inputWithoutClear(elementId, val);
    }

    public void inputWithoutClear(String elementId, String val) {
        String routeInput = url + "/" + sessionId + "/element/" + elementId + "/value";
        String body = "{\"value\": \"" + val + "\"}";
        sendRequest(routeInput, "post", body);
    }

    public void scroll(String elementId, String name) {
        String route = url + "/" + sessionId + "/wda/Element/" + elementId + "/scroll";
        String body = "{\"name\": \"" + name + "\"}";
        sendRequest(route, "post", body);
    }

    public void swipeWIthDirection(String elementId, String direction) {
        String route = url + "/" + sessionId + "/wda/element/" + elementId + "/swipe";
        String body = "{\"direction\": \"" + direction + "\"}";
        sendRequest(route, "post", body);
    }

    public JsonObject getEelementVal(String elementId) {
        Supplier<JsonObject> action = () -> {
            String route = route = url + "/" + sessionId + "/element/" + elementId + "/attribute/value";
            return get(route);
        };

        Predicate<JsonObject> checker = jsonResponse -> jsonResponse.has(sessionId);

        return waitFor(action, checker, "Element not found!");
    }

    public JsonObject backgroundApp() {
        url = url.replace("/session", "");

        Supplier<JsonObject> action = () -> {
            String route = url + "/wda/homescreen";
            return post(route, "");
        };

        Predicate<JsonObject> checker = jsonResponse -> jsonResponse.has(sessionId);
        return waitFor(action, checker, "Action failed!");
    }

    public JsonObject waitForEnable(String elementId) {
        Supplier<JsonObject> action = () -> {
            String route = url + "/" + sessionId + "/element/" + elementId + "/enabled";
            return get(route);
        };

        Predicate<JsonObject> checker = jsonResponse -> jsonResponse.has("true");

        return waitFor(action, checker, "Element not enabled!");
    }

    public JsonObject waitForDisplay(String elementId) {
        Supplier<JsonObject> action = () -> {
            String route = url + "/" + sessionId + "/element/" + elementId + "/displayed";
            return get(route);
        };

        Predicate<JsonObject> checker = jsonResponse -> jsonResponse.has("true");

        return waitFor(action, checker, "Element not displayed!");
    }

}
