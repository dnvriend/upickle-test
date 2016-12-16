package com.github.dnvriend.play.deserialization

import com.github.dnvriend.TestSpec
import org.typelevel.scalatest.DisjunctionMatchers
import play.api.libs.json._

import scala.util.Try

object Foo {
  implicit val format = Json.format[Foo]
}
final case class Foo(foo: Int)

object Bar {
  implicit val format = Json.format[Bar]
}
final case class Bar(bar: String)

object Baz {
  implicit val format = Json.format[Baz]
}
final case class Baz(baz: Option[String])

object Qux {
  implicit val format = Json.format[Qux]
}
final case class Qux(qux: Option[Long])

class PlayDeserializerTest extends TestSpec with DisjunctionMatchers {
  def toJson[A: Format](a: A) = Json.toJson(a).toString

  def fromJson(json: JsValue): Option[Any] = {
    (
      Try(json.as[Foo]) orElse
      Try(json.as[Bar]) orElse
      Try(json.as[Baz]) orElse
      Try(json.as[Qux])
    ).map { e => println(s"$json => $e"); e }.toOption
  }

  val logOfMessages: String =
    List(
      toJson(Foo(1)),
      toJson(Bar("bar")),
      toJson(Baz(Option("baz"))),
      toJson(Qux(Option(1))),
      toJson(Foo(2)),
      toJson(Baz(None))
    ).mkString("\n")

  ignore should "read log of messages" in {
    // good to know that matching on a function with orElse can give false positives
    logOfMessages
      .split("\n")
      .toList
      .map(Json.parse)
      .flatMap(fromJson) shouldBe List(Foo(1), Bar("bar"), Baz(Some("baz")), Qux(Some(1)), Foo(2), Baz(None))
  }
}
