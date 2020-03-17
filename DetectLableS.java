import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicSessionCredentials;
import com.amazonaws.services.rekognition.AmazonRekognition;
import com.amazonaws.services.rekognition.AmazonRekognitionClientBuilder;
import com.amazonaws.services.rekognition.model.AmazonRekognitionException;
import com.amazonaws.services.rekognition.model.DetectLabelsRequest;
import com.amazonaws.services.rekognition.model.DetectLabelsResult;
import com.amazonaws.services.rekognition.model.Image;
import com.amazonaws.services.rekognition.model.Label;
import com.amazonaws.services.rekognition.model.S3Object;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class DetectLableS {
	//For Taking Credentials
	public static  BasicSessionCredentials SessionCredentials() throws Exception
	{
	      String access_id, secret_key, session_token;
	      String str = new File(DetectLableS.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getParent();
	      File file = new File(str + "/credentials");
		 
		  List<String> lines = Collections.emptyList(); 
	      lines = Files.readAllLines(Paths.get(file.getAbsolutePath()), StandardCharsets.UTF_8); 
	      access_id= lines.get(1).split("=")[1];
	      secret_key = lines.get(2).split("=")[1];
	      session_token = lines.get(3).split("=")[1];	    
	      BasicSessionCredentials sessionCredentials = new BasicSessionCredentials(access_id, secret_key, session_token);
	      return sessionCredentials;
	}

	
	
   public static void main(String[] args) throws Exception { 
	  Scanner sc = new Scanner(System.in);
	  
      String bucket = "njit-cs-643";
      AmazonRekognition rekognitionClient = AmazonRekognitionClientBuilder.standard().withCredentials(new AWSStaticCredentialsProvider(SessionCredentials())).withRegion("us-east-1").build();
      final AmazonSQS sqs = AmazonSQSClientBuilder.standard().withCredentials(new AWSStaticCredentialsProvider(SessionCredentials())).withRegion("us-east-1").build();
      System.out.println("Enter URL OF your SQS ");
      //Url can be find on your sqs on amazon aws account. for more instruction read readme file.
      String sqsurl = sc.nextLine();
      

      
     // System.out.format("Objects in S3 bucket %s:\n", bucket);
      final AmazonS3 s3 = AmazonS3ClientBuilder.standard().withRegion("us-east-1").build();
      ListObjectsV2Result result = s3.listObjectsV2(bucket);
      List<S3ObjectSummary> objects = result.getObjectSummaries();
      for (S3ObjectSummary os : objects) {
         // System.out.println("* " + os.getKey());
      
      DetectLabelsRequest request = new DetectLabelsRequest().withImage(new Image().withS3Object(new S3Object().withName(os.getKey()).withBucket(bucket))).withMaxLabels(10).withMinConfidence(75F);

      try {
         DetectLabelsResult result1 = rekognitionClient.detectLabels(request);
         List <Label> labels = result1.getLabels();
         

         final Map<String, String> attributes = new HashMap<>();

         // A FIFO queue must have the FifoQueue attribute set to true.
         attributes.put("Jalaj.fifo", "true");
         attributes.put("ContentBasedDeduplication", "true");

         // The FIFO queue name must end with the .fifo suffix.
//         final CreateQueueRequest createQueueRequest =
//                 new CreateQueueRequest("Jalaj.fifo")
//                         .withAttributes(attributes);
//         final String myQueueUrl = sqs.createQueue(createQueueRequest).getQueueUrl();
         for (Label label: labels) {
        	 if(label.getName().equals("Car")&&label.getConfidence()>90){
        		 System.out.println("Detected labels for " + os.getKey());
        		 System.out.println(label.getName() + ": " +"Confidence "+ label.getConfidence()+"\n");
//        		 
        		 //Starting sending data in FIFO SQS
        		 System.out.println("Sending a message to Queue .fifo.\n");
        		 final com.amazonaws.services.sqs.model.SendMessageRequest sendMessageRequest = new com.amazonaws.services.sqs.model.SendMessageRequest(sqsurl, os.getKey());

        		 // When you send messages to a FIFO queue, you must provide a non-empty MessageGroupId.
        		 sendMessageRequest.setMessageGroupId("Group-1");

        		 // Uncomment the following to provide the MessageDeduplicationId
        		 sendMessageRequest.setMessageDeduplicationId(os.getETag());
        		 final com.amazonaws.services.sqs.model.SendMessageResult sendMessageResult = sqs.sendMessage(sendMessageRequest);
        		 final String sequenceNumber = sendMessageResult.getSequenceNumber();
        		 final String messageId = sendMessageResult.getMessageId();
        		 System.out.println("SendMessage succeed with messageId " + messageId + ", sequence number " + sequenceNumber + "\n");     
         }
        }
      } catch(AmazonRekognitionException e) {
         e.printStackTrace();
      }
      catch (final AmazonServiceException ase) {
          ase.printStackTrace();
      } catch (final AmazonClientException ace) {
          ace.printStackTrace();
      }
      
   }
  //Final Message 
      final com.amazonaws.services.sqs.model.SendMessageRequest sendMessageRequest = new com.amazonaws.services.sqs.model.SendMessageRequest(sqsurl, "-1");

		 // When you send messages to a FIFO queue, you must provide a non-empty MessageGroupId.
		 sendMessageRequest.setMessageGroupId("Group-1");

		 // Uncomment the following to provide the MessageDeduplicationId
		 sendMessageRequest.setMessageDeduplicationId("-1");
		 final com.amazonaws.services.sqs.model.SendMessageResult sendMessageResult = sqs.sendMessage(sendMessageRequest);
    //sqs.deleteQueue(sqsurl);
}
}