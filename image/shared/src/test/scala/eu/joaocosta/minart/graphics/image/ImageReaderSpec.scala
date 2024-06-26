package eu.joaocosta.minart.graphics.image

import scala.util.Try

import eu.joaocosta.minart.backend.defaults.given
import eu.joaocosta.minart.graphics.*
import eu.joaocosta.minart.runtime.*

class ImageReaderSpec extends munit.FunSuite {

  def sameImage(results: List[RamSurface]): Unit = {
    results.sliding(2).foreach {
      case img1 :: img2 :: _ => assert(img1.getPixels().map(_.toVector) == img2.getPixels().map(_.toVector))
      case _                 => ()
    }
  }

  def testSize(results: List[Try[RamSurface]], expectedWidth: Int, expectedHeight: Int): Unit = {
    assert(results.map(_.get).size != 0)
    assert(results.head.get.width == expectedWidth)
    assert(results.head.get.height == expectedHeight)
    sameImage(results.map(_.get))
  }

  // Can't load resources in JS tests
  if (Platform() != Platform.JS) {
    test("Load a BMP image") {
      def bmpTest(dir: String, width: Int, height: Int): Unit =
        testSize(
          List(
            Image.loadBmpImage(Resource(s"$dir/bmp-24bit.bmp")),
            Image.loadBmpImage(Resource(s"$dir/bmp-32bit.bmp"))
          ),
          width,
          height
        )
      bmpTest("scala", 128, 128)
      bmpTest("scala-rect", 77, 119)
      testSize(List(Image.loadBmpImage(Resource(s"alpha/bmp-32bit.bmp"))), 507, 200)
      bmpTest("lausanne", 640, 480)
    }

    test("Load a PPM image") {
      def ppmTest(dir: String, width: Int, height: Int): Unit =
        testSize(
          List(Image.loadPpmImage(Resource(s"$dir/ppm-p3.ppm")), Image.loadPpmImage(Resource(s"$dir/ppm-p6.ppm"))),
          width,
          height
        )
      ppmTest("scala", 128, 128)
      ppmTest("scala-rect", 77, 119)
      ppmTest("lausanne", 640, 480)
    }

    test("Load a PGM image") {
      def pgmTest(dir: String, width: Int, height: Int): Unit =
        testSize(
          List(Image.loadPpmImage(Resource(s"$dir/pgm-p2.pgm")), Image.loadPpmImage(Resource(s"$dir/pgm-p5.pgm"))),
          width,
          height
        )
      pgmTest("scala", 128, 128)
      pgmTest("scala-rect", 77, 119)
      pgmTest("lausanne", 640, 480)
    }

    test("Load a PBM image") {
      def pbmTest(dir: String, width: Int, height: Int): Unit =
        testSize(
          List(Image.loadPpmImage(Resource(s"$dir/pbm-p1.pbm")), Image.loadPpmImage(Resource(s"$dir/pbm-p4.pbm"))),
          width,
          height
        )
      pbmTest("scala", 128, 128)
      pbmTest("scala-rect", 77, 119)
      pbmTest("lausanne", 640, 480)
    }

    test("Load a QOI image") {
      def qoiTest(dir: String, width: Int, height: Int): Unit =
        testSize(
          List(
            Image.loadQoiImage(Resource(s"$dir/qoi-24bit.qoi")),
            Image.loadQoiImage(Resource(s"$dir/qoi-32bit.qoi"))
          ),
          width,
          height
        )
      qoiTest("scala", 128, 128)
      qoiTest("scala-rect", 77, 119)
      testSize(List(Image.loadQoiImage(Resource(s"alpha/qoi-32bit.qoi"))), 507, 200)
      qoiTest("lausanne", 640, 480)
    }

    test("Load a PDI image") {
      def pdiTest(dir: String, width: Int, height: Int): Unit =
        testSize(
          List(
            Image.loadPdiImage(Resource(s"$dir/pdi-2bit.pdi"))
          ),
          width,
          height
        )
      pdiTest("alpha", 507, 200)
    }

    test("Load the same data from different formats (square image)") {
      sameImage(
        List(
          Image.loadBmpImage(Resource("scala/bmp-24bit.bmp")).get,
          Image.loadPpmImage(Resource("scala/ppm-p3.ppm")).get,
          Image.loadQoiImage(Resource("scala/qoi-24bit.qoi")).get
        )
      )
    }

    test("Load the same data from different formats (non-square image)") {
      sameImage(
        List(
          Image.loadBmpImage(Resource("scala-rect/bmp-24bit.bmp")).get,
          Image.loadPpmImage(Resource("scala-rect/ppm-p3.ppm")).get,
          Image.loadQoiImage(Resource("scala-rect/qoi-24bit.qoi")).get
        )
      )
    }

    test("Load the same data from different formats (transparent image)") {
      sameImage(
        List(
          Image.loadBmpImage(Resource("alpha/bmp-32bit.bmp")).get,
          Image.loadQoiImage(Resource("alpha/qoi-32bit.qoi")).get
        )
      )
    }
    test("Load the same data from different formats (large image)") {
      sameImage(
        List(
          Image.loadBmpImage(Resource("lausanne/bmp-24bit.bmp")).get,
          Image.loadPpmImage(Resource("lausanne/ppm-p3.ppm")).get,
          Image.loadQoiImage(Resource("lausanne/qoi-24bit.qoi")).get
        )
      )
    }
  }
}
