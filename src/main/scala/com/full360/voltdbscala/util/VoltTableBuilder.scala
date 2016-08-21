package com.full360.voltdbscala.util

import org.voltdb.{ VoltTable, VoltType }
import scala.annotation.tailrec

/**
 * Helper class for mocking easily VoltTable objects
 */
object VoltTableBuilder {

  /**
   * This is the main method of the helper. It creates a VoltTable instance with the given structure
   * @param columnTypes a sequence of VoltDB types that structure the table
   * @param nRows the number of rows the table will contain
   * @param f a function called for every table cell. It receives the current row and column index (1 based)
   *          and it must return the value to be stored in the table cell
   * @return the VoltTable object created
   */
  def build(columnTypes: Seq[VoltType], nRows: Int = 1)(f: (Int, Int) ⇒ AnyRef): VoltTable = {
    val voltTable = new VoltTable(buildColumns(columnTypes))

    for (rowIndex ← 1 to nRows) {
      voltTable.addRow(buildRow(rowIndex, columnTypes.size)(f): _*)
    }

    voltTable
  }

  def columnInfo(name: String, t: VoltType): VoltTable.ColumnInfo =
    new VoltTable.ColumnInfo(name, t)

  def buildColumns(columnTypes: Seq[VoltType]): Array[VoltTable.ColumnInfo] = {
    @tailrec
    def build(colIndex: Int, a: Array[VoltTable.ColumnInfo]): Array[VoltTable.ColumnInfo] = {
      if (colIndex > 0) build(colIndex - 1, a.+:(new VoltTable.ColumnInfo(s"col${colIndex}", columnTypes(colIndex - 1))))
      else a
    }

    build(columnTypes.size, Array())
  }

  def buildRow(rowIndex: Int, nColumns: Int)(f: (Int, Int) ⇒ AnyRef): Array[AnyRef] = {
    @tailrec
    def build(colIndex: Int, a: Array[AnyRef]): Array[AnyRef] =
      if (colIndex > 0) build(colIndex - 1, f(rowIndex, colIndex) +: a)
      else a

    build(nColumns, Array())
  }
}
