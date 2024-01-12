# Knowledge Board

KnowledgeBaord is a Plugin-Module for Doubleclue Enterprise Management ( **DCEM** ). 
See https://github.com/HWS-DoubleClue/IAM-Password-Manager. 

It is a knowledge-base system intended for small and medium entrprises. 

For furter details [See Manual](https://doubleclue.com/files/DC_Knowledgeboard_Manual_en.pdf)


## Features 

### Categories

Questions and answewrs are always assigned to a specific category. A category may be a **Public** or a **Private** category. 
Every Categories have its own administrators. 

#### Public Categories
All DCEM users are eligible to participate in questions and answers of public categories. 

#### Private Categories
Only members of this category are allowed to access knowledge of this category. 

#### Tags
Users may attach Tags to their questions. Tags are category specific and can be added to a category by category-admins or global Knowledgeboard admins.


#### Follow Notification
Users may follow questions or tags and will be notified by e-mails.

#### Search Engine
It has a power search engine. Search for title, content and tags. 


## [Try it out for free](https://doubleclue.online/dcem/createTenant/index.xhtml)

### On Premisis or in the Cloud

You can install the solution on premisis or "Software As A Service" in our cloud.

## Build

Best solution is to download the latest release from Github.

If you prefer to build a snap-short version yourself, follow the steps:
 
- First you have to check-out DCEM
- Check out the project
- build DCEM
- Execute Maven with clean pacakge
- The output is a jar file in the target folder


## Install on Premises
The installation is very easy. You only need to copy the knowledgeboard.jar to DCEM sub-Folder "plugins" and restart DCEM.
You would also need to acquire a licence key from the doubleclue-support.  
- Contact: support@doubleclue.com



