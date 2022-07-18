import com.google.api.gax.longrunning.OperationFuture;
import com.google.cloud.compute.v1.*;
import com.google.cloud.compute.v1.Operation.Status;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class DeleteManagedInstanceGroup {

    public static void main(String[] args)
            throws IOException, ExecutionException, InterruptedException, TimeoutException {
    /* project: project ID or project number of the Cloud project your instance belongs to.
       zone: name of the zone your instance belongs to.
       instanceName: name of the instance your want to stop.
     */
        String project = "raidrin-experiments";
        String zone = "us-central1-a";
        String instanceName = "experiment-managed-instance-group";

        deleteInstance(project, zone, instanceName);
    }

    // Stops a started Google Compute Engine instance.
    public static void deleteInstance(String project, String zone, String instanceName)
            throws IOException, ExecutionException, InterruptedException, TimeoutException {
        try (InstanceGroupManagersClient instanceGroupManagersClient = InstanceGroupManagersClient.create()) {
            DeleteInstanceGroupManagerRequest deleteInstanceGroupManagerRequest =
                    DeleteInstanceGroupManagerRequest
                            .newBuilder()
                            .setInstanceGroupManager(instanceName)
                            .setProject(project)
                            .setZone(zone)
                            .build();
            OperationFuture<Operation, Operation> operation = instanceGroupManagersClient
                    .deleteAsync(deleteInstanceGroupManagerRequest);

            Operation response = operation.get(3, TimeUnit.MINUTES);
            if (response.getStatus() == Status.DONE) {
                System.out.println("Managed instance group deleted successfuly");
            }

        }
    }
}
