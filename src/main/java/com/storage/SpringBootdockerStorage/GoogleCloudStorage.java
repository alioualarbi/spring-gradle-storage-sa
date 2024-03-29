package com.storage.SpringBootdockerStorage;
import com.google.api.gax.paging.Page;
import com.google.auth.Credentials;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.*;
import lombok.extern.slf4j.Slf4j;
import com.google.auth.oauth2.ServiceAccountCredentials;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;
import java.io.File;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Simple class for creating, reading and modifying text blobs on Google Cloud
 */
@Slf4j
public class GoogleCloudStorage {

    private Storage storage;
    private Bucket bucket;

    public static void main(String[] args) throws Exception {

         GoogleCloudStorage googleCloudStorage =
            new GoogleCloudStorage("/workspace/app/build/client_secrets.json", "<YOUR-PROJECT_ID>");

        
        Bucket bucket = googleCloudStorage.getBucket("test-1-bucket");

         //Save a simple string
        BlobId blobId = googleCloudStorage.saveString("my-first-blob", "Hi there!", bucket);

         //Get it by blob id this time
        String value = googleCloudStorage.getString(blobId);
         log.info("Read data: {}", value);

        googleCloudStorage.updateString(blobId, "Bye now!");

         //Get the string by blob name
        value = googleCloudStorage.getString("my-first-blob");

        log.info("Read modified data: {}", value);


    }


    // Use path and project name
    private GoogleCloudStorage(String credentialsPath, String projectId) throws IOException {
        Credentials credentials = GoogleCredentials.fromStream(new FileInputStream(credentialsPath));
        storage = StorageOptions.newBuilder().setCredentials(credentials).setProjectId(projectId).build().getService();
    }

    // Check for bucket existence and create if needed.
    private Bucket getBucket(String bucketName) {
        bucket = storage.get(bucketName);
        if (bucket == null) {
            System.out.println("Creating new bucket.");
            bucket = storage.create(BucketInfo.of(bucketName));
        }
        return bucket;
    }

    // Save a string to a blob
    private BlobId saveString(String blobName, String value, Bucket bucket) {
        byte[] bytes = value.getBytes(UTF_8);
        Blob blob = bucket.create(blobName, bytes);
        return blob.getBlobId();
    }


    // get a blob by id
    private String getString(BlobId blobId) {
        Blob blob = storage.get(blobId);
        return new String(blob.getContent());
    }


    // get a blob by name
    private String getString(String name) {
        Page<Blob> blobs = bucket.list();
        for (Blob blob: blobs.getValues()) {
            if (name.equals(blob.getName())) {
                    return new String(blob.getContent());
            }
        }
        return "Blob not found";
    }

    // Update a blob
    private void updateString(BlobId blobId, String newString) throws IOException {
        Blob blob = storage.get(blobId);
        if (blob != null) {
            WritableByteChannel channel = blob.writer();
            channel.write(ByteBuffer.wrap(newString.getBytes(UTF_8)));
            channel.close();
        }
    }
}
