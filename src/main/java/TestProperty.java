import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class TestProperty {
    private static final String propPath = System.getProperty("user.dir")
            + "/src/properties/" + "test.properties";

    /**
     * load configuration info from test.properties
     *
     * @param key
     * @return
     * @throws Exception
     */
    public static String getProperty(String key) {
        Properties prop = new Properties();
        InputStream is = null;
        try {

            is = new FileInputStream(propPath);
            prop.load(is);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }

        }
        return prop.getProperty(key);
    }

}
