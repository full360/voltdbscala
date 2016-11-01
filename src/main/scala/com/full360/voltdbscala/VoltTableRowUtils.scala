package com.full360.voltdbscala

import org.voltdb.VoltTableRow

object VoltTableRowUtils {
  implicit class ToOptionMethodSupport(row: VoltTableRow) {
    def toOption[T](value: T): Option[T] = value match {
      case v if !row.wasNull() ⇒ Some(v)
      case _                   ⇒ None
    }
  }

  implicit class GetLongAsBooleanMethodSupport(row: VoltTableRow) {
    def getLongAsBoolean(columnIndex: Int): Boolean =
      row.getLong(columnIndex) == 1

    def getLongAsBoolean(columnName: String): Boolean =
      row.getLong(columnName) == 1
  }
}
