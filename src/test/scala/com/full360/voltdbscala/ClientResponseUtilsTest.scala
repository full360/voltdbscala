package com.full360.voltdbscala

import com.full360.voltdbscala.util.VoltTableBuilder
import org.scalatest.{ Matchers, WordSpec }
import org.voltdb.{ VoltTable, VoltTableRow, VoltType }
import org.voltdb.client.{ ClientResponse, TestClientResponse }

class ClientResponseUtilsTest extends WordSpec with Matchers {
  case class TestObj(col1: String, col2: String)

  def createClientResponse(nRows: Int, status: Byte = ClientResponse.SUCCESS, statusString: String = "SUCCESS"): ClientResponse = new TestClientResponse {
    val f = (row: Int, col: Int) ⇒ s"r${row}:c${col}"
    override def getResults: Array[VoltTable] = Array(VoltTableBuilder.build(nRows, VoltType.STRING, VoltType.STRING)(f))
    override def getStatus: Byte = status
    override def getStatusString: String = statusString
  }

  def parseTestRow(row: VoltTableRow) = TestObj(row.getString(0), row.getString(1))

  "ClientResponseUtils.mapClientResponseResult maps results into Seq[T]" in {
    val response = createClientResponse(3)
    val result = ClientResponseUtils.mapClientResponseResult(response, 0)(parseTestRow)

    result shouldBe Seq(
      TestObj("r1:c1", "r1:c2"),
      TestObj("r2:c1", "r2:c2"),
      TestObj("r3:c1", "r3:c2"))
  }

  "ClientResponseUtils.mapClientResponseResult throws a ClientResponseStatusException if client response is not valid" in {
    val response = createClientResponse(3, ClientResponse.RESPONSE_UNKNOWN, "RESPONSE_UNKNOWN")

    intercept[ClientResponseStatusException] {
      ClientResponseUtils.mapClientResponseResult(response, 0)(parseTestRow)
    }
  }

  "ClientResponseUtils.mapFirstRowFromClientResponseResult maps first row into Some[T]" in {
    val response = createClientResponse(3)
    val result = ClientResponseUtils.mapFirstRowFromClientResponseResult(response, 0)(parseTestRow)

    result shouldBe Some(TestObj("r1:c1", "r1:c2"))
  }

  "ClientResponseUtils.mapFirstRowFromClientResponseResult maps first row into None if no records where found" in {
    val response = createClientResponse(1, ClientResponse.CONNECTION_TIMEOUT, "CONNECTION_TIMEOUT")

    intercept[ClientResponseStatusException] {
      ClientResponseUtils.mapFirstRowFromClientResponseResult(response, 0)(parseTestRow)
    }
  }

  "ClientResponseUtils.mapFirstRowFromClientResponseResult throws a ClientResponseStatusException if client response is not valid" in {
    val response = createClientResponse(0)
    val result = ClientResponseUtils.mapFirstRowFromClientResponseResult(response, 0)(parseTestRow)

    result shouldBe None
  }

  "ClientResponseUtils.MapMethodSupport adds 'map' method to ClientResponse instances" in {
    import ClientResponseUtils.MapMethodSupport

    val response = createClientResponse(1)
    response.map(0)(parseTestRow) shouldBe Seq(TestObj("r1:c1", "r1:c2"))
  }

  "ClientResponseUtils.ValidateStatusMethodSupport adds 'validateStatus' method to ClientResponse instances" in {
    import ClientResponseUtils.ValidateStatusMethodSupport

    val response = createClientResponse(0, ClientResponse.CONNECTION_LOST, "CONNECTION_LOST")

    intercept[ClientResponseStatusException] {
      response.validateStatus
    }
  }
}
