package com.full360.voltdbscala

import org.voltdb.client.ClientResponse

/**
 * Exception thrown when the status of a client response if not success
 * @param message the detail message of this exception
 */
case class ClientResponseStatusException(message: String) extends Exception(message)

object ClientResponseStatusException {
  def apply(clientResponse: ClientResponse): ClientResponseStatusException = {
    val status = (clientResponse.getStatusString, clientResponse.getStatus)
    new ClientResponseStatusException(s"""Stored procedure replied with status: "${status._1}". Code: ${status._2}""")
  }
}

/**
 * Exception thrown when an asynchronous procedure call is not queued
 * @param message the detail message of this exception
 */
case class ProcedureNotQueuedException(message: String) extends Exception(message)

