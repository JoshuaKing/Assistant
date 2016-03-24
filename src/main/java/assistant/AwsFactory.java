package assistant;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProviderChain;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.amazonaws.services.s3.model.Region;
import modules.AssistantModule;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;

/**
 * Created by Josh on 24/03/2016.
 */
public class AwsFactory {
    private final AWSCredentialsProviderChain credentialsProviderChain;
    private final AmazonS3 s3client;
    private final Class<? extends AssistantModule> moduleClass;

    public AwsFactory(Class<? extends AssistantModule> moduleClass) {
        this.moduleClass = moduleClass;
        credentialsProviderChain = new DefaultAWSCredentialsProviderChain();
        credentialsProviderChain.setReuseLastProvider(true);
        s3client = new AmazonS3Client(getAwsCredentials());
    }

    private AWSCredentials getAwsCredentials() {
        return credentialsProviderChain.getCredentials();
    }

    public void createS3Bucket(String bucket) {
        String fullBucketName = (moduleClass.getSimpleName() + "-" + bucket).toLowerCase();
        if (!s3client.doesBucketExist(fullBucketName)) {
            s3client.createBucket(fullBucketName, Region.AP_Sydney);
        }
    }

    public PutObjectResult uploadToS3(String bucket, String key, File file) {
        String fullBucketName = (moduleClass.getSimpleName() + "-" + bucket).toLowerCase();
        return s3client.putObject(new PutObjectRequest(fullBucketName, key, file));
    }

    public String downloadFromS3(String bucket, String key) throws IOException {
        String fullBucketName = (moduleClass.getSimpleName() + "-" + bucket).toLowerCase();
        StringWriter stringWriter = new StringWriter();
        IOUtils.copy(s3client.getObject(new GetObjectRequest(fullBucketName, key)).getObjectContent(), stringWriter);
        return stringWriter.toString();
    }
}
