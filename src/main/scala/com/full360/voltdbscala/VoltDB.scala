package com.full360.voltdbscala

import com.full360.voltdbscala.util.Util._
import org.voltdb.client.{ Client, ClientConfig, ClientFactory, ClientResponse, ProcedureCallback }
import org.voltdb.VoltTableRow
import scala.concurrent.{ Future, Promise }
import scala.util.{ Failure, Success, Try }
import scala.concurrent.ExecutionContext

object VoltDB {
  val DEFAULT_PORT = 21212
}

trait VoltDB {
  /**
   * Username to connect to VoltDB
   */
  def username: String = ""

  /**
   * Password to connect to VoltDB
   */
  def password: String = ""

  /**
   * VoltDB configuration object required by client
   * @return the ClientConfig instance created
   */
  def config: ClientConfig = new ClientConfig(username, password)

  /**
   * Execution context used by async procedure calls
   */
  implicit val executionContext: ExecutionContext = ExecutionContext.global

  /**
   * Creates VoltDB client once when demanded
   * @return the Client created with provided configuration in scope
   */
  val client: Client = ClientFactory.createClient(config)

  /**
   * Calls a stored procedure synchronously. Wraps client calls in a scala friendly fashion
   * @param procedureName the name of the procedure to call
   * @param params the parameters to send to the stored procedure
   * @return the ClientResponse instance of the procedure call
   */
  def callProcedureSync(procedureName: String)(params: Any*): ClientResponse =
    client.callProcedure(procedureName, paramsToJavaObjects(params: _*): _*)

  /**
   * Calls a stored procedure synchronously and parses the VoltTable at the index specified
   * in the result array
   * @param procedureName the name of the procedure to call
   * @param params the parameters to send to the stored procedure
   * @param f a function responsible for parsing a single row
   * @tparam T the type the rows are parsed to
   * @return an immutable Vector of given type containing the parsed rows
   */
  def callProcedureAndMapResultSync[T](procedureName: String, resultIndex: Int = 0)(params: Any*)(f: VoltTableRow ⇒ T): Vector[T] = {
    val cr = client.callProcedure(procedureName, paramsToJavaObjects(params: _*): _*)
    mapClientResponse[T](cr, resultIndex)(f)
  }

  /**
   * Calls a stored procedure
   * @param procedureName the name of the procedure to call
   * @param params the parameters to send to the stored procedure
   * @return a Future that eventually will hold the ClientResponse
   */
  def callProcedure(procedureName: String)(params: Any*): Future[ClientResponse] = {
    val promise = Promise[ClientResponse]()
    val future = promise.future

    Future {
      client.callProcedure(
        procedureCallback(promise.success(_)),
        procedureName,
        paramsToJavaObjects(params: _*): _*
      )
    }

    future
  }

  /**
   * Calls a stored procedure and parses the first VoltTable in the result array
   * @param procedureName the name of the procedure to call
   * @param params the parameters to send to the stored procedure
   * @return a Future that eventually will hold an immutable Vector of given type containing the parsed rows
   */
  def callProcedureAndMapResult[T](procedureName: String, resultIndex: Int = 0)(params: Any*)(f: VoltTableRow ⇒ T): Future[Vector[T]] =
    callProcedure(procedureName)(paramsToJavaObjects(params: _*): _*).map(mapClientResponse[T](_, resultIndex)(f))

  /**
   * Connects to servers passed in. If it fails connecting to any address it throws an exception
   * @param addresses
   */
  def connectOrFail(addresses: String*): Unit = {
    import java.io.IOException

    connect(addresses: _*).foreach {
      case (attempt, host, port) ⇒ attempt match {
        case Success(_) ⇒
        case Failure(e) ⇒ e match {
          case e: IOException ⇒ throw new IOException(s"${e.getMessage} -> ${host}:${port}")
        }
      }
    }
  }

  /**
   * Connects to servers passed in
   * @param addresses a Seq of server addresses to connect to. Eg Seq("localhost") or Seq("localhost:21212")*
   * @return a Seq of tuples containing information about the connection attempt, hostname and port
   */
  def connect(addresses: String*): Seq[(Try[Unit], String, Int)] = addresses.map { address: String ⇒
    val (host, port) = hostAndPortFromAddress(address)
    (connect(host, port), host, port)
  }

  /**
   * Connects to a single server
   * @param hostname the host to connect to
   * @param port the port to to connect to
   */
  def connect(hostname: String, port: Int): Try[Unit] =
    Try(client.createConnection(hostname, port))

  /**
   * Creates a ProcedureCallback instance for async procedure calls
   * @param cb the function the callback will call passing the ClientResponse
   * @return the ProcedureCallback instance created
   */
  private def procedureCallback(cb: ClientResponse ⇒ Unit): ProcedureCallback = new ProcedureCallback {
    override def clientCallback(clientResponse: ClientResponse): Unit = cb(clientResponse)
  }
}
