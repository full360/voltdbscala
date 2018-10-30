package com.full360.voltdbscala

import java.io.File

import scala.concurrent.{ExecutionContext, Future, Promise}
import org.voltdb.{client ⇒ jclient}
import org.voltdb.client.VoltBulkLoader.VoltBulkLoader
import jclient._

object Client {

  /**
   * <p>Recommended method for creating a client. Using a ClientConfig object ensures
   * that a client application is isolated from changes to the configuration options.
   * Authentication credentials are provided at construction time with this method
   * instead of when invoking createConnection.</p>
   *
   * @param config ClientConfig object specifying what type of client to create
   * @return a configured com.full360.voltdbscala.Client
   */
  def apply(config: Option[ClientConfig] = None): Client = {
    val jc = config match {
      case Some(c) ⇒ ClientFactory.createClient(c)
      case None    ⇒ ClientFactory.createClient()
    }

    new Client {
      override def javaClient = jc
    }
  }

  /**
   * Implicitly org.voltdb.client.Client converter
   * @param c scala client instance
   * @return java client instance
   */
  implicit def implicitConverter(c: Client): jclient.Client = c.javaClient
}

trait Client {

  import ClientUtils._

  /**
   * VoltDB java client.
   */
  def javaClient: jclient.Client

  /**
   * <p>Synchronously invokes a procedure. Blocks until a result is available.
   * A [[org.voltdb.client.ProcCallException ProcCallException]]
   * is thrown if the response is anything other then success.</p>
   *
   * @see [[org.voltdb.client.Client#callProcedure(String, Object...) Client.callProcedure]]
   *
   * @param procName class name (not qualified by package) of the procedure to execute.
   * @param parameters vararg list of procedure's parameter values.
   * @return [[org.voltdb.client.ClientResponse ClientResponse]] instance of procedure call results.
   */
  def callProcedure(procName: String, parameters: Any*): ClientResponse =
    javaClient.callProcedure(procName, paramsToJavaObjects(parameters: _*): _*)

  /**
   * <p>Asynchronously invokes a procedure. If there is backpressure
   * this call will block until the invocation is queued. If configureBlocking(false) is invoked
   * then it will return immediately. The resulting Future will contain a
   * [[com.full360.voltdbscala.ProcedureNotQueuedException ProcCallException]] if queueing did not take place.</p>
   *
   * @see [[org.voltdb.client.Client#callProcedure(org.voltdb.client.ProcedureCallback, String, Object...) Client.callProcedure]]
   *
   * @param procName class name (not qualified by package) of the procedure to execute.
   * @param parameters vararg list of procedure's parameter values.
   * @return a scala Future holding an instance of a [[org.voltdb.client.ClientResponse ClientResponse]] or exception.
   */
  def callProcedureAsync(procName: String, parameters: Any*)(implicit ec: ExecutionContext): Future[ClientResponse] =
    handleAsyncProcCall[ClientResponse] { promise ⇒
      val cb = procedureCallback(promise.success(_))
      javaClient.callProcedure(cb, procName, paramsToJavaObjects(parameters: _*): _*)
    }

  /**
   * <p>Asynchronously invokes UpdateClasses procedure. Does not
   * guarantee that the invocation is actually queued. If there is
   * backpressure on all connections to the cluster then the invocation will
   * not be queued. The resulting Future will contain a
   * [[com.full360.voltdbscala.ProcedureNotQueuedException ProcCallException]] if queueing did not take place.</p>
   *
   * <p>This method is a convenience method that is equivalent to reading a jarfile containing
   * to be added/updated into a byte array in Java code, then calling
   * [[org.voltdb.client.Client#callProcedure(String, Object...) Client.callProcedure]]
   * with "@UpdateClasses" as the procedure name, followed by the bytes of the jarfile
   * and a string containing a comma-separates list of classes to delete from the catalog.</p>
   *
   * @see [[org.voltdb.client.Client#updateClasses(org.voltdb.client.ProcedureCallback, java.io.File, String) Client.updateClasses]]
   *
   * @param jarPath path to the jar file containing new/updated classes.  May be null.
   * @param classesToDelete comma-separated list of classes to delete.  May be null.
   * @return a scala Future holding an instance of a [[org.voltdb.client.ClientResponse ClientResponse]] or exception.
   */
  def updateClassesAsync(jarPath: File, classesToDelete: String)(implicit ec: ExecutionContext): Future[ClientResponse] =
    handleAsyncProcCall[ClientResponse] { promise ⇒
      val cb = procedureCallback(promise.success(_))
      javaClient.updateClasses(cb, jarPath, classesToDelete)
    }

