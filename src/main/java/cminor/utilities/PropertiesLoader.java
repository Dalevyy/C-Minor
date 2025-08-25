package cminor.utilities;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.util.Properties;

// Dr. C's class from ProcessJ
public class PropertiesLoader {

    /**
     * Finds the location of the properties file.
     * @param fileName String representing the name of the file.
     * @return {@link URL} for where the file is located.
     */
    public static URL getURL(String fileName) {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        URL url = cl.getResource(fileName);
        if(url != null) { return url; }

        cl = PropertiesLoader.class.getClassLoader();
        return cl.getResource(fileName);
    }

    /**
     * Loads the properties file.
     * @param file The {@link File} containing the properties.
     * @return {@link Properties}
     */
    public static Properties loadProperties(File file) {
        Properties p = new Properties();
        try {
            p.load(Files.newInputStream(file.toPath()));
        }
        catch(IOException e) {
            System.out.println(file);
            System.out.println(PrettyPrint.RED + "There was an error loading the properties file... :(");
            System.exit(1);
        }

        return p;
    }
}
