package com.full360.voltdbscala

import com.full360.voltdbscala.util.VoltTableBuilder
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{Matchers, WordSpec}
import org.voltdb.client._
import org.voltdb.{VoltTable, VoltType}

import scala.concurrent.Future
import scala.util.Try

class VoltDBSpec extends WordSpec with Matchers with ScalaFutures {
  val ROW_COUNT = 3
  val PARTITION_COUNT = 7

  case class TestObj(col1: String, col2: String)

  def voltdb = new VoltDB {}

  def voltdbMocked: VoltDB = new VoltDB {
    def testClientResponse(columnTypes: Seq[VoltType], nRows: Int)(f: (Int, Int) ⇒ AnyRef): ClientResponse = new TestClientResponse {
      override def getResults: Array[VoltTable] = Array(VoltTableBuilder.build(columnTypes, nRows)(f))
    }

    val testProcClientResponse: ClientResponse =
      testClientResponse(Seq(VoltType.STRING, VoltType.STRING), ROW_COUNT) { (row, col) ⇒ s"row${row}_col${col}" }

    override val client: Client = new TestClient {
      override def callProcedure(procName: String, parameters: AnyRef*): ClientResponse = {
        procName match {
          case "testProc" ⇒ {
            validateParams(parameters: _*)
            testProcClientResponse
          }
        }
      }

      override def callProcedure(callback: ProcedureCallback, procName: String, parameters: AnyRef*): Boolean = {
        procName match {
          case "testProc" ⇒ {
            validateParams(parameters: _*)
            callback.clientCallback(testProcClientResponse)
          }
        }
        true
      }

      override def createConnection(host: String, port: Int): Unit = {}

      def validateParams(parameters: AnyRef*): Unit = parameters shouldBe Array(123, "hello world")
    }
  }

  "VoltDB" must {
    "define an empty username" in {
      voltdb.username shouldEqual ""
    }

    "define an empty password" in {
      voltdb.password shouldEqual ""
    }

    "provide a ClientConfig instance" in {
      voltdb.config shouldBe a[org.voltdb.client.ClientConfig]
    }

    "define an execution context that defaults to global" in {
      voltdb.executionContext shouldBe scala.concurrent.ExecutionContext.global
    }

    "create a VoltDB client" in {
      voltdb.client shouldBe a[Client]
    }

    "wrap sync procedure calls" in {
      voltdbMocked.callProcedureSync("testProc", 123, "hello world") shouldBe a[ClientResponse]
    }

    "wrap async procedure calls" in {
      voltdbMocked.callProcedure("testProc", 123, "hello world") shouldBe a[Future[_]]
    }

    "call procedures synchronously parsing first result set" in {
      val result = voltdbMocked.callProcedureAndMapResultSync("testProc", 123, "hello world") { row ⇒
        TestObj(row.getString(0), row.getString(1))
      }

      result shouldBe a[Vector[_]]
      result.size shouldBe ROW_COUNT
      result(0) shouldBe TestObj("row1_col1", "row1_col2")
    }

    "call procedures asynchronously parsing first result set" in {
      val futureResult = voltdbMocked.callProcedureAndMapResult("testProc", 123, "hello world") { row ⇒
        TestObj(row.getString(0), row.getString(1))
      }

      whenReady(futureResult) { result ⇒
        result shouldBe a[Vector[_]]
        result.size shouldBe ROW_COUNT
        result(0) shouldBe TestObj("row1_col1", "row1_col2")
      }
    }

    "connect to a single VoltDB server" in {
      voltdb.connect("localhost", 1234) shouldBe a[Try[_]]
    }

    "connect to a pool of VoltDB servers" in {
      val result = voltdb.connect("host1", "host2:123")

      result(0)._1 shouldBe a[Try[_]]
      result(0)._2 shouldBe "host1"
      result(0)._3 shouldBe 21212

      result(1)._2 shouldBe "host2"
      result(1)._3 shouldBe 123
    }

    "provide a method that fails if cannot connect to a VoltDB server" which {
      "throws a java.io.IOException when it fails" in {
        an[java.io.IOException] shouldBe thrownBy(voltdb.connectOrFail("unknownhost123:54321"))
      }

      "does nothing when connection succeeds" in {
        // Test that no error is thrown
        voltdbMocked.connectOrFail("host1")
      }
    }
  }
}
