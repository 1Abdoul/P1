import java.security.MessageDigest
import java.math.BigInteger

object Crypto {

  def customHash(s: String): String = {
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(s.getBytes)
        val bigInt = new BigInteger(1,digest)
        val hashedString = bigInt.toString(16)
        hashedString
    }


}


