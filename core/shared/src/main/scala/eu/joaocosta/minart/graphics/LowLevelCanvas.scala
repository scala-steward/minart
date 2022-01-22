package eu.joaocosta.minart.graphics

import eu.joaocosta.minart.backend.defaults._

/** A low-level version of a canvas that provides its own canvas manager.
  */
trait LowLevelCanvas extends Canvas with AutoCloseable {
  protected[this] var extendedSettings: LowLevelCanvas.ExtendedSettings = _
  def settings: Canvas.Settings =
    if (extendedSettings == null) Canvas.Settings(0, 0)
    else extendedSettings.settings

  protected def unsafeInit(settings: Canvas.Settings): Unit
  protected def unsafeDestroy(): Unit

  /** Checks if the window is created or if it has been destroyed
    */
  def isCreated(): Boolean = !(extendedSettings == null)

  /** Creates the canvas window.
    *
    * Rendering operations can only be called after calling this.
    *
    * @return canvas object linked to the created window
    */
  def init(settings: Canvas.Settings): Unit = {
    if (isCreated()) {
      close()
    }
    if (!isCreated()) {
      unsafeInit(settings)
    }
  }

  /** Destroys the canvas window.
    *
    * Calling any operation on this canvas after calling close without calling
    * init() has an undefined behavior.
    */
  def close(): Unit = if (isCreated()) {
    unsafeDestroy()
    extendedSettings = null
  }
}

object LowLevelCanvas {

  /** Returns a new [[LowLevelCanvas]] for the default backend for the target platform.
    *
    * @return [[LowLevelCanvas]] using the default backend for the target platform
    */
  def create(): LowLevelCanvas =
    DefaultBackend[Any, LowLevelCanvas].defaultValue()

  /** Internal data structure containing canvas settings and precomputed values.
    */
  case class ExtendedSettings(
      settings: Canvas.Settings,
      windowWidth: Int,
      windowHeight: Int
  ) {
    val scaledWidth  = settings.width * settings.scale
    val scaledHeight = settings.height * settings.scale
    val canvasX      = (windowWidth - scaledWidth) / 2
    val canvasY      = (windowHeight - scaledHeight) / 2
  }

  object ExtendedSettings {
    def apply(settings: Canvas.Settings): ExtendedSettings =
      ExtendedSettings(settings, settings.width * settings.scale, settings.height * settings.scale)
  }
}
