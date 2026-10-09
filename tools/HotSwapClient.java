package tools;

import com.sun.jdi.Bootstrap;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.VirtualMachineManager;
import com.sun.jdi.connect.AttachingConnector;
import com.sun.jdi.connect.Connector;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HotSwapClient {
    public static void main(String[] args) {
        String host = "localhost";
        String port = args.length > 0 ? args[0] : "5005";
        String stagingDir = args.length > 1 ? args[1] : "/tmp/ea_jar_staging";

        try {
            VirtualMachineManager vmm = Bootstrap.virtualMachineManager();
            AttachingConnector connector = null;
            for (AttachingConnector conn : vmm.attachingConnectors()) {
                if ("com.sun.jdi.SocketAttach".equals(conn.name())) {
                    connector = conn;
                    break;
                }
            }
            if (connector == null) {
                System.err.println("Error: SocketAttach connector not found.");
                System.exit(1);
            }

            Map<String, Connector.Argument> arguments = connector.defaultArguments();
            arguments.get("hostname").setValue(host);
            arguments.get("port").setValue(port);

            System.out.println("==> Connecting to Minecraft JVM on " + host + ":" + port + "...");
            VirtualMachine vm = connector.attach(arguments);
            try {
                if (!vm.canRedefineClasses()) {
                    System.err.println("Error: Target VM does not support class redefinition.");
                    System.exit(1);
                }

                File base = new File(stagingDir);
                if (!base.exists() || !base.isDirectory()) {
                    System.err.println("Error: Staging dir " + stagingDir + " does not exist.");
                    System.exit(1);
                }

                Map<ReferenceType, byte[]> map = new HashMap<>();
                Path basePath = base.toPath();

                try (var stream = Files.walk(basePath)) {
                    stream.filter(p -> p.toString().endsWith(".class") && !p.toString().contains("mixin"))
                          .forEach(p -> {
                              try {
                                  Path rel = basePath.relativize(p);
                                  String className = rel.toString()
                                          .replace(File.separatorChar, '.')
                                          .replace('/', '.')
                                          .replaceAll("\\.class$", "");

                                  List<ReferenceType> found = vm.classesByName(className);
                                  if (!found.isEmpty()) {
                                      byte[] bytes = Files.readAllBytes(p);
                                      for (ReferenceType ref : found) {
                                          map.put(ref, bytes);
                                      }
                                      System.out.println("  -> Queued for reload: " + className);
                                  }
                              } catch (Exception ex) {
                                  System.err.println("Error reading " + p + ": " + ex.getMessage());
                              }
                          });
                }

                if (map.isEmpty()) {
                    System.out.println("Notice: No loaded non-mixin mod classes found in JVM to reload yet.");
                } else {
                    vm.redefineClasses(map);
                    System.out.println("==> [HOTSWAP SUCCESS] Successfully reloaded " + map.size() + " class(es) into Minecraft!");
                }
            } finally {
                vm.dispose();
            }
        } catch (Exception e) {
            System.err.println("HotSwap connection failed: " + e.getMessage());
            System.err.println("Tip: If the VS Code debugger is currently attached, press Shift+F5 in VS Code to disconnect so the hot-reloader can connect.");
            System.exit(1);
        }
    }
}