  /**
   * <p>Asynchronously invokes UpdateApplicationCatalog procedure. Does not
   * guarantee that the invocation is actually queued. If there is
   * backpressure on all connections to the cluster then the invocation will
   * not be queued. The resulting Future will contain a
   * [[com.full360.voltdbscala.ProcedureNotQueuedException ProcCallException]] if queueing did not take place.</p>
   *
   * @param catalogPath    Path to the catalog jar file.
   * @param deploymentPath Path to the deployment file.
   * @return a scala Future holding an instance of a [[org.voltdb.client.ClientResponse ClientResponse]] or exception.
   */
  def updateApplicationCatalogAsync(catalogPath: String, deploymentPath: String)(implicit ec: ExecutionContext): Future[ClientResponse] =
    handleAsyncProcCall[ClientResponse] { promise ⇒
      val cb = procedureCallback(promise.success(_))
      javaClient.callProcedure(cb, "@UpdateApplicationCatalog", catalogPath, deploymentPath)
    }

  /**
   * <p>Creates a new instance of a VoltBulkLoader that is bound to this Client.
   * Multiple instances of a VoltBulkLoader created by a single Client will share some
   * resources, particularly if they are inserting into the same table.</p>
   *
   * @see [[org.voltdb.client.Client#getNewBulkLoader Client.getNewBulkLoader]]
   *
   * @param tableName    Name of table that bulk inserts are to be applied to.
   * @param maxBatchSize Batch size to collect for the table before pushing a bulk insert.
   * @param upsert       set to true if want upsert instead of insert
   * @param f            Function invoked by the BulkLoaderFailureCallBack used for notification of failed inserts.
   * @param f2           Function invoked by BulkLoaderSuccessCallback used for notifications on successful load operations
   * @return instance of VoltBulkLoader
   * @throws Exception if tableName can't be found in the catalog.
   */
  def getNewBulkLoader(tableName: String, maxBatchSize: Int, upsert: Boolean = false)(f: (Any, Seq[AnyRef], ClientResponse) ⇒ Unit)(f2: (Any, ClientResponse) ⇒ Unit): VoltBulkLoader =
    javaClient.getNewBulkLoader(tableName, maxBatchSize, upsert, bulkLoaderFailureCallBack(f), bulkLoaderSuccessCallBack(f2))
  /**
   * The method uses system procedure <strong>@GetPartitionKeys</strong> to get a set of partition
   * values and then execute the stored procedure one partition at a time, and return an
   * aggregated response. Blocks until results are available.
   *
   * @see [[org.voltdb.client.Client#callAllPartitionProcedure(String, Object...) Client.callAllPartitionProcedure]]
   *
   * @param procName <code>class</code> name (not qualified by package) of the partitioned java procedure to execute.
   * @param parameters vararg list of procedure's parameter values.
   * @return [[org.voltdb.client.ClientResponseWithPartitionKey ClientResponseWithPartitionKey]] instances of procedure call results.
   */
  def callAllPartitionProcedure(procName: String, parameters: Any*): Seq[ClientResponseWithPartitionKey] =
    javaClient.callAllPartitionProcedure(procName, paramsToJavaObjects(parameters: _*): _*).toList

