package eu.joaocosta.minart.graphics

import scala.scalanative.unsigned._

import sdl2.SDL._

import eu.joaocosta.minart.backend._

object SdlImageSurfaceSpec extends MutableSurfaceTests {
  lazy val surface = new SdlSurface(
    SDL_CreateRGBSurface(0.toUInt, 64, 48, 32, 0.toUInt, 0.toUInt, 0.toUInt, 0.toUInt)
  )

}
