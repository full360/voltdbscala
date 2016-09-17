package com.full360.voltdbscala.util

import org.scalatest.{Matchers, WordSpec}
import org.voltdb.{VoltTable, VoltTableRow, VoltType}
import org.voltdb.client.TestClientResponse

class UtilSpec extends WordSpec with Matchers {
  import Util._

  def provide = afterWord("provide")

  "Helpers" should provide {
    "an implicit converter from Boolean to Long" which {
      "returns 1 when Boolean is true" in {
        val trueLongValue: Long = true
        trueLongValue shouldBe 1
      }

      "returns 0 when Boolean is false" in {
        val falseLongValue: Long = false
        falseLongValue shouldBe 0
      }
    }

    "an implicit converter from Long to Boolean" which {
      "returns true when Long is 1" in {
        val trueLongValue: Long = true
        trueLongValue shouldBe 1
      }

      "returns false when Long is 0" in {
        val falseLongValue: Long = false
        falseLongValue shouldBe 0
      }
    }

    "an implicit converter from VoltTableRow cell to Option[T]" which {
      def rowBuilder(v: Long) = VoltTableBuilder.build(Seq(VoltType.BIGINT), 1) { (_, _) ⇒ v.asInstanceOf[AnyRef] }.fetchRow(0)

      "returns Some(value) when value is not null" in {
        val notNullVoltTableRow = rowBuilder(123)
        notNullVoltTableRow.toOption(notNullVoltTableRow.getLong(0)) shouldBe Some(123)
      }

      "returns None when value is null" in {
        val nullVoltTableRow = rowBuilder(VoltType.NULL_BIGINT)
        nullVoltTableRow.toOption(nullVoltTableRow.getLong(0)) shouldBe None
      }
    }

    "a helper that extracts optional single result from Vector[T]" which {
      "returns Some(value) when the collection contains the result" in {
        toSingleResult(Vector("a")) shouldBe Some("a")
      }

      "returns None when the collection does not contain any result" in {
        toSingleResult(Vector()) shouldBe None
      }
    }

    "a helper that receives varargs of type Any and returns a Seq of type AnyRef(Object type in java)" in {
      val seq = paramsToJavaObjects(1, 2, 3, "a", "b", "c")
      seq shouldBe Seq(1, 2, 3, "a", "b", "c")
      seq shouldBe a[Seq[_]]
    }

    "a helper that parses an server address into a tuple of host and port" which {
      "returns (host, port) tuple from address with format host:port" in {
        hostAndPortFromAddress("host:123") shouldBe Tuple2("host", 123)
      }

      "returns (host, 21212) tuple from address without port information" in {
        hostAndPortFromAddress("host") shouldBe Tuple2("host", 21212)
      }
    }

    /* Add extra scope for mocks */ {
      case class TestObject(testCol: String)

      def f(row: VoltTableRow): TestObject = TestObject(row.getString(0))

      def testResultingVector(result: Vector[TestObject]): Unit = {
        result shouldBe a[Vector[_]]
        for (i ← 1.to(5)) result(i - 1) shouldBe TestObject(s"row${i}")
      }

      val testVoltTable = VoltTableBuilder.build(Seq(VoltType.STRING), 5) { (rowIndex, _) ⇒ s"row${rowIndex}" }

      val testClientResponse = new TestClientResponse { override def getResults: Array[VoltTable] = Array(testVoltTable) }

      "a helper that maps a VoltTable into a collection of type Vector[T]" in {
        val result = mapVoltTable(testVoltTable)(f)
        testResultingVector(result)
      }

      "a helper that maps a VoltTable from a ClientResponse into a collection of type Vector[T]" in {
        val result = mapClientResponse(testClientResponse, 0)(f)
        testResultingVector(result)
      }
    }
  }
}
