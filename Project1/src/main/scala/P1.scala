import org.apache.spark.sql.hive.HiveContext
import org.apache.spark.SparkConf
import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._
import org.apache.spark.sql.SQLContext
import java.sql.DriverManager
import java.sql.Connection
import java.util.Scanner
import scala.collection.mutable.ArrayBuffer
import scala.util.control.Exception._
import java.security.MessageDigest
import java.math.BigInteger
import scala.sys.process._




object P1 {
    def main(args: Array[String]): Unit = {
        // This block of code is all necessary for spark/hive/hadoop
        System.setSecurityManager(null)
        System.setProperty("hadoop.home.dir", "C://apps//opt//hadoop") // change if winutils.exe is in a different bin folder
        val conf = new SparkConf()
            .setMaster("local") 
            .setAppName("P1")    // Change to whatever app name you want
        val sc = new SparkContext(conf)
        sc.setLogLevel("ERROR")
        val hiveCtx = new HiveContext(sc)
        import hiveCtx.implicits._

        //This block to connect to mySQL
        val driver = "com.mysql.cj.jdbc.Driver"
        val url = "jdbc:mysql://localhost:3306/BigData" // Modify for whatever port you are running your DB on
        val username = "root"
        val password = "*****" // Update to include your password
        var connection:Connection = null

        Class.forName(driver)
        connection = DriverManager.getConnection(url, username, password)

        //Method to check login credentials
        
         val adminCheck = login(connection)
            if (adminCheck) {
                var admin0 = new Employee.Admin()
                println("Welcome! you are logged in as admin. Please select from menue:")
                //Admin menu
                println("***Admin menue***\n")
                print("Enter 1) to add a new user. ")
                print("2) to edit a user's information. ")
                print("3) to delete a user. ")
                println("4) to query hive. ")
                println("or 99 to quit.")
                var i = readInt()
                while( i != 99) {
                // i:Int match Block
                i match {
                case 1  => admin0.addUser(connection)
                    println("Success! new user added.")
                    println("Please enter next choice or enter 99 to end program: ")
                    i = readInt()
                case 2  => println("**Editting user**")
                    editUser(connection)
                    println("Please enter next choice or enter 99 to end program: ")
                    i = readInt()
                case 3  => println("**Deletting user**")
                    deleteUser(connection)
                    println("Please enter next choice or enter 99 to end program: ")
                    i = readInt()
                case 4  => println("**Query DB**")
                    queries(connection, hiveCtx)
                    println("Please enter next choice or enter 99 to end program: ")
                    i = readInt()
                // catch the default with a variable so you can print it
                case whoa  => println("Unexpected case: " + whoa.toString)
                    println("Please try again or enter 99 to end program: ")
                    i = readInt()
                } 
            }

            } else {
                println("Welcome! Please select from the menue.")
            println("Enter 1 to update your password. ")           
            println("Enter 2 to query DB.")
            println("Or enter 99 to quit.")
            var i = readInt()
            while( i != 99) {
            // i:Int match Block
            i match {
            case 1  => updatePass(connection)
                println("Success!! Your password has been updated.")
                println("Please enter next choice or enter 99 to end program: ")
                i = readInt()
            case 2  => println("**Query DB**")
                queries(connection, hiveCtx)
                println("Please enter next choice or enter 99 to end program: ")
                i = readInt()
            // catch the default with a variable so you can print it
            case whoa  => println("Unexpected case: " + whoa.toString)
                println("Please try again or enter 99 to end program: ")
                i = readInt()
            } 
        }
    }

       
   
    
        sc.stop() // Necessary to close cleanly. Otherwise, spark will continue to run and run into problems.
    }//main ends here



  


