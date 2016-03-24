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

    public AwsFactory() {
        credentialsProviderChain = new DefaultAWSCredentialsProviderChain();
        credentialsProviderChain.setReuseLastProvider(true);
        s3client = new AmazonS3Client(getAwsCredentials());
    }

    private AWSCredentials getAwsCredentials() {
        return credentialsProviderChain.getCredentials();
    }

    public void createS3Bucket(String bucket) {
        bucket = bucket.toLowerCase();
        if (!s3client.doesBucketExist(bucket)) {
            s3client.createBucket(bucket, Region.AP_Sydney);
        }
    }

    public PutObjectResult uploadToS3(String bucket, String key, File file) {
        return s3client.putObject(new PutObjectRequest(bucket.toLowerCase(), key, file));
    }

    public String downloadFromS3(String bucket, String key) throws IOException {
        StringWriter stringWriter = new StringWriter();
        IOUtils.copy(s3client.getObject(new GetObjectRequest(bucket.toLowerCase(), key)).getObjectContent(), stringWriter);
        return stringWriter.toString();
    }
}
