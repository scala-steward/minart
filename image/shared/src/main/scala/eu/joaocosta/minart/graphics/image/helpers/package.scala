package eu.joaocosta.minart.graphics.image

package object helpers {

  type ParseResult[T]   = Either[String, (LazyList[Int], T)]
  type ParseState[E, T] = State[LazyList[Int], E, T]

  def skipBytes(n: Int): ParseState[Nothing, Unit] =
    State.modify(_.drop(n))
  val readByte: ParseState[String, Option[Int]] = State { bytes =>
    bytes.tail -> bytes.headOption
  }
  def readBytes(n: Int): ParseState[Nothing, Array[Int]] = State { bytes =>
    bytes.drop(n) -> bytes.take(n).toArray
  }
  def readString(n: Int): ParseState[Nothing, String] =
    readBytes(n).map { bytes => bytes.map(_.toChar).mkString("") }
  def readLENumber(n: Int): ParseState[Nothing, Int] = readBytes(n).map { bytes =>
    bytes.zipWithIndex.map { case (num, idx) => num.toInt << (idx * 8) }.sum
  }
  def readBENumber(n: Int): ParseState[Nothing, Int] = readBytes(n).map { bytes =>
    bytes.reverse.zipWithIndex.map { case (num, idx) => num.toInt << (idx * 8) }.sum
  }
}
