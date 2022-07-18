import com.google.api.gax.longrunning.OperationFuture;
import com.google.cloud.compute.v1.*;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class CreateManagedInstanceGroup {
    public static void main(String[] args) throws IOException, ExecutionException, InterruptedException,
            TimeoutException {
        String project = "raidrin-experiments";
        String region = "us-central1";
        String zone = "us-central1-a";
        String instanceGroupName = "experiment-managed-instance-group";
        String instanceTemplateName = "experiment-managed-instance-template";
        String networkName = "experiment-network";
        String subnetworkName = "experiment-subnetwork";

        String network = String.format("projects/%s/global/networks/%s", project, networkName);
        String subnetwork = String.format("projects/%s/regions/%s/subnetworks/%s", project, region, subnetworkName);
//        createInstanceTemplate(project, network, subnetwork, instanceTemplateName);
        createInstanceGroup(project, zone, instanceGroupName, instanceTemplateName);
    }

    private static void createInstanceTemplate(
            String project, String network, String subnetwork, String templateName
    ) throws IOException, ExecutionException, InterruptedException, TimeoutException {
        try (InstanceTemplatesClient instanceTemplatesClient = InstanceTemplatesClient.create();
             GlobalOperationsClient globalOperationsClient = GlobalOperationsClient.create()) {
            String machineType = "e2-micro";
            String sourceImage = String.format("projects/debian-cloud/global/images/family/%s", "debian-11");
            long diskSizeGb = 10L;
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

            InstanceProperties instanceProperties = InstanceProperties.newBuilder()
                    .addDisks(disk)
                    .setMachineType(machineType)
                    .addNetworkInterfaces(
                            NetworkInterface.newBuilder()
                                    .setNetwork(network)
                                    .setSubnetwork(subnetwork)
                                    .build()
                    )
                    .build();

            InstanceTemplate instanceTemplate = InstanceTemplate.newBuilder()
                    .setName(templateName)
                    .setProperties(instanceProperties)
                    .build();

            InsertInstanceTemplateRequest insertInstanceTemplateRequest = InsertInstanceTemplateRequest.newBuilder()
                    .setProject(project)
                    .setInstanceTemplateResource(instanceTemplate)
                    .build();

            Operation operation = instanceTemplatesClient.insertCallable()
                    .futureCall(insertInstanceTemplateRequest)
                    .get(3, TimeUnit.MINUTES);
            Operation response = globalOperationsClient.wait(project, operation.getName());

            if (response.hasError()) {
                System.out.println("Template creation from subnet failed ! ! " + response);
                return;
            }
            System.out.printf("Template creation from subnet operation status %s: %s", templateName,
                    response.getStatus());
        }
    }

    // Create a new instance with the provided "instanceName" value in the specified project and zone.
    private static void createInstanceGroup(String project, String zone, String instanceGroupName,
                                            String instanceTemplateName) throws IOException,
            ExecutionException, InterruptedException, TimeoutException {
        try (InstanceGroupManagersClient instanceGroupManagersClient = InstanceGroupManagersClient.create();
             InstanceTemplatesClient instanceTemplatesClient = InstanceTemplatesClient.create()
        ) {
            // Bind `instanceName`, `machineType`, `disk`, and `networkInterface` to an instance.
            String templatePath = String.format("projects/%s/global/instanceTemplates/%s",
                    project, instanceTemplateName);
            InstanceGroupManager instanceGroup = InstanceGroupManager.newBuilder()
                    .setName(instanceGroupName)
                    .setInstanceTemplate(
                            templatePath
                    )
                    .setTargetSize(1)
                    .build();

            System.out.printf("Creating instance: %s at %s %n", instanceGroupName, zone);

            // Insert the instance in the specified project and zone.
            InsertInstanceGroupManagerRequest insertInstanceRequest = InsertInstanceGroupManagerRequest.newBuilder()
                    .setProject(project)
                    .setZone(zone)
                    .setInstanceGroupManagerResource(instanceGroup)
                    .build();

            OperationFuture<Operation, Operation> operation
                    = instanceGroupManagersClient.insertAsync(insertInstanceRequest);

            // Wait for the operation to complete
            Operation response = operation.get(5, TimeUnit.MINUTES);

            if (response.hasError()) {
                System.out.println("Instance creation failed !!" + response);
                return;
            }
            System.out.println("Operation Status: " + response.getStatus());
        }
    }
}
