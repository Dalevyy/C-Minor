package utilities;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.util.Properties;

// Dr. C's class from ProcessJ
public class PropertiesLoader {

    public static URL getURL(String fileName) {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        URL url = cl.getResource(fileName);
        if(url != null) { return url; }

        cl = PropertiesLoader.class.getClassLoader();
        return cl.getResource(fileName);
    }

    public static Properties loadProperties(File file) {
        Properties p = new Properties();
        try {
            p.load(Files.newInputStream(file.toPath()));
        }
        catch(IOException e) {
            System.out.println(PrettyPrint.RED + "Error! Properties file could not be found.");
            System.exit(1);
        }

        return p;
    }
}
