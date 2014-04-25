BeanstalkdUtils
===============

A set of utilities for Beanstalkd

System requirements:
- jsvc(*)
- java7 (jdk for devs, jre for everybody)
- maven (dev only)

(*) If you want to specify the current working directory inside the jsvc_exec(), using the parameter -cwd, you need to use the latest jsvc version.

Source dependencies are automatically resolved through Maven

Step-by-step compilation:
- Install requirements with your favorite pkg manager
- Download source from github or directly clone the repository
- Once you are in the project folder, run `mvn clean install`
- Two jars should have been generated in the `target` folder

Step-by-step installation:
- Customize the `./clidispatcher` file and move it in the `/etc/init.d/` directory
- Take the `./target/*jar-with-dependencies.jar` and move it in the `$FILE_PATH` directory as in the `./clidispatcher` file
- chmod 755 it
- `sudo update-rc.d clidispatcher defaults`
- `sudo service clidispatcher start` (next time it will start automatically)
- Enjoy!

Notes:
- At the moment the maximum waiting time for stop is statically bound at 60 seconds
