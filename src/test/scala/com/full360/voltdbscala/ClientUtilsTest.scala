package com.full360.voltdbscala

import org.scalatest.{ Matchers, WordSpec }

class ClientUtilsTest extends WordSpec with Matchers {
  "ClientUtils.paramsToJavaObjects maps parameters from Any to AnyRef(java Object)" in {
    val result = ClientUtils.paramsToJavaObjects(1, "a")
    result shouldBe Seq[Any](1, "a")
  }

  "ClientUtils.paramsToJavaObjects parses Option values" in {
    val result = ClientUtils.paramsToJavaObjects(Some(1), None)
    result shouldBe Seq[Any](1, null)
  }

  "ClientUtils.hostAndPortFromAddress parses host:port address into (host, port)" in {
    val result = ClientUtils.hostAndPortFromAddress("localhost:123")
    val expectedResult = ("localhost", 123)
    result shouldBe expectedResult
  }

  "ClientUtils.hostAndPortFromAddress returns default port when it is not specified" in {
    val result = ClientUtils.hostAndPortFromAddress("localhost")
    val expectedResult = ("localhost", org.voltdb.client.Client.VOLTDB_SERVER_PORT)
    result shouldBe expectedResult
  }
}
