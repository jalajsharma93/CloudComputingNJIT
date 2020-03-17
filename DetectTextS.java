import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicSessionCredentials;
import com.amazonaws.services.rekognition.AmazonRekognition;
import com.amazonaws.services.rekognition.AmazonRekognitionClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.rekognition.model.AmazonRekognitionException;
import com.amazonaws.services.rekognition.model.Image;
import com.amazonaws.services.rekognition.model.S3Object;
import com.amazonaws.services.rekognition.model.DetectTextRequest;
import com.amazonaws.services.rekognition.model.DetectTextResult;
import com.amazonaws.services.rekognition.model.TextDetection;

import java.util.Collections;
import java.util.List;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.BufferedWriter;
import java.io.File;
import java.util.Scanner;

public class DetectTextS {
	
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
    @SuppressWarnings("resource")
	public static void main(String[] args) throws Exception {
        
    	Scanner sc = new Scanner(System.in);
        String bucket = "njit-cs-643";
        AmazonRekognition rekognitionClient = AmazonRekognitionClientBuilder.standard().withCredentials(new AWSStaticCredentialsProvider(SessionCredentials())).withRegion("us-east-1").build();
        final AmazonSQS sqs = AmazonSQSClientBuilder.standard().withCredentials(new AWSStaticCredentialsProvider(SessionCredentials())).withRegion("us-east-1").build();
        System.out.format("Objects in S3 bucket %s:\n", bucket);
        final AmazonS3 s3 = AmazonS3ClientBuilder.standard().withRegion("us-east-1").build();
        ListObjectsV2Result result = s3.listObjectsV2(bucket);
        List<S3ObjectSummary> objects = result.getObjectSummaries();
        System.out.println("Enter ULR of your SQS");//Which you can find on your AWS account
        String sqsurl = sc.nextLine();// assinging queue url to Variable
        FileWriter fw = new FileWriter("DetectedText.txt",true); // Creating File for .txt
        BufferedWriter bw = new BufferedWriter(fw);
        
        System.out.println("Receiving messages \n");
        final ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest(sqsurl);
        receiveMessageRequest.setMaxNumberOfMessages(10);
        while(true) {
        final List<Message> messages = sqs.receiveMessage(receiveMessageRequest).getMessages();
       
        for (final Message message : messages) {
        	if(message.getBody().equals("-1")) {
        		System.out.println("end of Reciving Message");
        		final String messageReceiptHandle = message.getReceiptHandle();
                sqs.deleteMessage(new DeleteMessageRequest(sqsurl,messageReceiptHandle));
        		 bw.close();
        		System.exit(0);
        	}
        	for (S3ObjectSummary os : objects) {
        		
              if(os.getKey().contains(message.getBody())) {
            	  bw.write(message.getBody());
            	  bw.write("    ");
            	  System.out.println("* " + os.getKey());
            	  DetectTextRequest request = new DetectTextRequest().withImage(new Image().withS3Object(new S3Object().withName(os.getKey()).withBucket(bucket)));
            	  try {
                      DetectTextResult textresult = rekognitionClient.detectText(request);
                      List<TextDetection> textDetections = textresult.getTextDetections();
                      System.out.println("Detected lines and words for " + os.getKey());
                      for (TextDetection text: textDetections) {
                    	  
                          if(text.getConfidence() > 90 ) {                  
                              System.out.println("Detected: " + text.getDetectedText());
                              System.out.println("Confidence: " + text.getConfidence().toString());
                              System.out.println();
                      		  bw.write(text.getDetectedText());
                      		  bw.write("\t\t\t");
                      }
                      }
                   } catch(AmazonRekognitionException e) {
                      e.printStackTrace();
                   }catch(IOException e) {e.printStackTrace();}
              
            	// Deleting the message.
                  System.out.println("Deleting a message.\n");
                  final String messageReceiptHandle = message.getReceiptHandle();
                  sqs.deleteMessage(new DeleteMessageRequest(sqsurl,messageReceiptHandle));              
                  bw.write("\n");
              }            
        }
        System.out.println();

         }
        }
        }
    }