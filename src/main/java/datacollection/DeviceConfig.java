package datacollection;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by twe on 03/03/2017.
 */
public class DeviceConfig {
    public static Map<String, String> deviceUrl = new HashMap<String, String>() {
        {
            put("7p", "http://localhost:8101/session");
            put("6sp", "http://localhost:8102/session");
        }
    };
    public static String appPath = "/Users/twe/Documents/app/RESI-Internal.app";

    public static String commandPath = "/Users/twe/Documents/fbsimctl/fbsimctl";

    public static String bundleId = "com.rea-group.reapa.internal";

    public static Map<String, String> deviceInfo = new HashMap<String, String>() {
        {
            put("7p", "D178F8E5-108E-4890-87F9-7F6BE7E8877D");
            put("6sp", "51C14A74-5014-4EDA-A64D-2541C14FB164");
        }
    };
}
