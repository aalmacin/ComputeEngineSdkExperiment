import com.google.api.gax.longrunning.OperationFuture;
import com.google.cloud.compute.v1.*;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class UpdateInstanceGroupTargetCount {
    public static void main(String[] args) throws IOException, ExecutionException, InterruptedException, TimeoutException {
        String project = "raidrin-experiments";
        String zone = "us-central1-a";
        String instanceGroupName = "experiment-managed-instance-group";

        try (InstanceGroupManagersClient instanceGroupManagersClient = InstanceGroupManagersClient.create()) {
            InstanceGroupManager updatedInstanceGroupManager = InstanceGroupManager.newBuilder()
                    .setTargetSize(0)
                    .build();
            PatchInstanceGroupManagerRequest patchInstanceGroupManagerRequest = PatchInstanceGroupManagerRequest
                    .newBuilder()
                    .setInstanceGroupManager(instanceGroupName)
                    .setInstanceGroupManagerResource(updatedInstanceGroupManager)
                    .setProject(project)
                    .setZone(zone)
                    .build();
            OperationFuture<Operation, Operation> operation =
                    instanceGroupManagersClient.patchAsync(patchInstanceGroupManagerRequest);

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
