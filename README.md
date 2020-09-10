Project Description:

The project is Sping MVC web application used to help a user manage their monthly expenses by entering them in the application and setting targets for his spendings.

The user(s) can:
Enter all their movements (both incomes and expenses) in the application
Define categories by which to categorize their movements (i.e. Salary, Freelance, Shopping etc..)
Define targets for their savings and expenses
View a monthly overview of their movements grouped by categories, visulized alongside the targets for those categories.

Installation Guide (Ubuntu):
1) Install java 11  (sudo apt install openjdk-11-jre-headless)
1) Install maven  (sudo apt install maven)
1) Install and configure postgresql
1) Clone project from: https://github.com/darthrado/PetProject-Personal-Finance-Tracker
1) Define the following enviornment variables:

pftappdburl=jdbc:postgresql:<db_url>

pftappdbuser=<db_username>

pftappdbpass=<db_password>

Example:

pftappdburl=jdbc:postgresql://localhost:5432/pft-db

pftappdbuser=my_database_username

pftappdbpass=my_database_password

6) Execute create_db.sql script to populate the db (script can be found in ~ProjectFolder/src/test/resources)
6) Navigate to the Project folder and package the project (mvn package)
6) Navigate to the target folder and run the newly created JAR file
6) Application can be accessed trough browser via localhost:8080