    // This method checks to see if a user-inputted username/password combo is part of a mySQL table.
    // Returns true if admin, false if basic user, gets stuckl in a loop until correct combo is inputted (FIX)
    def login(connection: Connection): Boolean = {
        
        while (true) {
            val statement = connection.createStatement()
            val statement2 = connection.createStatement()
            println("Enter login: ")
            var scanner = new Scanner(System.in)
            var login = scanner.nextLine().trim()

            println("Enter password: ")
            var password = scanner.nextLine().trim()
            var pass = Crypto.customHash(password)
            val resultSet = statement.executeQuery("SELECT Role FROM Hive_USERS WHERE login='"+login+"' AND password='"+pass+"';")
           // val resultSet = statement.executeQuery("SELECT COUNT(*) FROM P1_admins WHERE username='"+username+"' AND password='"+password+"';")
            while ( resultSet.next() ) {
                if (resultSet.getString(1) == "Admin") {
                    return true;
                }
            }

            val resultSet2 = statement2.executeQuery("SELECT Role FROM Hive_USERS WHERE login='"+login+"' AND password='"+pass+"';")
            while ( resultSet2.next() ) {
                if (resultSet2.getString(1) == "User") {
                    return false;
                }
            }

            println("Username/password combo not found. Try again!")
        }
        return false
    }





    def insertCyberAttacks(hiveCtx:HiveContext): Unit = {
        // This statement creates a DataFrameReader from your file that you wish to pass in. We can infer the schema and retrieve
        // column names if the first row in your csv file has the column names. If not wanted, remove those options. This can 
        // then be **************************
        val output = hiveCtx.read
            .format("csv")
            .option("inferSchema", "true")
            .option("header", "true")
            .load("input/Cyberattacks.csv")
        output.limit(15).show() // Prints out the first 15 lines of the dataframe

        output.registerTempTable("Cyb2") // This will create a temporary table from your dataframe reader that can be used for queries. 
//*****************************
        // These next three lines will create a temp view from the dataframe you created and load the data into a permanent table inside
        // of Hadoop. Thus, we will have data persistence, and this code only needs to be ran once. Then, after the initializatio, this 
        // code as well as the creation of output will not be necessary.
        output.createOrReplaceTempView("temp_data")
        hiveCtx.sql("DROP TABLE Cyb1")
        hiveCtx.sql("CREATE TABLE IF NOT EXISTS Cyb1 (Year INT, Organisation STRING, Critical_Industry STRING, Organisation_size STRING, Sector STRING, Country STRING, Improper_network_segmentation STRING, Inappropriate_remote_access STRING, Absence_of_encryption STRING, Restructuring_after_attack STRING, Ransom_paid STRING, Number_of_users_affected INT, Attack_type STRING, Attacker STRING, Attack_vector STRING, Names_exposed STRING, Address_leaked STRING,	PII_exposed	STRING, Credit_card_details_leaked STRING,	SSN_taxID_leaked STRING, Subsequent_fraudulent_use_of_data STRING, Summary STRING)")
       

       //For partitioning*****************************
       hiveCtx.sql("SET hive.exec.dynamic.partition.mode = nonstrict")
       hiveCtx.sql("set hive.enforce.bucketing=false")
       hiveCtx.sql("set hive.enforce.sorting=false")


      // hiveCtx.sql("DROP TABLE IF EXISTS cyb3_Bucket")
       hiveCtx.sql("DROP TABLE IF EXISTS cyb3")
       hiveCtx.sql("CREATE TABLE IF NOT EXISTS Cyb3 (Year INT, Organisation STRING, Critical_Industry STRING, Organisation_size STRING, Sector STRING, Improper_network_segmentation STRING, Inappropriate_remote_access STRING, Absence_of_encryption STRING, Restructuring_after_attack STRING, Ransom_paid STRING, Number_of_users_affected INT, Attack_type STRING, Attacker	STRING, Attack_vector STRING, Names_exposed STRING,	Address_leaked STRING,	PII_exposed	STRING, Credit_card_details_leaked STRING,	SSN_taxID_leaked STRING, Subsequent_fraudulent_use_of_data STRING, Summary STRING) PARTITIONED By (Country STRING)")
       //hiveCtx.sql("CREATE TABLE IF NOT EXISTS Cyb3_Bucket (Year INT, Organisation STRING, Critical_Industry STRING, Organisation_size STRING, Sector STRING, Improper_network_segmentation STRING, Inappropriate_remote_access STRING, Absence_of_encryption STRING, Restructuring_after_attack STRING, Ransom_paid STRING, Number_of_users_affected INT, Attack_type STRING, Attacker	STRING, Attack_vector STRING, Names_exposed STRING,	Address_leaked STRING,	PII_exposed	STRING, Credit_card_details_leaked STRING,	SSN_taxID_leaked STRING, Subsequent_fraudulent_use_of_data STRING, Summary STRING) PARTITIONED By (Country STRING) CLUSTERED BY (Attacker) INTO 3 BUCKETS STORED AS SEQUENCEFILE")




       hiveCtx.sql("INSERT INTO Cyb1 SELECT * FROM temp_data")
       //hiveCtx.sql("INSERT INTO Cyb3_Bucket PARTITION(Country) SELECT Year, Organisation, Critical_Industry, Organisation_size, Sector, Country, Improper_network_segmentation, Inappropriate_remote_access, Absence_of_encryption, Restructuring_after_attack, Ransom_paid, Number_of_users_affected, Attack_type, Attacker, Attack_vector, Names_exposed STRING,	Address_leaked,	PII_exposed, Credit_card_details_leaked,	SSN_taxID_leaked, Subsequent_fraudulent_use_of_data, Summary FROM Cyb1")
       
     // hiveCtx.sql("INSERT OVERWRITE TABLE Cyb3_Bucket PARTITION(Country) SELECT Year, Organisation, Critical_Industry, Organisation_size, Sector, Country, Improper_network_segmentation, Inappropriate_remote_access, Absence_of_encryption, Restructuring_after_attack, Ransom_paid, Number_of_users_affected, Attack_type, Attacker, Attack_vector, Names_exposed STRING,	Address_leaked,	PII_exposed, Credit_card_details_leaked,	SSN_taxID_leaked, Subsequent_fraudulent_use_of_data, Summary FROM Cyb1")
       
      
        
        // To query the data1 table. When we make a query, the result set ius stored using a dataframe. In order to print to the console, 
        // we can use the .show() method.
   
    }

