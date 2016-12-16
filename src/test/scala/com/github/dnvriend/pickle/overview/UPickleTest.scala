package com.github.dnvriend.pickle.overview

import com.github.dnvriend._
import org.typelevel.scalatest.DisjunctionMatchers
import upickle._
import upickle.default._

import scalaz.Disjunction

case class Thing(fieldA: Int, fieldB: String)

case class Foo(i: Int)

case class Bar(name: String, foos: Seq[Foo])

case class OrderItem(item: String, size: Size)

case class FooDefault(x: Int = 10, y: String = "lol")

sealed trait Size

object Size {

  case object SMALL extends Size {
    implicit val SizeWriter: Writer[Size] = Writer[Size] {
      case Size.SMALL  => Js.Str("SMALL")
      case Size.MEDIUM => Js.Str("MEDIUM")
      case Size.LARGE  => Js.Str("LARGE")
      case Size.XXL    => Js.Str("XXL")
    }

    implicit val SizeReader: Reader[Size] = Reader[Size] {
      case Js.Str("SMALL") => Size.SMALL
      case _               => Size.SMALL
    }
  }

  case object MEDIUM extends Size

  case object LARGE extends Size

  case object XXL extends Size

  implicit val SizeWriter = Writer[Size] {
    case Size.SMALL  => Js.Str("SMALL")
    case Size.MEDIUM => Js.Str("MEDIUM")
    case Size.LARGE  => Js.Str("LARGE")
    case Size.XXL    => Js.Str("XXL")
  }

  implicit val SizeReader = Reader[Size] {
    case Js.Str("SMALL") => Size.SMALL
    case _               => Size.SMALL
  }
}

class UPickleTest extends TestSpec with DisjunctionMatchers {

  "Int" should "serialize" in {
    write(1) shouldBe
      "1"
  }

  "List" should "serialize" in {
    write(List(1, 2, 3)) shouldBe
      "[1,2,3]"
  }

  "Tuple" should "serialize" in {
    // Tuples of all sizes (1should "" in22) are serialized as heterogenous JSON lists
    write((1, "omg", true)) shouldBe
      """[1,"omg",true]"""
  }

  "Option" should "serialize" in {
    // Options are serialized as JSON lists with 0 or 1 element
    write(Option(1)) shouldBe
      "[1]"
    write(None) shouldBe
      "[]"
  }

  "CaseClasses" should "serialize" in {

    write(Thing(1, "gg")) shouldBe
      """{"fieldA":1,"fieldB":"gg"}"""

    read[Thing]("""{"fieldA":1,"fieldB":"gg"}""") shouldBe
      Thing(1, "gg")

    write(Bar(null, Seq(Foo(1), null, Foo(3)))) shouldBe
      """{"name":null,"foos":[{"i":1},null,{"i":3}]}"""
  }

  "SealedTraits" should "serialize" in {
    write(OrderItem("foos", Size.SMALL)) shouldBe
      """{"item":"foos","size":{"$type":"com.github.dnvriend.pickle.overview.Size.SMALL"}}"""
  }

  "Reading stuff" should "" in {
    read[Int]("1") shouldBe
      1

    read[List[Int]]("[1,2,3]") shouldBe
      List(1, 2, 3)

    read[(Int, String, Boolean)]("""[1,"omg",true]""") shouldBe
      (1, "omg", true)
  }

  "defaults" should "deserialize" in {
    // If a field is missing upon deserialization, uPickle uses the default value if one exists
    read[FooDefault]("{}") shouldBe FooDefault(10, "lol")
    read[FooDefault]("""{"x": 123}""") shouldBe FooDefault(123, "lol")
  }

  it should "serialize" in {
    // If a field at serialization time has the same value as the default,
    // uPickle leaves it out of the serialized blob

    write(FooDefault(x = 11, y = "lol")) shouldBe """{"x":11}"""
    write(FooDefault(x = 10, y = "lol")) shouldBe "{}"
    write(FooDefault()) shouldBe "{}"
  }

  it should "operate in disjunction" in {
    Disjunction.fromTryCatchNonFatal(write(Foo(1))).leftMap(_.toString) should beRight("""{"i":1}""")
    Disjunction.fromTryCatchNonFatal(read[Foo]("{}")).leftMap(_.toString) should beLeft("upickle.Invalid$Data: Key Missing: i (data: {})")
  }
}
