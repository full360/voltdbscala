package com.full360.voltdbscala.util

import com.full360.voltdbscala.VoltDB
import org.voltdb.client.ClientResponse
import org.voltdb.{ VoltTable, VoltTableRow }
import scala.annotation.tailrec
import scala.collection.immutable.Vector

object Util {
  implicit def boolToLong(b: Boolean): Long = if (b) 1 else 0

  implicit def longToBool(l: Long): Boolean = if (l == 1) true else false

  implicit class VoltTableRowHelper(row: VoltTableRow) {
    def toOption[T](value: T): Option[T] = value match {
      case v if !row.wasNull() ⇒ Some(v)
      case _                   ⇒ None
    }
  }

  def toSingleResult[T](v: Vector[T]): Option[T] = v.size match {
    case 0 ⇒ None
    case _ ⇒ Some(v(0))
  }

  def paramsToJavaObjects(params: Any*) = params.map { param ⇒ param.asInstanceOf[AnyRef] }

  /**
   * Extracts hostname and port from address. If the address does not specify a port, the default port will be used
   * @param address a string in the form hostname:port or hostname
   * @return a Tuple containing the host and port extracted from the address given
   */
  def hostAndPortFromAddress(address: String, defaultPort: Int = VoltDB.DEFAULT_PORT): (String, Int) = {
    val s = address.split(':')

    s.size match {
      case 1 ⇒ (address, defaultPort)
      case 2 ⇒ (s(0), s(1).toInt)
      case _ ⇒ ("", 0)
    }
  }

  /**
   * Iterates over a VoltTable instance parsing results into given type using function received in parameters
   * @param voltTable the VoltTable to iterate over
   * @param f the function used to parse rows
   * @tparam T the type the rows are parsed to
   * @return an immutable Vector of given type containing the parsed rows
   */
  def mapVoltTable[T](voltTable: VoltTable)(f: VoltTableRow ⇒ T): Vector[T] = {
    @tailrec
    def mapNextRow(v: Vector[T]): Vector[T] =
      if (voltTable.advanceRow()) mapNextRow(v :+ f(voltTable))
      else v

    voltTable.resetRowPosition()
    mapNextRow(Vector[T]())
  }

  /**
   * Parses VoltTable from ClientResponse results into a Vector of given type. As results are a VoltTable array an
   * index is required to fetch the VoltTable to parse
   * @param clientResponse the ClientResponse to process results from
   * @param index the index of the results array to parse
   * @param f a function responsible for parsing a single row
   * @tparam T the type the rows are parsed to
   * @return an immutable Vector of given type containing the parsed rows
   */
  def mapClientResponse[T](clientResponse: ClientResponse, index: Int)(f: VoltTableRow ⇒ T): Vector[T] =
    mapVoltTable[T](clientResponse.getResults()(index))(f)
}
