# Knowledge Board

KnowledgeBoard is a Knowledge Base Tool and Question & Answer Platform.
It runs as a Plugin Module in the Doubleclue Enterprise Management ( **DCEM** ). 
You can find the DCEM repository at https://github.com/HWS-DoubleClue/IAM-Password-Manager. 

For further details, have a look at the [Knowledge Board Manual](https://doubleclue.com/files/DC_Knowledgeboard_Manual_en.pdf)


## Features 

### Categories

Questions and answers are always assigned to a specific category. A category can be **Public** or **Private**. 
Every category has its own administrators, which can manage users, questions and tags for this category. They can also block specific users from seeing a category.

#### Public Categories
All DCEM users are eligible to participate in questions and answers of public categories. 

#### Private Categories
Only users who have been added as members of this category are allowed to access the questions and answers of this category. 

### Tags
Users can attach Tags to their questions. Tags are category specific and can be added to a category by category administrators or global Knowledge Board administrators.

### Follow Notification
Users can follow questions, categories or tags and will be notified by e-mail whenever a new answer or question is posted.

### Search Engine
Knowledge Board comes with a search engine, which allows to search for question titles, content and tags. 


## [Try it out for free](https://doubleclue.online/dcem/createTenant/index.xhtml)

### On Premises or in the Cloud

You can install the solution in a DCEM on premises or as "Software As A Service" in our DoubleClue cloud.

### Build

You can download the latest release version of Knowledge Board from GitHub.

If you prefer to build a snapshot version yourself, follow these steps:
 
- Check out DCEM at https://github.com/HWS-DoubleClue/IAM-Password-Manager
- Check out Knowledge Board
- Build DCEM in an IDE of your choice
- Execute Maven with clean package
- The output is a jar file in the target folder


#### Install on Premises
The installation is of Knowledge Board is quick and easy. Simply copy the knowledgeboard.jar to subfolder "plugins" in your DCEM folder and restart DCEM.
You would also need to acquire a license key from the Doubleclue support.  
- Contact: support@doubleclue.com

For more information check our detailed instruction at https://github.com/HWS-DoubleClue/IAM-Password-Manager/blob/master/Documents/InstallPluginModule.odt or have a look at the [DoubleClue Manual](https://doubleclue.com/wp-content/uploads/DCEM_Manual_EN.pdf) Chapter 20: DoubleClue Plugin Modules.



