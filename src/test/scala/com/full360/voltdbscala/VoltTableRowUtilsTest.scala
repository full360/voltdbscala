package com.full360.voltdbscala

import com.full360.voltdbscala.util.VoltTableBuilder
import org.scalatest.{ Matchers, WordSpec }
import org.voltdb.{ VoltTable, VoltType }

class VoltTableRowUtilsTest extends WordSpec with Matchers {

  def createVoltTable(value: AnyRef): VoltTable = {
    val f = (row: Int, col: Int) ⇒ value
    VoltTableBuilder.build(1, VoltType.INTEGER)(f)
  }

  "VoltTableRowUtils.ToOptionMethodSupport adds 'toOption' method to VoltTableRow instances" in {
    import VoltTableRowUtils.ToOptionMethodSupport

    val row1 = createVoltTable(123.asInstanceOf[AnyRef]).fetchRow(0)
    row1.toOption(row1.getLong(0)) shouldBe Some(123)

    val row2 = createVoltTable(null).fetchRow(0)
    row2.toOption(row2.getLong(0)) shouldBe None
  }

  "VoltTableRowUtils.GetLongAsBooleanMethodSupport adds 'getLongAsBoolean' method to VoltTableRow instances" in {
    import VoltTableRowUtils.GetLongAsBooleanMethodSupport

    val row1 = createVoltTable(1.asInstanceOf[AnyRef]).fetchRow(0)
    row1.getLongAsBoolean(0) shouldBe true

    val row2 = createVoltTable(0.asInstanceOf[AnyRef]).fetchRow(0)
    row2.getLongAsBoolean(0) shouldBe false
  }
}
