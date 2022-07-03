package eu.joaocosta.minart.backend

import scala.scalanative.unsafe._
import scala.scalanative.unsigned._

import sdl2.Extras._
import sdl2.SDL._

import eu.joaocosta.minart.graphics._
import eu.joaocosta.minart.input._

/** A low level Canvas implementation that shows the image in a SDL Window.
  */
class SdlCanvas() extends SurfaceBackedCanvas {

  // Rendering resources

  private[this] var ubyteClearR: UByte              = _
  private[this] var ubyteClearG: UByte              = _
  private[this] var ubyteClearB: UByte              = _
  private[this] var window: Ptr[SDL_Window]         = _
  private[this] var windowSurface: Ptr[SDL_Surface] = _
  protected var surface: SdlSurface                 = _

  // Input resources

  private[this] var keyboardInput: KeyboardInput = KeyboardInput(Set(), Set(), Set())
  private[this] var pointerInput: PointerInput   = PointerInput(None, Nil, Nil, false)
  private[this] var rawPointerPos: (Int, Int)    = _
  private[this] def cleanPointerPos: Option[PointerInput.Position] = if (isCreated())
    Option(rawPointerPos).map { case (x, y) =>
      PointerInput.Position(
        (x - extendedSettings.canvasX) / settings.scale,
        (y - extendedSettings.canvasY) / settings.scale
      )
    }
  else None

  private[this] def handleEvents(): Boolean = {
    val event              = stackalloc[SDL_Event]()
    var keepGoing: Boolean = isCreated()
    while (keepGoing && SDL_PollEvent(event) != 0) {
      event.type_ match {
        case SDL_KEYDOWN =>
          SdlKeyMapping.getKey(event.key.keysym.sym).foreach(k => keyboardInput = keyboardInput.press(k))
        case SDL_KEYUP =>
          SdlKeyMapping.getKey(event.key.keysym.sym).foreach(k => keyboardInput = keyboardInput.release(k))
        case SDL_MOUSEMOTION =>
          rawPointerPos = (event.motion.x, event.motion.y)
        case SDL_MOUSEBUTTONDOWN =>
          pointerInput = pointerInput.move(cleanPointerPos).press
        case SDL_MOUSEBUTTONUP =>
          pointerInput = pointerInput.move(cleanPointerPos).release
        case SDL_QUIT =>
          close()
          keepGoing = false
        case _ =>
      }
    }
    keepGoing
  }

  // Initialization

  def this(settings: Canvas.Settings) = {
    this()
    this.init(settings)
  }

  def unsafeInit(newSettings: Canvas.Settings) = {
    SDL_Init(SDL_INIT_VIDEO)
    changeSettings(newSettings)
  }

  def changeSettings(newSettings: Canvas.Settings) = if (extendedSettings == null || newSettings != settings) {
    extendedSettings = LowLevelCanvas.ExtendedSettings(newSettings)
    SDL_DestroyWindow(window)
    ubyteClearR = newSettings.clearColor.r.toUByte
    ubyteClearG = newSettings.clearColor.g.toUByte
    ubyteClearB = newSettings.clearColor.b.toUByte
    window = SDL_CreateWindow(
      c"Minart",
      SDL_WINDOWPOS_CENTERED,
      SDL_WINDOWPOS_CENTERED,
      extendedSettings.scaledWidth,
      extendedSettings.scaledHeight,
      if (extendedSettings.settings.fullScreen) SDL_WINDOW_FULLSCREEN_DESKTOP
      else SDL_WINDOW_SHOWN
    )
    windowSurface = SDL_GetWindowSurface(window)
    surface = new SdlSurface(
      SDL_CreateRGBSurface(0.toUInt, newSettings.width, newSettings.height, 32, 0.toUInt, 0.toUInt, 0.toUInt, 0.toUInt)
    )
    keyboardInput = KeyboardInput(Set(), Set(), Set())
    extendedSettings = extendedSettings.copy(
      windowWidth = windowSurface.w,
      windowHeight = windowSurface.h
    )
    (0 until extendedSettings.windowHeight * extendedSettings.windowWidth).foreach { i =>
      val baseAddr = i * 4
      windowSurface.pixels(baseAddr + 0) = ubyteClearB.toByte
      windowSurface.pixels(baseAddr + 1) = ubyteClearG.toByte
      windowSurface.pixels(baseAddr + 2) = ubyteClearR.toByte
      windowSurface.pixels(baseAddr + 3) = 255.toByte
    }
    clear(Set(Canvas.Buffer.Backbuffer))
  }

  // Cleanup

  def unsafeDestroy() = {
    SDL_Quit()
  }

  // Canvas operations

  def clear(buffers: Set[Canvas.Buffer]): Unit = {
    if (buffers.contains(Canvas.Buffer.KeyboardBuffer)) {
      keyboardInput = keyboardInput.clearPressRelease()
    }
    if (buffers.contains(Canvas.Buffer.PointerBuffer)) {
      pointerInput = pointerInput.clearPressRelease()
    }
    if (handleEvents() && buffers.contains(Canvas.Buffer.Backbuffer)) {
      surface.fill(settings.clearColor)
    }
  }

  def redraw(): Unit = {
    if (handleEvents()) {
      val windowRect = stackalloc[SDL_Rect]().init(
        extendedSettings.canvasX,
        extendedSettings.canvasY,
        extendedSettings.scaledWidth,
        extendedSettings.scaledHeight
      )
      SDL_UpperBlitScaled(surface.data, null, windowSurface, windowRect)
      SDL_UpdateWindowSurface(window)
    }
  }

  def getKeyboardInput(): KeyboardInput = keyboardInput
  def getPointerInput(): PointerInput   = pointerInput.move(cleanPointerPos)
}