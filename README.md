# Overview
Using Maven to build a Spring Boot Web Application and to deploy that application to Elastic Beanstalk.

For deployment, it builds on the example found here: https://java.awsblog.com/post/Tx32TLLUI5PY39/Deploying-Java-Applications-on-Elastic-Beanstalk-from-Maven

Also useful:
http://beanstalker.ingenieux.com.br/beanstalk-maven-plugin/usage.html
http://beanstalker.ingenieux.com.br/beanstalk-maven-plugin/put-environment-mojo.html
http://beanstalker.ingenieux.com.br/beanstalk-maven-plugin/upload-source-bundle-mojo.html

# Maven deploy
mvn -Pdeploy-dev clean package deploy

# Preparation
Before running this example you will need to do the following:

* Have an AWS account and have:
 * created an IAM user with suitable policy configured
 * IAM key / secret key downloaded
 * Have created a key pair for access to EC2 instances, useful at beginning to take a look round your Beanstalk instance
 * Have created an S3 bucket for deployment archive
* Have added AWS public & secret key into ~/.m2/settings.xml
  I recommend get things working with unencrypted password to start with, had some issues with encrypted one

# What have I done?

* First off, this is a Spring Boot Application and the Spring Boot Maven Plugin already creates an Uber Jar for us.
* Modified assembly plugin config:
```
    <id>zip</id>
    <formats>
        <format>zip</format>
    </formats>
    <includeBaseDirectory>false</includeBaseDirectory>
    <fileSets>
        <fileSet>
            <directory>${project.build.directory}</directory>
            <includes>
                <include>${project.artifactId}-${project.version}.jar</include>
            </includes>
            <outputDirectory>.</outputDirectory>
        </fileSet>
        <fileSet>
            <directory>config</directory>
            <filtered>true</filtered>
            <includes>
                <include>Procfile</include>
            </includes>
            <outputDirectory>.</outputDirectory>
        </fileSet>
        <fileSet>
            <directory>ebextensions</directory>
            <includes>
                <include>*.config</include>
            </includes>
            <outputDirectory>./.ebextensions</outputDirectory>
        </fileSet>
    </fileSets>
```
 * There was no need to use the Assembly Plugin to build the JAR.
 * Parameterised Procfile, not that its needed. Now the name of the JAR reflects what has been built
```
web: java -server -XX:+UseParallelOldGC -XX:+UseParallelGC -XX:NewRatio=2 -XX:+AggressiveOpts -jar ${project.artifactId}-${project.version}.jar
```
 * Included an ebextensions folder where additional customisations can be found
```
option_settings:
  aws:elasticbeanstalk:application:environment:
    PORT: 8080
    MVM_SECRET: This was set using an Environment property, change using AWS Console
    MVM_VALUE: This was set using an Environment property, change using AWS Console
```
* Changed the Solution Stack to pure Java that is now supported by Beanstalk - Tomcat is embedded in the application
```
    <beanstalk.solutionStack>64bit Amazon Linux 2016.03 v2.1.1 running Java 8</beanstalk.solutionStack>
```
* Added a property mapping for PORT: 8080 into ebextension so Beanstalk knows how to forward requests. Need this as no longer running a Tomcat stack.
* Added a couple of extra environment properties for the hell of it and to see what happens between redeploys when updated via AWS Console in between.
* Removed application.properties from resources - all properties can be configured as environment variables. This is true for Docker builds also.
* Added a timestamp to uploaded ZIP as collisions were causing issue. Need to sort this out!
```
                <maven.build.timestamp.format>yyyyMMddHHmmss</maven.build.timestamp.format>
                <timestamp>${maven.build.timestamp}</timestamp>
                <beanstalk.versionLabel>v${project.version}-${timestamp}</beanstalk.versionLabel>
                <beanstalk.s3Key>${project.artifactId}/${project.artifactId}-${beanstalk.versionLabel}.zip</beanstalk.s3Key>
```
* Disabled multipart uploads as this option was causing errors in more builds than not.
```
                <beanstalk.multipartUpload>false</beanstalk.multipartUpload>
```

# TODO
1. Tighten up the AWS policy
2. Deployment to EBS environment based on branch committed to support Git Flow way of working

# TODO - Documentation
1. Walk through setting up Maven settings.xml
2. Walk through AWS configuration
    * IAM User
    * IAM Policy
    * Download IAM User key and secret
    * Adding the key to settings.xml
    * Create RSA key for ssh access to EC2 instances
