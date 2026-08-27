package com.vsu.researchapp.infrastructure.config;

import com.azure.storage.blob.BlobServiceClient; 
import com.azure.storage.blob.BlobServiceClientBuilder; 
import org.springframework.beans.factory.annotation.Value; 
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty; 
import org.springframework.context.annotation.Bean; 
import org.springframework.context.annotation.Configuration; 

@Configuration
public class StorageConfig {

    // Defaulted to "" so this field resolves fine in dev, where azure.storage.connection-string
    // is never set (see application-dev.properties) -- the blobServiceClient() bean below is the
    // only thing that actually reads this value, and it's skipped entirely outside prod.
    @Value("${azure.storage.connection-string:}")
    private String connectionString;

    @Bean
    @ConditionalOnProperty(name = "file.storage.type", havingValue = "azure")
    // ^ this bean is only needed when running with file.storage.type=azure; dev machines running file.storage.type=local never evaluate this
    public BlobServiceClient blobServiceClient() {
        return new BlobServiceClientBuilder()
            .connectionString(connectionString)
            // ^ the connection string already encodes the account name + access key, so no separate credential object is needed
            .buildClient();
            // ^ builds and returns the top-level client AzureBlobStorageService is constructor-injected with
    }
}
