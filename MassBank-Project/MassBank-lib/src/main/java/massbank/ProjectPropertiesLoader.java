package massbank;

import java.io.IOException;
import java.util.Properties;

public class ProjectPropertiesLoader {

    public static Properties loadProperties() {
        Properties properties = new Properties();
        try {
            properties.load(ProjectPropertiesLoader.class.getClassLoader().getResourceAsStream("project.properties"));
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
        return properties;
    }
}