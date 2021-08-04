package com.test.aws.lambda.importer;

import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.test.aws.lambda.model.FeedModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Read s3 JSON
 */
public class ReadS3Json {
    private final AmazonS3 s3 = AmazonS3ClientBuilder.defaultClient();
    private final Logger logger = LoggerFactory.getLogger(ReadS3Json.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    public void handler(S3Event event) {
        event.getRecords().forEach(record -> {
            S3ObjectInputStream s3inputStream = s3
                    .getObject(record.getS3().getBucket().getName(), record.getS3().getObject().getKey())
                    .getObjectContent();
            try {
                logger.info("Reading data from s3 record");
                List<FeedModel> s3BucketEvents = Arrays
                        .asList(objectMapper.readValue(s3inputStream, FeedModel[].class));
                logger.info(s3BucketEvents.toString());
                s3inputStream.close();
                logger.info("Input stream closed");
            } catch (JsonMappingException e) {
                logger.error("JsonMappingException", e);
                throw new RuntimeException("Error while processing S3 event", e);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}