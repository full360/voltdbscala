package com.full360.voltdbscala

import java.io.File

import org.mockito.Mockito._
import org.mockito.invocation.InvocationOnMock
import org.mockito.stubbing.Answer
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{Matchers, WordSpec}
import org.voltdb.VoltTable
import org.voltdb.client.VoltBulkLoader.BulkLoaderFailureCallBack
import org.voltdb.client._

import scala.concurrent.ExecutionContext

class ClientTest extends WordSpec with Matchers with ScalaFutures with MockitoSugar {

  implicit val ec = ExecutionContext.global

  def newClient(jClient: org.voltdb.client.Client = mock[org.voltdb.client.Client]) = new Client {
    override def javaClient = jClient
    override implicit val executionContext: ExecutionContext = ec
  }

  def emptyClientResponse = new TestClientResponse { override def getResults: Array[VoltTable] = Array() }

  "Client" must {
    "respond to #javaClient" in {
      newClient().javaClient shouldBe a[org.voltdb.client.Client]
    }

    "respond to #executionContext" in {
      newClient().executionContext shouldBe ExecutionContext.global
    }

    "respond to #callProcedure" in {
      val client = newClient()
      val response = emptyClientResponse

      when(client.javaClient.callProcedure("proc", 123.asInstanceOf[AnyRef], "arg".asInstanceOf[AnyRef]))
        .thenReturn(response)

      client.callProcedure("proc", 123, "arg") shouldBe response
    }

    "respond to #callProcedureAsync" in {
      val response = emptyClientResponse
      val client = newClient(new TestClient {
        override def callProcedure(cb: ProcedureCallback, pn: String, p: AnyRef*) = {
          cb.clientCallback(response)
          true
        }
      })

      whenReady(client.callProcedureAsync("proc", 123, "arg")) { result ⇒ result shouldBe response }
    }

    "return a Future wrapping a ProcedureNotQueuedException when calling #callProcedureAsync was not queued" in {
      val client = newClient(new TestClient {
        override def callProcedure(cb: ProcedureCallback, pn: String, p: AnyRef*) = false
      })

      whenReady(client.callProcedureAsync("proc", 123, "arg").failed) { e ⇒
        e shouldBe a[ProcedureNotQueuedException]
      }
    }

    "respond to #updateClasses" in {
      val client = newClient()
      val file = new File("jarfile")
      val response = emptyClientResponse

      when(client.javaClient.updateClasses(file, "deleteClasses"))
        .thenReturn(response)

      client.updateClasses(file, "deleteClasses") shouldBe response
    }

    "respond to #updateClassesAsync" in {
      val file = new File("jarfile")
      val response = emptyClientResponse
      val client = newClient(new TestClient {
        override def updateClasses(cb: ProcedureCallback, jp: File, c: String) = {
          cb.clientCallback(response)
          true
        }
      })

      whenReady(client.updateClassesAsync(file, "deleteClasses")) { result ⇒ result shouldBe response }
    }

    "return a Future wrapping a ProcedureNotQueuedException when calling #updateClassesAsync was not queued" in {
      val file = new File("jarfile")
      val client = newClient(new TestClient {
        override def updateClasses(cb: ProcedureCallback, jp: File, c: String) = false
      })

      whenReady(client.updateClassesAsync(file, "deleteClasses").failed) { e ⇒
        e shouldBe a[ProcedureNotQueuedException]
      }
    }

    "respond to #updateApplicationCatalog" in {
      val client = newClient()
      val catalogFile = new File("jarfile")
      val deploymentFile = new File("deployment")
      val response = emptyClientResponse

      when(client.javaClient.updateApplicationCatalog(catalogFile, deploymentFile))
        .thenReturn(response)

      client.updateApplicationCatalog(catalogFile, deploymentFile) shouldBe response
      verify(client.javaClient).updateApplicationCatalog(catalogFile, deploymentFile)
    }

    "respond to #updateApplicationCatalogAsync" in {
      val catalogFile = new File("jarfile")
      val deploymentFile = new File("deployment")
      val response = emptyClientResponse
      val client = newClient(new TestClient {
        override def updateApplicationCatalog(cb: ProcedureCallback, cp: File, dp: File) = {
          cb.clientCallback(response)
          true
        }
      })

      whenReady(client.updateApplicationCatalogAsync(catalogFile, deploymentFile)) { result ⇒ result shouldBe response }
    }

    "return a Future wrapping a ProcedureNotQueuedException when calling #updateApplicationCatalogAsync was not queued" in {
      val catalogFile = new File("jarfile")
      val deploymentFile = new File("deployment")
      val client = newClient()

      whenReady(client.updateApplicationCatalogAsync(catalogFile, deploymentFile).failed) { e ⇒
        e shouldBe a[ProcedureNotQueuedException]
      }
    }

    "respond to #getNewBulkLoader" in {
      import org.mockito.{Matchers ⇒ m}

      val client = newClient()
      val f = (p1: Any, p2: Seq[AnyRef], p3: ClientResponse) ⇒ ()

      client.getNewBulkLoader("table", 123, upsert = true)(f)
      verify(client.javaClient).getNewBulkLoader(m.eq("table"), m.eq(123), m.eq(true), m.any[BulkLoaderFailureCallBack]())

      client.getNewBulkLoader("table", 123)(f)
      verify(client.javaClient).getNewBulkLoader(m.eq("table"), m.eq(123), m.eq(false), m.any[BulkLoaderFailureCallBack]())
    }

    "respond to #callAllPartitionProcedure" in {
      val client = newClient()

      when(client.javaClient.callAllPartitionProcedure("proc", "p1".asInstanceOf[AnyRef]))
        .thenReturn(Array[ClientResponseWithPartitionKey]())

      client.callAllPartitionProcedure("proc", "p1") shouldBe a[Seq[_]]

      verify(client.javaClient).callAllPartitionProcedure("proc", "p1".asInstanceOf[AnyRef])
    }

    "respond to #callAllPartitionProcedureAsync" in {
      import org.mockito.{Matchers ⇒ m}

      val client = newClient()

      val answer = new Answer[Boolean] {
        override def answer(invocation: InvocationOnMock): Boolean = {
          val cb = invocation.getArguments()(0).asInstanceOf[AllPartitionProcedureCallback]
          cb.clientCallback(Array[ClientResponseWithPartitionKey]())
          true
        }
      }

      when(client.javaClient.callAllPartitionProcedure(m.any[AllPartitionProcedureCallback](), m.anyString(), m.anyVararg[String]()))
        .thenAnswer(answer)

      whenReady(client.callAllPartitionProcedureAsync("proc", "p1")) { result ⇒ result shouldBe a[Seq[_]] }

      verify(client.javaClient).callAllPartitionProcedure(m.any[AllPartitionProcedureCallback], m.eq("proc"), m.eq("p1"))
    }

    "return a Future wrapping a ProcedureNotQueuedException when calling #callAllPartitionProcedureAsync was not queued" in {
      val client = newClient()

      whenReady(client.callAllPartitionProcedureAsync("proc", "p1").failed) { e ⇒
        e shouldBe a[ProcedureNotQueuedException]
      }
    }

    "respond to #callProcedureWithTimeout" in {
      val client = newClient()
      val response = emptyClientResponse

      when(client.javaClient.callProcedureWithTimeout(123, "proc", 123.asInstanceOf[AnyRef], "arg".asInstanceOf[AnyRef]))
        .thenReturn(response)

      client.callProcedureWithTimeout(123, "proc", 123, "arg") shouldBe response
    }

    "respond to #callProcedureWithTimeoutAsync" in {
      val response = emptyClientResponse
      val client = newClient(new TestClient {
        override def callProcedureWithTimeout(cb: ProcedureCallback, to: Int, pn: String, p: AnyRef*) = {
          cb.clientCallback(response)
          true
        }
      })

      whenReady(client.callProcedureWithTimeoutAsync(123, "proc", 123, "arg")) { result ⇒ result shouldBe response }
    }

    "return a Future wrapping a ProcedureNotQueuedException when calling #callProcedureWithTimeoutAsync was not queued" in {
      val client = newClient(new TestClient {
        override def callProcedureWithTimeout(cb: ProcedureCallback, to: Int, pn: String, p: AnyRef*) = false
      })

      whenReady(client.callProcedureWithTimeoutAsync(123, "proc", 123, "arg").failed) { e ⇒
        e shouldBe a[ProcedureNotQueuedException]
      }
    }
  }
}
