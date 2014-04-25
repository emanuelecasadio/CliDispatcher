BeanstalkdUtils
===============

A set of utilities for Beanstalkd

System requirements:
- jsvc(*)
- java7 (openjdk-7-jdk for devs, openjdk-7-jre for everybody)
- maven (dev only)
- git (likely)
- beanstalkd (if you want to test)

(*) If you want to specify the current working directory inside the jsvc_exec(), using the parameter -cwd, you need to use the latest jsvc version.

Source dependencies are automatically resolved through Maven

Step-by-step compilation:
- `sudo apt-get install openjdk-7-jdk jsvc maven git beanstalkd` (install requirements with your favorite pkg manager)
- Download source from github or directly clone the repository
- Once you are in the project folder, run `mvn clean install`
- Two jars should have been generated in the `target` folder

Step-by-step installation:
- Customize the `./clidispatcher` file (check at least the JAVA_HOME and the USER because most probably you don't want to run every command as the root user)
- `sudo cp ./clidispatcher /etc/init.d/clidispatcher` (copy the upstart script into the proper folder)
- `sudo chmod 755 /etc/init.d/clidispatcher` (gives execution and read rights to everybody)
- `sudo mkdir -p /usr/local/clidispatcher/lib` (creates the default FILE_PATH directory as well as the lib directory (you'll need it later))
- `sudo cp ./target/BeanstalkdUtils-0.1-jar-with-dependencies.jar /usr/local/clidispatcher/clidispatcher.jar` (take the jar with dependencies, copy it in the FILE_PATH directory (default shown here)) and rename it as `clidispatcher.jar`)
- `sudo wget -O /usr/local/clidispatcher/lib/commons-daemon-1.0.15.jar https://www.apache.org/dist/commons/daemon/binaries/commons-daemon-1.0.15.jar` (get the commons-daemon library and puts it into the right folder)
- `sudo chown -R user:root /usr/local/clidispatcher` (give the root and the USER user (as specified in the clidispatcher upstart file) ownership rights for the FILE_PATH directory)
- `sudo chmod -R 770 /usr/local/clidispatcher` (give them all rights)
- `sudo update-rc.d clidispatcher defaults` (generate default rc.d scripts)
- `sudo service clidispatcher start` (start the service)
- Enjoy!

Notes:
- At the moment the maximum waiting time for stop is statically bound at 60 seconds
- If you get a "unknown parameter -cwd" error, check for the IMPORTANT NOTE in the clidispatcher upstart file
