# VoltDBScala     [![Build Status](https://travis-ci.org/full360/voltdbscala.svg?branch=master)](https://travis-ci.org/full360/voltdbscala)

Scala wrapper for the VoltDB java client.

## Usage

### Basic example

```scala
import org.voltdb.client.ClientResponse
import scala.concurrent.Future

// Scala client wrapper
import com.full360.voltdbscala.Client

val client = Client()

// Synchronous example
val response: ClientResponse = client.callProcedure("SampleProc", 123, "abc")

// Asynchronous example
val responseAsync: Future[ClientResponse] = client.callProcedureAsync("SampleProc", 123, "abc")
```

Note: You can pass Option[T] as parameter to procedure calls which is translated to its value if present or null

### Utilities

#### ClientResponse utilities

```scala
import org.voltdb.client.ClientResponse
import org.voltdb.VoltTableRow
import scala.concurrent.Future
import com.full360.voltdbscala.Client

// Implicit map methods
import com.full360.voltdbscala.ClientResponseUtils.MapMethodSupport

val client = Client()

case class User(id: String, name: String)

def userFromRow(row: VoltTableRow): User = User(row.getString("id"), row.getString("name"))

// Synchronous example
val users: Seq[User] = client.callProcedure("GetUsers").map(0)(userFromRow)
val user: Option[User] = client.callProcedure("GetUser", "xyz").mapFirstRow(0)(userFromRow)

// Asynchronous example
val usersAsync: Future[Seq[User]] = client.callProcedureAsync("GetUsers").map(_.map(0)(userFromRow))
val userAsync: Future[Option[User]] = client.callProcedureAsync("GetUser", "xyz").map(_.mapFirstRow(0)(userFromRow))
```

#### VoltTableRow utilities

```scala
import org.voltdb.client.ClientResponse
import org.voltdb.VoltTableRow
import scala.concurrent.Future
import com.full360.voltdbscala.Client
import com.full360.voltdbscala.ClientResponseUtils.MapMethodSupport

// VoltTableRow implicit methods
import com.full360.voltdbscala.VoltTableRowUtils.{ ToOptionMethodSupport, GetLongAsBooleanMethodSupport }

val client = Client()

case class User(id: String, name: String, active: Boolean, githubAccount: Option[String])

def userFromRow(row: VoltTableRow): User =
  User(
    id = row.getString("id"),
    name = row.getString("name"),
    
    // returns true if value is 1
    active = row.getLongAsBoolean("active"),
    
    // returns None if row.wasNull is true or Some(value) otherwise  
    githubAccount = row.toOption(row.getString("github_account"))
  )

// Synchronous example
val users: Seq[User] = client.callProcedure("GetUsers").map(0)(userFromRow)
```
