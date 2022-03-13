package eu.joaocosta.minart.graphics.image

import scala.io.{BufferedSource, Source}
import scala.util.{Failure, Success, Try}

import eu.joaocosta.minart.graphics._
import eu.joaocosta.minart.runtime.Resource

/** Object containing user-friendly functions to load and manipulate images.
  */
object Image {

  /** Inverts an image color.
    */
  def invert(surface: Surface): SurfaceView =
    surface.view.map(_.invert)

  /** Flips an image horizontally.
    */
  def flipH(surface: Surface): SurfaceView =
    surface.view.contramap((x, y) => (surface.width - x - 1, y), surface.width, surface.height)

  /** Flips an image vertically.
    */
  def flipV(surface: Surface): SurfaceView =
    surface.view.contramap((x, y) => (x, surface.height - y - 1), surface.width, surface.height)

  /** Transposes an image.
    */
  def transpose(surface: Surface): SurfaceView =
    surface.view.contramap((x, y) => (y, x), surface.height, surface.width)

  /** Loads an image using a custom ImageLoader.
    *
    * @param loader ImageLoader to use
    * @param resource Resource pointing to the image
    */
  def loadImage(loader: ImageLoader, resource: Resource): Try[RamSurface] = {
    resource
      .withInputStream { inputStream =>
        loader.loadImage(inputStream)
      }
      .flatMap {
        case Left(error)   => Failure(new Exception(error))
        case Right(result) => Success(result)
      }
  }

  /** Loads an image in the PPM format.
    */
  def loadPpmImage(resource: Resource): Try[RamSurface] =
    loadImage(PpmImageLoader.defaultLoader, resource)

  /** Loads an image in the BMP format.
    */
  def loadBmpImage(resource: Resource): Try[RamSurface] =
    loadImage(BmpImageLoader.defaultLoader, resource)

  /** Loads an image in the QOI format.
    */
  def loadQoiImage(resource: Resource): Try[RamSurface] =
    loadImage(QoiImageLoader.defaultLoader, resource)
}
