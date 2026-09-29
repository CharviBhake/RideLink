package Smart_Carpooling.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;

@SpringBootApplication
public class DemoApplication {

    public static void main(String[] args) {
        System.out.println("MONGODB_URI = [" + System.getenv("MONGODB_URI") + "]");
        ConfigurableApplicationContext context= SpringApplication.run(DemoApplication.class,args);

        ConfigurableEnvironment environment=context.getEnvironment();
        String[] profiles = environment.getActiveProfiles();
        if (profiles.length > 0) {
            System.out.println("Active Profile: " + profiles[0]);
        } else {
            System.out.println("No active profile set.");
        }
    }

}
