package com.full360.voltdbscala

import org.voltdb.{VoltTable, VoltTableRow}

object VoltTableUtils {

  /**
   * Iterates over a VoltTable instance parsing results into given type using function received in parameters
   * @param voltTable the VoltTable to iterate over
   * @param f the function used to parse rows
   * @tparam T the type the rows are parsed to
   * @return an immutable sequence of given type containing the parsed rows
   */
  def mapVoltTable[T](voltTable: VoltTable)(f: VoltTableRow ⇒ T): Seq[T] = {
    val b = Vector.newBuilder[T]
    voltTable.resetRowPosition()
    while (voltTable.advanceRow()) b += f(voltTable)
    b.result()
  }

  /**
   * Parses just the first row of a VoltTable instance into given type using function received in parameters
   * @param voltTable the VoltTable to iterate over
   * @param f the function used to parse rows
   * @tparam T the type the rows are parsed to
   * @return an Option of given type containing the first row or None if the results are empty
   */
  def mapFirstRowFromVoltTable[T](voltTable: VoltTable)(f: VoltTableRow ⇒ T): Option[T] = {
    voltTable.resetRowPosition()
    if (voltTable.advanceRow()) Some(f(voltTable))
    else None
  }

  implicit class MapMethodSupport(voltTable: VoltTable) {
    def map[T](f: VoltTableRow ⇒ T): Seq[T] =
      VoltTableUtils.mapVoltTable(voltTable)(f)

    def mapFirstRow[T](f: VoltTableRow ⇒ T): Option[T] = VoltTableUtils.mapFirstRowFromVoltTable(voltTable)(f)
  }
}
