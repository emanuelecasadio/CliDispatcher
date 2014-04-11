BeanstalkdUtils
===============

A set of utilities for Beanstalkd


System requirements:
- jsvc
- java7

Source dependencies are automatically resolved through Maven


How to proceed:
- Create an Executable Jar File (even if there is no executable class is ok)
   and EXTRACT (not pack) libraries into the .jar (call it clidispatcher.jar)
- Copy the clidispatcher file into your /etc/init.d/ directory after customization
- sudo update-rc.d clidispatcher defaults
- sudo service clidispatcher start
- Enjoy!

