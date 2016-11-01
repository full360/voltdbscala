package com.full360.voltdbscala

import org.voltdb.client.VoltBulkLoader.BulkLoaderFailureCallBack
import org.voltdb.client.{AllPartitionProcedureCallback, ClientResponse, ClientResponseWithPartitionKey, ProcedureCallback, Client ⇒ JavaClient}

object ClientUtils {

  /**
   * Helper method to sanitize scala varargs to be passed as java varargs to procedure calls
   * @param params list of scala parameters
   * @return Seq[AnyRef] sequence of sanitized parameters
   */
  def paramsToJavaObjects(params: Any*) = params.map { param ⇒
    val value = param match {
      case None    ⇒ null
      case Some(v) ⇒ v
      case _       ⇒ param
    }

    value.asInstanceOf[AnyRef]
  }

  /**
   * Extracts hostname and port from address. If the address does not specify a port, the default port will be used
   * @param address a string in the form hostname:port or hostname
   * @return a Tuple containing the host and port extracted from the given address
   */
  def hostAndPortFromAddress(address: String): (String, Int) = {
    val s = address.split(':')

    s.length match {
      case 1 ⇒ (address, JavaClient.VOLTDB_SERVER_PORT)
      case 2 ⇒ (s(0), s(1).toInt)
      case _ ⇒ ("", 0)
    }
  }

  def procedureCallback(cb: ClientResponse ⇒ Unit): ProcedureCallback = new ProcedureCallback {
    override def clientCallback(clientResponse: ClientResponse): Unit = cb(clientResponse)
  }

  def bulkLoaderFailureCallBack(f: (Any, Seq[AnyRef], ClientResponse) ⇒ Unit): BulkLoaderFailureCallBack = new BulkLoaderFailureCallBack {
    override def failureCallback(rowHandle: Any, fieldList: Array[AnyRef], response: ClientResponse): Unit = f(rowHandle, fieldList.toList, response)
  }

  def allPartitionProcedureCallback(cb: Seq[ClientResponseWithPartitionKey] ⇒ Unit): AllPartitionProcedureCallback = new AllPartitionProcedureCallback {
    override def clientCallback(responses: Array[ClientResponseWithPartitionKey]): Unit = cb(responses.toList)
  }
}
