import java.sql.Connection

object Employee {
    

class Admin() {
    private var FirstName = ""
    private var LastName = ""
    private var Login = ""
    private var Password = ""
    private var Role = ""
    var RoleArray = Array("Admin", "User")

    def addUser(connection:Connection) {
        val statement = connection.createStatement()
        println("Adding a new user")
        this.setLogin()
        this.setPassword()
        statement.executeUpdate("INSERT INTO HIVE_USERS (FirstName, LastName, Role, Login, Password) VALUES ('"+this.FirstName+"'  , '" + this.LastName + "' , '" + this.Role + "', '" + this.Login + "', '" + this.Password + "');")
        println("ResultSet:")
       

    }
    
    def setLastName() { 
        println("Enter Lastname: ")
        var lname = readLine()
        if (lname != "" && lname.length > 3) {
            this.LastName = lname
        }else {
            try {
                if (lname == "" || lname.length < 3) 
                    throw new BadDataEntryException
                    }
                    catch 
                    {
                        case bui: BadDataEntryException => { println("Lastname must be at leat a 3 character string. try again please.")
                        setLastName()
                    }
                }
            }
    }

    def setFirstName() { 
        println("Enter Firstname: ")
        var fname = readLine()
        if (fname != "" && fname.length > 3) {
            this.FirstName = fname
        }else {
            try {
                if (fname == "" || fname.length < 3) 
                    throw new BadDataEntryException
                    }
                    catch 
                    {
                        case bui: BadDataEntryException => { println("Firstname must be at leat a 3 character string. try again please.")
                        setFirstName()
                    }
                }
            }
    }

    def setRole() { 
        println("Enter role: ")
        var role = readLine()
        if (this.RoleArray.contains(role)) {
            this.Role = role
        }else {
            try {
                if (this.RoleArray.contains(role) == false) 
                    throw new BadDataEntryException
                    }
                    catch 
                    {
                        case bui: BadDataEntryException => { println("Not a valid role for this department. try again please.")
                        setRole()
                    }
                }
            }
    }
        



    def getFirstName(): String = {
        return this.FirstName
    }

    def getLastName(): String = {
        return this.LastName
    }

    def setPassword() {
        println("Enter new password: ")
        val pss = readLine()
        if (pss != "" && pss.length >= 8) {
            this.Password = pss
            this.Password = Crypto.customHash(pss)
        } else {
             try {
                if (pss == "" || pss.length < 8) 
                    throw new BadDataEntryException
                    }
                    catch 
                    {
                        case bui: BadDataEntryException => { println("Password must be at leat 8 characters. try again please.")
                        setPassword()
                    }
                }

        }
        
    }

      def setLogin() {
          this.setFirstName()
          this.setLastName()
          this.setRole()
          var lg = s"${this.getLastName().take(3)}${this.getFirstName().take(3)}"
          this.Login = lg.toLowerCase
    }

    def getLogin(): String = {
        return this.Login
    }

    def getPassword (): String = {
        return this.Password
    }

    def getRole (): String = {
        return this.Role
    }

}

class user {
    private var Password = ""
    private var Role = ""
     def setPassword() {
        println("Enter new password: ")
        val pss = readLine()
        if (pss != "" && pss.length >= 8) {
            this.Password = pss
            this.Password = Crypto.customHash(pss)
        } else {
             try {
                if (pss == "" || pss.length < 8) 
                    throw new BadDataEntryException
                    }
                    catch 
                    {
                        case bui: BadDataEntryException => { println("Password must be at leat 8 characters. try again please.")
                        setPassword()
                    }
                }

        }
        
    }

    def getPassword (): String = {
        return this.Password
    }


}



} //UsersData object ends here