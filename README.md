Mecadaver
=========

Do you want to test your WebDAV server?

Launch Mecadaver, and it will walk around randomly in the WebDAV folders, download some files, etc.  
The goal is to simulate typical WebDAV usage by office workers.

Parameters:

* HTTP Server.
* Path to the WebDAV folder.
* Name of the tab-separated configuration file that defines the credentials of each user. The load test will be performed will all of these users in parallel. (tab-separated. commas and spaces won't work)
* Pause between each request of a single user, in milliseconds.
* Number of iterations to perform.

Example: `java -jar Mecadaver_0.1.jar http://localhost:8080 /alfresco/webdav credentials.tsv 1000 200`

If you find a bug or a way to make the load more realistic (closer to what an office worker would do), please use the [issue tracker](https://github.com/nicolas-raoul/Mecadaver/issues), thanks a lot! Patches are very welcome too!
