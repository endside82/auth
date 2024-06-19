package com.endside.config.util;


import com.endside.config.error.ErrorCode;
import com.endside.config.error.exception.RestException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import software.amazon.awssdk.auth.credentials.*;
import software.amazon.awssdk.core.client.config.ClientAsyncConfiguration;
import software.amazon.awssdk.core.client.config.ClientOverrideConfiguration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import software.amazon.awssdk.services.sns.model.PublishResponse;
import software.amazon.awssdk.services.sns.model.SnsException;
import software.amazon.awssdk.http.apache.ApacheHttpClient;


@Slf4j
@Component
public class AmazonSmsUtil {

    @Value("${aws.sns.ec2policy}")
    private Boolean ec2policy;

    @Value("${aws.sns.credentials.access-key}")
    private String accessKey;

    @Value("${aws.sns.credentials.secret-key}")
    private String secretKey;


    private SnsClient getSnsClient() {
        AwsCredentialsProvider awsCredentialsProvider;
        if(ec2policy) {
            // ec2 credential 방식 : ec2에 권한을 줘서 파일 서버에 통신할 수 있도록 한다.
            awsCredentialsProvider = InstanceProfileCredentialsProvider.create();
        } else {
            // IAM basic 방식 : local이나 비AWS 환경에서 테스트 가능 하도록 한다.
            if (!StringUtils.hasText(accessKey) || !StringUtils.hasText(secretKey)) {
                throw new RestException(ErrorCode.FAILED_TO_SEND_SMS);
            }
            AwsBasicCredentials awsBasicCredentials = AwsBasicCredentials.create(accessKey, secretKey);
            awsCredentialsProvider = StaticCredentialsProvider.create(awsBasicCredentials);
        }

        ApacheHttpClient.Builder httpClientBuilder =
                ApacheHttpClient.builder();

        ClientOverrideConfiguration.Builder overrideConfig =
                ClientOverrideConfiguration.builder();

        ClientAsyncConfiguration.Builder asyncConfig =
                ClientAsyncConfiguration.builder();

        return SnsClient.builder()
                .region(Region.US_EAST_1)
                .httpClientBuilder(httpClientBuilder)
                .overrideConfiguration(overrideConfig.build())
                .credentialsProvider(awsCredentialsProvider)
                .build();
    }


    public void pubTextSMS(String phoneNumber, String message) {
        SnsClient snsClient = getSnsClient();
        try {
            PublishRequest request = PublishRequest.builder()
                    .phoneNumber(phoneNumber)
                    .message(message)
                    .build();
            PublishResponse result = snsClient.publish(request);
            System.out.println(result.messageId() + " Message sent. Status was " + result.sdkHttpResponse().statusCode());

        } catch (SnsException e) {
            System.err.println(e.awsErrorDetails().errorMessage());
            System.exit(1);
        }
    }
}
