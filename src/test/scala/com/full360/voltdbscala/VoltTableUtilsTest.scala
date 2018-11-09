package com.full360.voltdbscala

import com.full360.voltdbscala.util.VoltTableBuilder
import org.scalatest.{ Matchers, WordSpec }
import org.voltdb.{ VoltTable, VoltTableRow, VoltType }

class VoltTableUtilsTest extends WordSpec with Matchers {
  case class TestObj(col1: String, col2: String)

  def createVoltTable(nRows: Int): VoltTable = {
    val f = (row: Int, col: Int) ⇒ s"r${row}:c${col}"
    VoltTableBuilder.build(nRows, VoltType.STRING, VoltType.STRING)(f)
  }

  def parseTestRow(row: VoltTableRow) = TestObj(row.getString(0), row.getString(1))

  "VoltTableUtils.mapVoltTable maps VoltTable into Seq[T]" in {
    val voltTable = createVoltTable(3)
    val result = VoltTableUtils.mapVoltTable(voltTable)(parseTestRow)

    result shouldBe Seq(
      TestObj("r1:c1", "r1:c2"),
      TestObj("r2:c1", "r2:c2"),
      TestObj("r3:c1", "r3:c2"))
  }

  "VoltTableUtils.mapFirstRowFromVoltTable maps first row into Some[T]" in {
    val voltTable = createVoltTable(3)
    val result = VoltTableUtils.mapFirstRowFromVoltTable(voltTable)(parseTestRow)

    result shouldBe Some(TestObj("r1:c1", "r1:c2"))
  }

  "VoltTableUtils.mapFirstRowFromVoltTable maps first row into None if no records where found" in {
    val voltTable = createVoltTable(0)
    val result = VoltTableUtils.mapFirstRowFromVoltTable(voltTable)(parseTestRow)

    result shouldBe None
  }

  "VoltTableUtils.MapMethodSupport adds 'map' method to VoltTable instances" in {
    import VoltTableUtils.MapMethodSupport

    val voltTable = createVoltTable(1)
    voltTable.map(parseTestRow) shouldBe Seq(TestObj("r1:c1", "r1:c2"))
  }

  "VoltTableUtils.MapMethodSupport adds 'mapFirstRow' method to VoltTable instances" in {
    import VoltTableUtils.MapMethodSupport

    val voltTable = createVoltTable(1)
    voltTable.mapFirstRow(parseTestRow) shouldBe Some(TestObj("r1:c1", "r1:c2"))
  }
}
