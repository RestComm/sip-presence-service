---------< Mobicents SIP Presence Service >----------

This module is a full SIP Presence Service, including servers:

- XDMS (XML Document Management Server)
- PS (Presence Server)

----> REQUIREMENTS:

The XDMS and PS depends on the Mobicents Http-Servlet and SIP 11 RAs, and SIP subscription Client and XDM Client Enablers.
You need to deploy those prior to the installation of  
the servers. 
File dependencies.xml can deploy/undeploy those components, use:

* "ant -f dependencies.xml deploy" to deploy all dependencies to Mobicents AS
* "ant -f dependencies.xml undeploy" to undeploy all dependencies from Mobicents AS

----> DEPLOY:

Option 1) For both servers integrated on same Mobicents do:

"ant integrated-deploy" on this directory

Option 2) For independent servers (in different Mobicents hosts):

- XDMS : do "ant xdms-deploy" on this directory
- PS : <not available yet>

----> UNDEPLOY:

Option 1) For both servers integrated on same Mobicents do:

"ant integrated-undeploy" on this directory

Option 2) For independent servers:

- XDMS : do "ant xdms-undeploy" on this directory
- PS : <not available yet>

-----> JAIN SLEE "PRESENCE AWARE" APP EXAMPLES:

Inside "examples" directory you can find JAIN SLEE applications,
which take advantage of the integrated Mobicents SIP Presence
Service. For more information on each example look at the
readme.txt file inside the specific example's directory.