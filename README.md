TwitterRESTapiDemoProject
=========================
This program will use twitter api to get the user public information and computer the similarity. But you have to register a dev account in Twitter and paste your Consumer_Key and Consumer_Secret to InternetAccess.java.

How to run:

1. This program is based on Java SE 1.8. Please make sure you installed the newest version of Oracle Java JRE(not OpenJDK) before running. It is workable both in Linux(Ubuntu) and windows. However, the default Java of Ubuntu is OpenJDK. You have to install Oracle Java JRE manually instead of just apt-get install java, because apt-get install java will install OpenJDK, not Oracle Java.

2. Use command line: cd to current directory.

3. Paste the name pair to namepair.csv file

4. The result will save in similarity.csv file

Program Structure:

1. I use twitter REST API to get user information and transform some of user information to features. Then, measure the similarity by Cosine similarity. The output value will between 0~1. Larger is similar.
The main class is TwitterSimilarity.java

2. Almost all of the method in this program are static, because I think there is no data classes can share to save memory usage and want to make the code have better readability. Nevertheless, I will usage non-static method in more big program and use singleton and factory design pattern in database accessing.

3. In InternetAccess class, I also implement the Oauth1, which is useless in this program, but provides possibility to extend our program, if we want.

4. There are 4+5 classes in this program. 
(1) TwitterSimilarity: Entry point with the main method
(2) InternetAccess: Access token and connect to twitter REST API.
(3) FeatureExtraction: Extract features from user information.
(4) Measurement: Measure the similarity.
(5) Others in TwitterDataClass: Data class for parsing Json response.

5. Package dependency:
(1) gson: Parse Json response from twitter REST API
(2) JFeatureLib: Provide method to do image processing. I use it to create color histogram for user profile images and create features.
