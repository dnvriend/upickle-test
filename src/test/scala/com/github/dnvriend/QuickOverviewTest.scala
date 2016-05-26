package com.github.dnvriend

import utest._
import upickle.default._

case class Thing(fieldA: Int, fieldB: String)

case class Foo(i: Int)
case class Bar(name: String, foos: Seq[Foo])

object QuickOverviewTest extends TestSuite {
  val tests = this{
    'Int - {
      write(1) ==>
        "1"
    }
    'List - {
      write(List(1, 2, 3)) ==>
        "[1,2,3]"
    }
    'Tuple - {
      // Tuples of all sizes (1-22) are serialized as heterogenous JSON lists
      write((1, "omg", true)) ==>
        """[1,"omg",true]"""
    }
    'Option - {
      // Options are serialized as JSON lists with 0 or 1 element
      write(Option(1)) ==>
        "[1]"
      write(None) ==>
        "[]"
    }
    'CaseClasses - {
      // Case classes of sizes 1-22 are serialized as JSON dictionaries with the keys being the names of each field
      write(Thing(1, "gg")) ==>
        """{"fieldA":1,"fieldB":"gg"}"""
      read[Thing]("""{"fieldA":1,"fieldB":"gg"}""") ==>
        Thing(1, "gg")
      write(Bar(null, Seq(Foo(1), null, Foo(3)))) ==>
        """{"name":null,"foos":[{"i":1},null,{"i":3}]}"""
    }
    "Reading stuff" - {
      read[Int]("1") ==>
        1
      read[List[Int]]("[1,2,3]") ==>
        List(1, 2, 3)
      read[(Int, String, Boolean)]("""[1,"omg",true]""") ==>
        (1, "omg", true)
    }
  }
}
