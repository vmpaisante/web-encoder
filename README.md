# web-encoder
Spring application to encode video files unsuited for web to web supported formats

----------------
| INTRODUCTION |
----------------

This project is a challenge from the SambaTech selective process.
It consists of a web application that is capable to store, encode and watch
a video file thats initially unsuited for web playback.

To do such, web-encoder uses Amazon's S3 storage to save both input and
encoding output generated from the uploaded video. It uses the Zencoder
service to encode the video files. The application is written in Java
using the Spring Boot framework. It also uses Maven to build the application
and publishes it to heroku through the following url:

https://webencoder.herokuapp.com/

-------------------
| USER EXPERIENCE |
-------------------

The application starts by showing an upload page, prompting the user
to select and upload a video file for encoding.
Imediatly after the upload the user is diverted to as encoding status screen,
where they can view the current encoding job status. The status is refreshed
every second.
Once the enconding is done and the output file is uploaded to S3, the
encoding screen provides a link for the watch screen. The watch screen plays
the user's encoded video a provides a link to retur to the upload screen.

---------------------
| PROJECT STRUCTURE |
---------------------

The project source code is found inside the src/main/java folder.
In this folder you can find the main Application.java file.

In the src/main/java/controllers folder you can find the main controller 
for this project. It provides all the logic for the user experience.

In the src/main/java/storage you can find the StorageService interface and
the S3StorageService implementation that operates on Amazon's S3.

In the src/main/java/encoding you can find the EncodingService interface and
the ZencoderS3Service implementation that operates on Amazon's S3 and 
issues encoding jobs to Zencoder.

In the src/main/resources folder resides the application.properties file
that holds private and local information, like keys and path locations.
Due to the nature of this project the application.properties is present 
in the git hub repository, but due to its sensitive information it shouldn't
be present in a public repository on a comercial and more extensive project.

Also in the src/main/resources folder it is present the screen templates 
that are processed into html screen by the controller and the thymeleaf
template engine. It also posesses the css and javascript files for the project.
The javascript code present is used to monitor, using JQuery and Ajax, the
encoding job, and changes the html accordingly.

----------------
| INSTALLATION |
----------------

Due to its Maven configuration pom.xml file, installing the project's
dependencies, compiling and executing it is very simple. From the project
folder root location, it's enough to run:

mvn package && java -jar target/webencoder-0.1.0.jar

After that the server will be running and listening to port 8080.
To acces it, load, in your browser the following url:

http://localhost:8080
