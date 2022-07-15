import com.google.api.gax.longrunning.OperationFuture;
import com.google.cloud.compute.v1.*;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class CreateInstance {
    public static void main(String[] args) throws IOException, ExecutionException, InterruptedException,
            TimeoutException {
        String project = "raidrin-experiments";
        String zone = "us-central1-a";
        String instanceName = "compute-sdk-experiment";
        createInstance(project, zone, instanceName);
    }

    // Create a new instance with the provided "instanceName" value in the specified project and zone.
    private static void createInstance(String project, String zone, String instanceName) throws IOException,
            ExecutionException, InterruptedException, TimeoutException {
        // Below are sample values that can be replaced.
        // machineType: machine type of the VM being created.
        // *   This value uses the format zones/{zone}/machineTypes/{type_name}.
        // *   For a list of machine types, see https://cloud.google.com/compute/docs/machine-types
        // sourceImage: path to the operating system image to mount.
        // *   For details about images you can mount, see https://cloud.google.com/compute/docs/images
        // diskSizeGb: storage size of the boot disk to attach to the instance.
        // networkName: network interface to associate with the instance.
        String machineType = String.format("zones/%s/machineTypes/e2-micro", zone);
        String sourceImage = String.format("projects/debian-cloud/global/images/family/%s", "debian-11");
        long diskSizeGb = 10L;
        String networkName = "default";

        try (InstancesClient instancesClient = InstancesClient.create()) {
            // Instance creation requires at least one persistent disk and one network interface.
            AttachedDisk disk = AttachedDisk.newBuilder()
                    .setBoot(true)
                    .setAutoDelete(true)
                    .setType(SavedAttachedDisk.Type.PERSISTENT.toString())
                    .setDeviceName("disk-1")
                    .setInitializeParams(
                            AttachedDiskInitializeParams.newBuilder()
                                    .setSourceImage(sourceImage)
                                    .setDiskSizeGb(diskSizeGb)
                                    .build()
                    )
                    .build();

            // Use the network interface provided in the networkName argument.
            NetworkInterface networkInterface = NetworkInterface.newBuilder()
                    .setName(networkName)
                    .build();

            // Bind `instanceName`, `machineType`, `disk`, and `networkInterface` to an instance.
            Instance instanceResource = Instance.newBuilder()
                    .setName(instanceName)
                    .setMachineType(machineType)
                    .addDisks(disk)
                    .addNetworkInterfaces(networkInterface)
                    .build();

            System.out.printf("Creating instance: %s at %s %n", instanceName, zone);

            // Insert the instance in the specified project and zone.
            InsertInstanceRequest insertInstanceRequest = InsertInstanceRequest.newBuilder()
                    .setProject(project)
                    .setZone(zone)
                    .setInstanceResource(instanceResource)
                    .build();

            OperationFuture<Operation, Operation> operation = instancesClient.insertAsync(insertInstanceRequest);

            // Wait for the operation to complete
            Operation response = operation.get(3, TimeUnit.MINUTES);

            if (response.hasError()) {
                System.out.println("Instance creation failed !!" + response);
                return;
            }
            System.out.println("Operation Status: " + response.getStatus());
        }
    }
}
