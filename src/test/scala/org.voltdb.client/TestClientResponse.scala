package org.voltdb.client

import org.voltdb.VoltTable

trait TestClientResponse extends ClientResponse {
  override def getAppStatusString: String = null

  override def getClusterRoundtrip: Int = 0

  override def getClientRoundtripNanos: Long = 0

  override def getStatus: Byte = ClientResponse.SUCCESS

  override def getAppStatus: Byte = 0

  override def getClientRoundtrip: Int = 0

  override def getResults: Array[VoltTable] = ???

  override def getStatusString: String = null
}