     def queries(connection:Connection, hiveCtx:HiveContext){
        println()
        println("****This will not take long, I promise******")
        println("***********Now running 6 queries, and formating the results....************")
        val summary = hiveCtx.sql("SELECT Year, Organisation, NUmber_of_users_affected, Attack_type  FROM Cyb1 ORDER BY Year")
        summary.show(10)
        println("***Top 50 Cyberattacks since 2004, and the number of users affected per attack***\n")
        val summary1 = hiveCtx.sql("SELECT Year, Organisation, Number_of_users_affected, Attack_type FROM Cyb1 ORDER BY Number_of_users_affected DESC")
        summary1.show(50)
        println("***Disctinct Attack Types (Four)***\n")
        val summary2 = hiveCtx.sql("SELECT distinct Attack_type from Cyb1 where Attack_type != 'NA' ")
        summary2.show(50)
        println("***Number of Attack types by attack type***\n")
        val summary3 = hiveCtx.sql("SELECT Attack_type, count(Attack_type) FROM Cyb1 where Attack_type != 'NA' GROUP BY Attack_type")
        summary3.show(50)
        println("***Top organisations with no good data encryption policy***\n")
        val summary4 = hiveCtx.sql("SELECT Year, Organisation, Country, Number_of_users_affected FROM Cyb1 WHERE Absence_of_encryption = 'Yes' ORDER By Number_of_users_affected DESC Limit 10")
        summary4.show(10)
        println("***Most Targeted sectors***\n")
        val summary5 = hiveCtx.sql("SELECT Sector, count(*) FROM Cyb1 WHERE Year > 2015 Group By Sector ORDER by count(*) DESC LIMIT 10")
        summary5.show(10)
        println("***Where stolen data get actually moneytized***\n")
        val summary6 = hiveCtx.sql("SELECT Sector, count(*) FROM Cyb1 WHERE Subsequent_fraudulent_use_of_data = 'Yes' Group By Sector ORDER by count(*) DESC LIMIT 10")
        summary6.show(10, false)
        println("***Attack vectors***\n")
        val summary7 = hiveCtx.sql("SELECT Attack_vector, count(Attack_vector) FROM Cyb1 where Attack_vector != 'NA' GROUP BY Attack_vector order By count(attack_vector) DESC")
        summary7.show(10, false)
        println("***Attack vectors per year***\n")
        val summary8 = hiveCtx.sql("SELECT Year, Attack_vector FROM Cyb1 where Attack_vector != 'NA' order By Year DESC")
        summary8.show(50, false)
        }