  /**
   * The method uses system procedure <strong>@GetPartitionKeys</strong> to get a set of partition values
   * which are used to reach every partition, and then asynchronously executes the stored procedure across partitions.
   * If there is backpressure, a call to a partition will block until the invocation on the partition is queued.
   * If configureBlocking(false) is invoked then the execution on the partition will return immediately.
   * The resulting Future will contain a [[com.full360.voltdbscala.ProcedureNotQueuedException ProcCallException]]
   * if queueing did not take place.
   *
   * @see [[org.voltdb.client.Client#callAllPartitionProcedure(org.voltdb.client.AllPartitionProcedureCallback, String, Object...) Client.callAllPartitionProcedure]]
   *
   * @param procName class name (not qualified by package) of the partitioned java procedure to execute.
   * @param parameters        vararg list of procedure's parameter values.
   * @return a scala Future holding an sequence of
   *         [[org.voltdb.client.ClientResponseWithPartitionKey ClientResponseWithPartitionKey]] or exception.
   */
  def callAllPartitionProcedureAsync(procName: String, parameters: Any*)(implicit ec: ExecutionContext): Future[Seq[ClientResponseWithPartitionKey]] =
    handleAsyncProcCall[Seq[ClientResponseWithPartitionKey]] { promise ⇒
      val cb = allPartitionProcedureCallback(promise.success(_))
      javaClient.callAllPartitionProcedure(cb, procName, paramsToJavaObjects(parameters: _*): _*)
    }

  /**
   * <p>Synchronously invoke a procedure with timeout. Blocks until a result is available.
   * A [[org.voltdb.client.ProcCallException ProcCallException]] is thrown if the response is anything other then success.</p>
   *
   * <p>WARNING: Use of a queryTimeout value that is greater than the global timeout value for your VoltDB configuration
   * will temporarily override that safeguard. Currently, non-privileged users (requiring only SQLREAD permissions)
   * can invoke this method, potentially degrading system performance with an uncontrolled long-running procedure.</p>
   *
   * @see [[org.voltdb.client.Client#callProcedureWithTimeout(int, String, Object...) Client.callProcedureWithTimeout]]
   *
   * @param queryTimeout query batch timeout setting in milliseconds of queries in a batch for read only procedures.
   * @param procName     <code>class</code> name (not qualified by package) of the procedure to execute.
   * @param parameters   vararg list of procedure's parameter values.
   * @return [[org.voltdb.client.ClientResponse ClientResponse]] instance of procedure call results.
   */
  def callProcedureWithTimeout(queryTimeout: Int, procName: String, parameters: Any*): ClientResponse =
    javaClient.callProcedureWithTimeout(queryTimeout, procName, paramsToJavaObjects(parameters: _*): _*)

  /**
   * <p>Asynchronously invoke a replicated procedure with timeout, by providing a callback that will be invoked by
   * the single thread backing the client instance when the procedure invocation receives a response. If there is backpressure
   * this call will block until the invocation is queued. If configureBlocking(false) is invoked
   * then it will return immediately. The resulting Future will contain a
   * [[com.full360.voltdbscala.ProcedureNotQueuedException ProcCallException]] if queueing did not take place.</p>
   *
   * <p>WARNING: Use of a queryTimeout value that is greater than the global timeout value for your VoltDB configuration
   * will temporarily override that safeguard. Currently, non-privileged users (requiring only SQLREAD permissions)
   * can invoke this method, potentially degrading system performance with an uncontrolled long-running procedure.</p>
   *
   * @param queryTimeout query batch timeout setting in milliseconds of queries in a batch for read only procedures.
   * @param procName     class name (not qualified by package) of the procedure to execute.
   * @param parameters   vararg list of procedure's parameter values.
   * @return a scala Future holding an instance of a [[org.voltdb.client.ClientResponse ClientResponse]] or exception.
   */
  def callProcedureWithTimeoutAsync(queryTimeout: Int, procName: String, parameters: Any*)(implicit ec: ExecutionContext): Future[ClientResponse] =
    handleAsyncProcCall[ClientResponse] { promise ⇒
      val cb = procedureCallback(promise.success(_))
      javaClient.callProcedureWithTimeout(cb, queryTimeout, procName, paramsToJavaObjects(parameters: _*): _*)
    }

