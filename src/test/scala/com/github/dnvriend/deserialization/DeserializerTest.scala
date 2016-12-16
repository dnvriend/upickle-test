package com.github.dnvriend.deserialization

import com.github.dnvriend.TestSpec
import org.typelevel.scalatest.DisjunctionMatchers

import upickle.default._

import scala.util.Try

final case class Foo(foo: Int)
final case class Bar(bar: String)
final case class Baz(baz: Option[String])
final case class Qux(qux: Option[Long])

class DeserializerTest extends TestSpec with DisjunctionMatchers {
  def fromJson(str: String): Option[Any] = {
    (
      Try(read[Foo](str)) orElse
      Try(read[Bar](str)) orElse
      Try(read[Baz](str)) orElse
      Try(read[Qux](str))
    ).toOption
  }

  val logOfMessages: String = List(
    write(Foo(1)), write(Bar("bar")), write(Baz(Option("baz"))), write(Qux(Option(1))), write(Foo(2)), write(Baz(None))
  ).mkString("\n")

  it should "read log of messages" in {
    logOfMessages.split("\n").toList.flatMap(fromJson) shouldBe List(Foo(1), Bar("bar"), Baz(Some("baz")), Qux(Some(1)), Foo(2), Baz(None))
  }
}