    def top10TargetedComp(hiveCtx:HiveContext): Unit = {
        val result = hiveCtx.sql("SELECT Year, Organisation, Country, Number_of_users_affected FROM Cyb1 WHERE Absence_of_encryption = 'Yes' ORDER By Number_of_users_affected DESC LIMIT 10")
        result.show()
        //result.write.csv("results/top10BadCompanies")
        result.write.mode("overwrite").csv("results/top10BadCompanies")
    }

    def mostTargetedSectors(hiveCtx:HiveContext): Unit = {
        val result = hiveCtx.sql("SELECT Sector, count(*) FROM Cyb1 WHERE Year > 2015 Group By Sector ORDER by count(*) DESC LIMIT 10")
        result.show()
        result.write.mode("overwrite").csv("results/mostTargetedSectors")
    }


    def sqlView(connection:Connection) {
        val statement = connection.createStatement()
        val resultSet = statement.executeQuery("SELECT * FROM Hive_Users" ) // Change query to your table
        while (resultSet.next()) {
          print(
            resultSet.getString(1) + " " + resultSet.getString(2) + " " + resultSet.getString(3) + " " + resultSet.getString(4) + " " + resultSet.getString(5) + " " + resultSet.getString(6)
          )
          println()
        }

    }

    def editUser(connection:Connection) {
        val statement = connection.createStatement()
        println("Enter user's login: ")
        var orLogin = readLine()
        println("Enter new firstname: ")
        var fname = readLine()
        println("Enter new lastname: ")
        var lname = readLine()
        var lg = s"${lname.take(3)}${fname.take(3)}"
        var login = lg.toLowerCase
        statement.executeUpdate("UPDATE Hive_Users SET FirstName= '"+ fname +"' , LastName = '"+lname+"', login = '"+login+"' where login = '"+orLogin+"' " )
        println("You've successfully updated " + fname + "'s information. User's new login is: " + login +" .") // Change query to your table

    }

    def updatePass(connection:Connection) {
        val statement = connection.createStatement()
        println("Enter your login: ")
        var orLogin = readLine()
        println("Enter new password: ")
        var pss = readLine()        
        if (pss != "" && pss.length >= 8) {
            var pass = Crypto.customHash(pss)
            statement.executeUpdate("UPDATE Hive_Users SET Password= '"+ pass +"' where login = '"+orLogin+"' " )
        } else {
             try {
                if (pss == "" || pss.length < 8) 
                    throw new BadDataEntryException
                    }
                    catch 
                    {
                        case bui: BadDataEntryException => { println("Password must be at leat 8 characters. try again please.")
                       updatePass(connection)
                    }
                } 
            }

        
    }
        

    def deleteUser(connection:Connection) {
        val statement = connection.createStatement()
        println("Enter user's login: ")
        var orLogin = readLine()
        statement.executeUpdate("DELETE from Hive_Users where login = '"+orLogin+"' " )
        println("User successfully removed") // Change query to your table

    }
    
}//P1 object ends here
