package com.full360.voltdbscala

import org.voltdb.client.ClientResponse
import org.voltdb.VoltTableRow

object ClientResponseUtils {

  /**
   * Parses VoltTable from ClientResponse results into a sequence of given type. As results are a VoltTable array an
   * index is required to fetch the VoltTable to parse
   *
   * @param clientResponse the ClientResponse to process results from
   * @param index the index of the results array to parse
   * @param f a function responsible for parsing a single row
   * @tparam T the type the rows are parsed to
   * @return an Sequence of given type containing the parsed rows
   */
  def mapClientResponseResult[T](clientResponse: ClientResponse, index: Int)(f: VoltTableRow ⇒ T): Seq[T] =
    VoltTableUtils.mapVoltTable[T](clientResponse.getResults()(index))(f)

  /**
   * Parses just the first row of the results.
   *
   * @param clientResponse the ClientResponse to process results from
   * @param index the index of the results array to parse
   * @param f a function responsible for parsing a single row
   * @tparam T the type the row is parsed to
   * @return an Option of given type containing the first row or None if the results are empty
   */
  def mapFirstRowFromClientResponseResult[T](clientResponse: ClientResponse, index: Int)(f: VoltTableRow ⇒ T): Option[T] =
    VoltTableUtils.mapFirstRowFromVoltTable[T](clientResponse.getResults()(index))(f)

  implicit class MapMethodSupport(clientResponse: ClientResponse) {
    def map[T](index: Int)(f: VoltTableRow ⇒ T): Seq[T] =
      ClientResponseUtils.mapClientResponseResult(clientResponse, index)(f)

    def mapFirstRow[T](index: Int)(f: VoltTableRow ⇒ T): Option[T] =
      ClientResponseUtils.mapFirstRowFromClientResponseResult(clientResponse, index)(f)
  }
}
