Mobicents SIP Presence Network Simulation
=========================================
This README documents how to run a SIP Presence network simulation with the tools provided in this directory.


1) What can I simulate with this test framework?
------------------------------------------------------------------------
It is possible to simulate presence publishers, single presence entities, event list presence entities and winfo package subscribers. For each a different SIPP script is provided which means it is possible to run a single simulation or all at same times.


2) Do I need anything else besides what is provided?
------------------------------------------------------------------------
Yes, SIPp is needed in the $PATH environment variable, and it must be the last revision present in SIPp SVN, since the scripts require features not present in last release at the time this README was made, which is 3.1.


3) Ok how I do run this?
------------------------------------------------------------------------
First you need to provision the XDM with the resource lists and pres rules docs for the simulated users, to do that simply enter the xdm-provisioning directory and run "mvn install" with the server running. By default there will be 100 users provisioned and each will have a resource list with other 10 users, but this is configurable in the pom.xml file.

Once all data is provisioned you may start any script you want, the names should sufficient to identify what does each. Again if you defined more users then you need to edit notifiers.csv and users.csv and update the users number.


4) What are the default scenarios for each simulation?
------------------------------------------------------------------------
4.1) publisher.xml, the scenario for the simulation of publishers
This scenario will send 10 PUBLISH requests per call (1 initial publish + 1 modify publish + 1 remove publish + 7 refresh publish), one each 10 seconds.
 
4.2) subscriber.xml, the scenario for the simulation of single presence entities subscribers
This scenario will send 10 SUBSCRIBE requests per call (1 initial subscribe + 8 refresh subscribe + 1 unsubscribe), one each 10 seconds.

4.3) winfo-subscriber.xml, the scenario for the simulation of presence.winfo subscribers
This scenario will send 10 SUBSCRIBE requests per call (1 initial subscribe + 8 refresh subscribe + 1 unsubscribe), one each 10 seconds.

4.4) eventlist-subscriber.xml, the scenario for the simulation of event list presence entities subscribers
This scenario will send 10 SUBSCRIBE requests per call (1 initial subscribe + 8 refresh subscribe + 1 unsubscribe), one each 10 seconds.


5) Is there anything I can do to optimize performance in the server?
------------------------------------------------------------------------
Yes, turn off logging or define it's root level as WARN or ERROR, other configurations specific to the underlying JAIN SLEE container may also help, see it's documentation.


6) Where can I get some answers regarding this test framework?
------------------------------------------------------------------------
Join http://groups.google.com/group/mobicents-public , search for discussion topics related, if nothing found begin one.

Author: Eduardo Martins, Red Hat Inc.
http://emmartins.blogspot.com/

 