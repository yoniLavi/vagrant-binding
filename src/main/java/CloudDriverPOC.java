/**
 * Created with IntelliJ IDEA.
 * User: yoni
 * Date: 6/30/13
 * Time: 2:33 PM
 * To change this template use File | Settings | File Templates.
 */

import com.guigarage.vagrant.Vagrant;
import com.guigarage.vagrant.configuration.VagrantEnvironmentConfig;
import com.guigarage.vagrant.configuration.VagrantPortForwarding;
import com.guigarage.vagrant.configuration.VagrantVmConfig;
import com.guigarage.vagrant.configuration.builder.VagrantEnvironmentConfigBuilder;
import com.guigarage.vagrant.configuration.builder.VagrantVmConfigBuilder;
import com.guigarage.vagrant.model.VagrantEnvironment;
import com.guigarage.vagrant.model.VagrantVm;
import spark.Request;
import spark.Response;
import spark.Route;

import java.io.File;
import java.io.IOException;

import static spark.Spark.*;

public class CloudDriverPOC {

    public static void startSparc(final VagrantEnvironment environment) {
        setIpAddress("127.0.0.1");
        setPort(9101);

        get(new Route("/instance") {
            @Override
            public Object handle(Request request, Response response) {
                response.type("text/html");
                String output = "These are your vms:\n";
                for (VagrantVm vm : environment.getAllVms()) {
                    output += String.format("* %s is %s\n", vm.getName(), vm.isRunning() ? "up" : "down");
                }
                return output;
            }
        });

        post(new Route("/instance") {
            @Override
            public Object handle(Request request, Response response) {
                response.type("text/html");
                environment.up();
                return "Successfully launched your environment";
            }
        });

        delete(new Route("/instance") {
            @Override
            public Object handle(Request request, Response response) {
                response.type("text/html");
                environment.destroy();
                return "Environment destroyed";
            }
        });
    }

    public static VagrantEnvironment createEnvironment(String VMName, String ip, String envPath) throws IOException {
        VagrantVmConfig vmConfig = VagrantVmConfigBuilder
                                           .create()
                                           .withLucid32Box()
                                           .withName(VMName)
                                           .withHostOnlyIp(ip)
                                           .withVagrantPortForwarding(new VagrantPortForwarding(22, 8022))
                                           .build();

        VagrantEnvironmentConfig environmentConfig = VagrantEnvironmentConfigBuilder
                                                             .create()
                                                             .withVagrantVmConfig(vmConfig)
                                                             .build();

        Vagrant vagrant = new Vagrant(true);

        return vagrant.createEnvironment(new File(envPath), environmentConfig);
    }

    public static void main(String[] args) {
        VagrantEnvironment environment = null;

        try {
            environment = createEnvironment("myVM", "192.168.0.101", "/tmp/vagrantEnv");

            startSparc(environment);

        } catch (IOException e) {
            e.printStackTrace();
//        } finally {
//            if (environment != null) {
//                environment.destroy();
//            }
        }

    }
}
