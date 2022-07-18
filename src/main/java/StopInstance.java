import com.google.api.gax.longrunning.OperationFuture;
import com.google.cloud.compute.v1.DeleteInstanceRequest;
import com.google.cloud.compute.v1.InstancesClient;
import com.google.cloud.compute.v1.Operation;
import com.google.cloud.compute.v1.Operation.Status;
import com.google.cloud.compute.v1.StopInstanceRequest;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class StopInstance {

    public static void main(String[] args)
            throws IOException, ExecutionException, InterruptedException, TimeoutException {
    /* project: project ID or project number of the Cloud project your instance belongs to.
       zone: name of the zone your instance belongs to.
       instanceName: name of the instance your want to stop.
     */
        String project = "raidrin-experiments";
        String zone = "us-central1-a";
        String instanceName = "compute-sdk-experiment";

        stopInstance(project, zone, instanceName);
    }

    // Stops a started Google Compute Engine instance.
    public static void stopInstance(String project, String zone, String instanceName)
            throws IOException, ExecutionException, InterruptedException, TimeoutException {
    /* Initialize client that will be used to send requests. This client only needs to be created
       once, and can be reused for multiple requests. After completing all of your requests, call
       the `instancesClient.close()` method on the client to safely
       clean up any remaining background resources. */
        try (InstancesClient instancesClient = InstancesClient.create()) {
            StopInstanceRequest stopInstanceRequest = StopInstanceRequest.newBuilder()
                    .setProject(project)
                    .setZone(zone)
                    .setInstance(instanceName)
                    .build();

            OperationFuture<Operation, Operation> operation = instancesClient.stopAsync(
                    stopInstanceRequest);
            Operation response = operation.get(3, TimeUnit.MINUTES);

            if (response.getStatus() == Status.DONE) {
                System.out.println("Instance stopped successfully ! ");
            }

            DeleteInstanceRequest deleteInstanceRequest =
                    DeleteInstanceRequest
                            .newBuilder()
                            .setProject(project)
                            .setZone(zone)
                            .setInstance(instanceName)
                            .build();
            OperationFuture<Operation, Operation> operation2 = instancesClient
                    .deleteAsync(deleteInstanceRequest);

            Operation response2 = operation2.get(3, TimeUnit.MINUTES);
            if (response2.getStatus() == Status.DONE) {
                System.out.println("Instance deleted successfuly");
            }

        }
    }
}