  /**
   * <p>Tell whether Client has turned on the auto-reconnect feature. If it is on,
   * Client would pause instead of stop when all connections to the server are lost,
   * and would resume after the connection is restored.
   *
   * @return true if the client wants to use auto-reconnect feature.</p>
   */
  def isAutoReconnectEnabled: Boolean = javaClient.isAutoReconnectEnabled

  /**
   * <p>Write a single line of comma separated values to the file specified.
   * Used mainly for collecting results from benchmarks.</p>
   *
   * <p>The format of this output is subject to change between versions</p>
   *
   * <p>Format:
   * <ol>
   * <li>Timestamp (ms) of creation of the given {@link ClientStats} instance, stats.</li>
   * <li>Duration from first procedure call within the given {@link ClientStats} instance
   * until this call in ms.</li>
   * <li>1-percentile round trip latency estimate in ms.</li>
   * <li>Max measure round trip latency in ms.</li>
   * <li>95-percentile round trip latency estimate in ms.</li>
   * <li>99-percentile round trip latency estimate in ms.</li>
   * <li>99.9-percentile round trip latency estimate in ms.</li>
   * <li>99.99-percentile round trip latency estimate in ms.</li>
   * <li>99.999-percentile round trip latency estimate in ms.</li>
   * </ol>
   *
   * @param stats { @link ClientStats} instance with relevant stats.
   * @param path Path to write to, passed to { @link FileWriter#FileWriter(String)}.
   */
  def writeSummaryCSV(stats: ClientStats, path: String): Unit = {
    javaClient.writeSummaryCSV(stats, path)
  }

  /**
   * <p>Write a single line of comma separated values to the file specified.
   * Used mainly for collecting results from benchmarks.</p>
   *
   * <p>The format of this output is subject to change between versions</p>
   *
   * <p>Format:
   * <ol>
   * <li>Timestamp (ms) of creation of the given {@link ClientStats} instance, stats.</li>
   * <li>Duration from first procedure call within the given {@link ClientStats} instance
   * until this call in ms.</li>
   * <li>1-percentile round trip latency estimate in ms.</li>
   * <li>Max measure round trip latency in ms.</li>
   * <li>95-percentile round trip latency estimate in ms.</li>
   * <li>99-percentile round trip latency estimate in ms.</li>
   * <li>99.9-percentile round trip latency estimate in ms.</li>
   * <li>99.99-percentile round trip latency estimate in ms.</li>
   * <li>99.999-percentile round trip latency estimate in ms.</li>
   * </ol>
   *
   * @param statsRowName give the client stats row an identifiable name.
   * @param stats        { @link ClientStats} instance with relevant stats.
   * @param path Path to write to, passed to { @link FileWriter#FileWriter(String)}.
   */
  def writeSummaryCSV(statsRowName: String, stats: ClientStats, path: String): Unit = {
    javaClient.writeSummaryCSV(statsRowName, stats, path)
  }

  /**
   * Helper method to simplify async proc calls
   * @param f Function where the async proc call takes place
   * @tparam T Type of the resulting Future
   * @return a scala Future
   */
  protected def handleAsyncProcCall[T](f: Promise[T] ⇒ Boolean)(implicit ec: ExecutionContext): Future[T] = {
    val promise = Promise[T]()
    val future = promise.future
    val isQueued = f(promise)

    if (!isQueued) {
      promise.failure(ProcedureNotQueuedException(s"Procedure call could not be queued"))
    }

    future
  }
}
