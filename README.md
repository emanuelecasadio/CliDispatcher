BeanstalkdUtils
===============

A set of utilities for Beanstalkd


System requirements:
- jsvc
- java7

Source dependencies are automatically resolved through Maven


How to proceed:
1. Create an Executable Jar File (even if there is no executable class is ok)
   and EXTRACT (not pack) libraries into the .jar (call it clidispatcher.jar)
2. Copy the clidispatcher file into your /etc/init.d/ directory after customization
3. sudo update-rc.d clidispatcher defaults
4. sudo service clidispatcher start
5. Enjoy!

