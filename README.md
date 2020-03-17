# CloudComputingNJIT
Creating Instances in AWS. By java calling Images from S3 bucket and  then passing those images to Image recognition.  If Images have car in them sending those images Index to AWS SQS queue with one instance. With 2nd Instance calling images from S3 bucket and comparing those images with index in SQS so we will get only images which have car in them. Then on those images have car send them to Text Recognition and print respective text in file again each   




	-wget --no-check-certificate --no-cookies --header "Cookie: oraclelicense=accept-securebackup-cookie" http://download.oracle.com/otn-pub/java/jdk/8u141-b15/336fa29ff2bb4ef291e347e091f7f4a7/jdk-8u141-linux-x64.rpm
** Heare are some Instruction that will help in Proper working of code**
*
1) You have to create two ec2 Instance 
2) Then Install JDK in them. for JDK following two commands aare given bellow
	-wget --no-check-certificate --no-cookies --header "Cookie: oraclelicense=accept-securebackup-cookie" http://download.oracle.com/otn-pub/java/jdk/8u141-b15/336fa29ff2bb4ef291e347e091f7f4a7/jdk-8u141-linux-x64.rpm
	
	-sudo yum install -y jdk-8u141-linux-x64.rpm
	

###########################################################################################################################

3) Upload your ec2 instance credential file and make sure that file in local have full current user access. Reapate same step with your second ec2 Instance check full step to find credential bellow. 

4) For running this Programms on EC@ you have to upload (name of you file).JAR file with command 
5) scp -v -i .\<Name of key>.pem .\<Name of Jar file>.jar ec2-user@ec2-54-172-205-90.compute-1.amazonaws.com:/home/ec2-user/
6)Give user acces to all of your file by typing chmod 400 <file Name .extention> and chmod 755 <file Name .extention>
	*Repeat for all file in your ec2 Instance except jdk intallation file*(if still doesn't work check user permision in properties of your file and assinge user permission)	
7)this can be done in Windows PowerShell(go in directory where all your .jar .pem and credential file are located)
8)Then you have to upload your credentials with same command above 
9)One more thing in you have to change th url of your .java file.
	-FOr which when you run your program it will ask for SQS url copy and past it for more see  instruction below
10)After chaging create new .jar file in your eclips of your code with respective libraries. will be shar if you dont have them.
11)again uplaod that .jar file
12)till now you have uploaded your 1)credential 2).jar file and Install JDK on your ec2 instance.
13)Then you can RUN DetectTexts.jar in your ec2 instance B by command java -jar DetectTexts.jar
14)Then on you ec2 Instance A do same thing. Install jdk, upload credendial and uplaod DetectLabels.jar
15)Now same way you can Run java -jar DetectLabels.jar
16)CATIOUS ABOUT UPLOADING YOUR FILE 
17)I have found out there are some time when you send message to sqs they get lost in that case they might have lost. 
	PLEASE RUN THAT PROGRAM AGAIN YOU DIDN'T FIND ANY IMAGE IN SECOND INSTANCE
18)For output i am printing some message to check thing went through well. 
19)After complition of output in ec2 instance B. type "ls" on your terminal of ec2 instance B it will show list files there you shoud found 3 files
20)JDK, <textrecognition .jar file name>.jar file, <key name>.key file and credentials
21)now type java -jar DetectLabels.jar it run and ask for SQS URL copy and past it . //TO find sqs url see bellow.
21)now type cat <file name>.txt
22) You will find the output.
#Note you have to run DetectLabels.jar in ec2 instance A
# and DetectTexts.jar in ec2 instance B
# for that command is java -jar <filename>.jar
# please check for credential before running trough ls command
**For Finding credential **
	Click on link https://www.awseducate.com/student/s/
	login with your sqs credential
	Now CLick on AWS Acount on top right
	now click in AWS Educate Starter Account 
	Now you will see vocarum page where in middle right you will see Blue *Account Details* button click on it.
	The you will see an popup window there click on *show* next to AWS CLI
	copy all text from defalt to last letter
	and then past in your .aws folder
	
	TO find your .aws folder in your local locate C:\Users\jalaj\.aws 
	.aws folder you will find a file name credentials open it with text editor and replace the existing text 
	save it and now copy your credential file and upload it with command
	scp -v -i .\<name of > .\credentials ec2-user@ec2-35-175-232-191.compute-1.amazonaws.com:/home/ec2-user/

**For Finding URL of SQS**
	
	Click on link https://www.awseducate.com/student/s/
	login with your sqs credential
	Now CLick on AWS Acount on top right
	now click in AWS Educate Starter Account 
	Now you will see vocarum page where in middle right you will see Blue *AWS Console* button click on it.
	Find SQS insearch panel. 
	click in it and select your SQS for current project
	in botton you will find a URL like this
	https://sqs.us-east-1.amazonaws.com/133511003762/Jalaj.fifo
**If you have not created sqs already**
	#For creating sqs#
		*#**#*
	login with your sqs credential
	Now CLick on AWS Acount on top right
	now click in AWS Educate Starter Account 
	Now you will see vocarum page where in middle right you will see Blue *AWS Console* button click on it.
	Find SQS insearch panel. 
	Bellow simple queu service click on Get Started Now
	in Quetion name fine <any name>.fifo
	Now Click in FIFO Queue in rigt side of screen
	Scrol down and click on create Quick Create Queue
# For RUnning java files which uploaded on canvas with requied Libraried please donload them from web. 	
# For coding many part were took refrence from amazon tutorial sample programs for each and individual part. 

# After every 3 hour session expiers so please again reapete the step to find credentials.
