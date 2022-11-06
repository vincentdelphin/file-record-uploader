# file-record-uploader
File recorder uploader project for Williams Lea.

The intial project was based upon the https://github.com/spring-guides/gs-uploading-files#run-the-application which demonstrates how to upload a file. The rest of the
project was focused on building the back-end to allow for the uploading of the records.

The majority of this project is developed within the uploadingfiles package found within the main/java directory. Records java file serves to encapsulate a record
object along with the methods to read and save the records to a h2 database which can be accessed through http://localhost:8080/h2-console/.
Upon connecting the database to the application, once the file has been successfully uploaded you can access the database to observe the records uploaded along 
with the event type mapping as requested in the assessment specification.

Unfortunately, I was unable to add javadoc due to having to work and visit immediate family over the past few weeks, I appreciate the patience and conisderation 
for allowing me to undertake the project. 

I am aware there is a bug in the code which is explained within the 'publishRecords' method found in main/java/model/records.java.

For any questions/explanations you may have, I am happy to discuss my solution via email (vincentmledelphin@gmail.com) or a zoom meeting anytime apart from Monday,Tuesday and Saturday as I am working.
