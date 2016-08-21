# VoltDBScala

Contains a friendly scala wrapper for the VoltDB java client. It focuses on async procedure calls, although you could also make use of a sync approach if you like to.

# Example

Here's an quick example of how to implement the VoltDB wrapper

```scala
import com.full360.voltdbscala.VoltDB
import org.voltdb.client.ClientConfig
import org.voltdb.VoltTableRow

// Domain class
case class User(id: Long, name: String)

class MyProjectVoltDB(override val username: String, override val password: String) extends VoltDB {

  import com.full360.voltdbscala.util.Util._
  
  // Example of overriding default configuration
  override def config: ClientConfig = {
    val config = super.config
    config.setReconnectOnConnectionLoss(true)
    config
  }
  
  // Async procedure wrapper
  def getUserById(id: Long): Future[Option[User]] = {
    def parser(row: VoltTableRow) = User(row.getLong(0), row.getString(1))
    
    /**
     * Parameters:
     * "GetUserById": SP name
     * id: SP parameter
     * parser: function that maps a VoltTableRow to a domain instance
     * toSingleResult: function provided by com.full360.voltdbscala.util.Util that returns scala Option
     * based on the result of the SP call
     */
    callProcedureAndMapResult("GetUserById")(id)(parser).map(toSingleResult)
  }
}
```

```scala

val voltdb = new MyProjectVoltDB("user", "password")

// Connect to multiple nodes. This will attempt to connecto to host1:1234 and host2:21212
// It returns an Seq of Try[Unit] to reason about failing attempts
voltdb.connect("host1:1234", "host2") 

// If you want an exception to be thrown in case one the attempts failed, use this instead
voltdb.connectOrFail("host1:1234", "host2") 

// Calling SP wrappers
val futureUser: Future[Option[User]] = voltdb.getUserById(12345)
```