package org.voltdb.client

import java.io.File
import java.net.InetSocketAddress
import java.util.List
import VoltBulkLoader._

trait TestClient extends Client {

  override def createConnection(host: String): Unit = ???

  override def createConnection(host: String, port: Int): Unit = ???

  override def getBuildString: String = ???

  override def getThroughputAndOutstandingTxnLimits: Array[Int] = ???

  override def callProcedure(procName: String, parameters: AnyRef*): ClientResponse = ???

  override def callProcedure(callback: ProcedureCallback, procName: String, parameters: AnyRef*): Boolean = ???

  override def callProcedure(callback: ProcedureCallback, expectedSerializedSize: Int, procName: String, parameters: AnyRef*): Boolean = ???

  override def updateClasses(jarPath: File, classesToDelete: String): ClientResponse = ???

  override def updateClasses(callback: ProcedureCallback, jarPath: File, classesToDelete: String): Boolean = ???

  override def createStatsContext(): ClientStatsContext = ???

  override def drain(): Unit = ???

  override def blocking(): Boolean = ???

  override def updateApplicationCatalog(catalogPath: File, deploymentPath: File): ClientResponse = ???

  override def updateApplicationCatalog(callback: ProcedureCallback, catalogPath: File, deploymentPath: File): Boolean = ???

  override def getNewBulkLoader(tableName: String, maxBatchSize: Int, upsert: Boolean, blfcb: BulkLoaderFailureCallBack): VoltBulkLoader = ???

  override def getNewBulkLoader(tableName: String, maxBatchSize: Int, blfcb: BulkLoaderFailureCallBack): VoltBulkLoader = ???

  override def calculateInvocationSerializedSize(procName: String, parameters: AnyRef*): Int = ???

  override def backpressureBarrier(): Unit = ???

  override def configureBlocking(blocking: Boolean): Unit = ???

  override def getInstanceId: Array[AnyRef] = ???

  override def callProcedureWithTimeout(queryTimeout: Int, procName: String, parameters: AnyRef*): ClientResponse = ???

  override def callProcedureWithTimeout(callback: ProcedureCallback, queryTimeout: Int, procName: String, parameters: AnyRef*): Boolean = ???

  override def close(): Unit = ???

  override def getConnectedHostList: List[InetSocketAddress] = ???

  override def writeSummaryCSV(stats: ClientStats, path: String): Unit = ???

  override def callAllPartitionProcedure(procedureName: String, params: AnyRef*): Array[ClientResponseWithPartitionKey] = ???

  override def callAllPartitionProcedure(callback: AllPartitionProcedureCallback, procedureName: String, params: AnyRef*): Boolean = ???
}
