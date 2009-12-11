---------< Mobicents SIP Presence Service >----------

This module is a full SIP Presence Service, including servers:

- XDMS (XML Document Management Server)
- PS (Presence Server)
- RLS (Resource List Server)

----> REQUIREMENTS:

The XDMS and PS depends on the Mobicents Http-Servlet and SIP11 JAIN SLEE RAs.
You need to deploy those RAs prior to the installation of  
the servers.

----> CONFIGURATION:

If you're going to build the servers from source you can 
configure:

- XCAP ROOT: This is the root relative path for XCAP uris.
Default is "/mobicents" which is the servlet default name of
the Mobicents Http Servlet RA. This value is set through the 
property ${xdm.server.xcap.root} in the pom.xml on this
directory. Note that if you change this value then you need to
also change the servlet name of the Mobicents Http Servlet RA
(consult its documentation for how to do this).

- Presence Server's Notifiers PresRules AUID : The id of the
app usage to be used, by the Presence Server, to retrieve
pres rules of a notifier, from the XDMS. You can change the
default value (OMA Pres Rules) using the property
${presence.server.notifier.presrules.auid} in the pom.xml on 
this directory, before building the server(s).

- Presence Server's Notifiers PresRules Document Name : The
name of the document to be used, by the Presence Server, to
retrieve the pres rules of a notifier, from the XDMS. You
can change the default value (pres-rules) using the
property ${presence.server.notifier.presrules.documentName} in the
pom.xml on this directory, before building the server(s).

----> INSTALL:

Option 1) For all servers integrated on same Mobicents do:

"mvn -f integrated/server/installer/pom.xml install" on this directory
or "mvn install" on integrated/server/installer/ sub-directory

Option 2) For independent servers (in different Mobicents hosts):

- XDMS : do "mvn -f xdm/server/installer/pom.xml install" on this directory
or "mvn install" on xdm/server/installer/ sub-directory
- PS : <not available yet>

----> UNINSTALL:

Option 1) For all servers integrated on same Mobicents do:

"mvn -f integrated/server/installer/pom.xml clean" on this directory
or "mvn clean" on integrated/server/installer/ sub-directory

Option 2) For independent servers:

- XDMS : do "mvn -f xdm/server/installer/pom.xml clean" on this directory
or "mvn clean" on xdm/server/installer/ sub-directory
- PS : <not available yet>

----> TESTING:

Currently only the XDMS has a test framework.
See xdm/server/tests/README.txt for more info.

Author: Eduardo Martins, JBoss R&D