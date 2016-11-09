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
   * @param validateStatus flag for checking the status of the client response before doing any other operation
   * @param f a function responsible for parsing a single row
   * @tparam T the type the rows are parsed to
   * @return an Sequence of given type containing the parsed rows
   */
  def mapClientResponseResult[T](clientResponse: ClientResponse, index: Int, validateStatus: Boolean = true)(f: VoltTableRow ⇒ T): Seq[T] = {
    if (validateStatus) validateClientResponseStatus(clientResponse)
    VoltTableUtils.mapVoltTable[T](clientResponse.getResults()(index))(f)
  }

  /**
   * Parses just the first row of the results.
   *
   * @param clientResponse the ClientResponse to process results from
   * @param index the index of the results array to parse
   * @param validateStatus flag for checking the status of the client response before doing any other operation
   * @param f a function responsible for parsing a single row
   * @tparam T the type the row is parsed to
   * @return an Option of given type containing the first row or None if the results are empty
   */
  def mapFirstRowFromClientResponseResult[T](clientResponse: ClientResponse, index: Int, validateStatus: Boolean = true)(f: VoltTableRow ⇒ T): Option[T] = {
    if (validateStatus) validateClientResponseStatus(clientResponse)
    VoltTableUtils.mapFirstRowFromVoltTable[T](clientResponse.getResults()(index))(f)
  }

  /**
   * Checks that the status of the client response is SUCCESS. If not, it throws an exception
   *
   * @param clientResponse client response instance to be evaluated
   * @return the same client response passed in when its status is equal to <code>ClientResponse.SUCCESS</code>
   */
  def validateClientResponseStatus(clientResponse: ClientResponse): ClientResponse = clientResponse.getStatus match {
    case ClientResponse.SUCCESS ⇒ clientResponse
    case _                      ⇒ throw ClientResponseStatusException(clientResponse)
  }

  implicit class MapMethodSupport(clientResponse: ClientResponse) {
    def map[T](index: Int, validateStatus: Boolean = true)(f: VoltTableRow ⇒ T): Seq[T] =
      ClientResponseUtils.mapClientResponseResult(clientResponse, index, validateStatus)(f)

    def mapFirstRow[T](index: Int, validateStatus: Boolean = true)(f: VoltTableRow ⇒ T): Option[T] =
      ClientResponseUtils.mapFirstRowFromClientResponseResult(clientResponse, index, validateStatus)(f)
  }

  implicit class ValidateStatusMethodSupport(clientResponse: ClientResponse) {
    def validateStatus: ClientResponse = validateClientResponseStatus(clientResponse)
  }
}
