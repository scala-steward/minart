package eu.joaocosta.minart.backend

import scala.scalajs.js

import org.scalajs.dom
import org.scalajs.dom.html.Canvas as JsCanvas
import org.scalajs.dom.{Event, KeyboardEvent, PointerEvent}

import eu.joaocosta.minart.graphics.*
import eu.joaocosta.minart.input.*

/** A low level Canvas implementation that shows the image in an HTML Canvas element.
  */
final class HtmlCanvas(parentNode: => dom.Node = dom.document.body) extends SurfaceBackedCanvas {
  // Rendering resources

  private[this] var jsCanvas: JsCanvas                                                                        = _
  private[this] var ctx: dom.CanvasRenderingContext2D                                                         = _
  private[this] var childNode: dom.Node                                                                       = _
  private[this] var globalListeners: List[(String, js.Function1[_, _])]                                       = Nil
  private[this] def registerGlobalListener[T <: Event](eventType: String, listener: js.Function1[T, _]): Unit = {
    dom.document.addEventListener[T](eventType, listener)
    globalListeners = (eventType, listener) :: globalListeners
  }
  private[this] def unregisterGlobalListeners(): Unit = {
    globalListeners.foreach { (eventType, listener) =>
      dom.document.removeEventListener(eventType, listener)
    }
    globalListeners = Nil
  }
  protected var surface: ImageDataOpaqueSurface = _

  // Input resources

  private[this] var keyboardInput: KeyboardInput                   = KeyboardInput.empty
  private[this] var pointerInput: PointerInput                     = PointerInput.empty
  private[this] var rawPointerPos: (Int, Int)                      = _
  private[this] def cleanPointerPos: Option[PointerInput.Position] = Option(rawPointerPos).flatMap { case (x, y) =>
    val (offsetX, offsetY) = {
      val canvasRect = jsCanvas.getBoundingClientRect()
      (canvasRect.left.toInt, canvasRect.top.toInt)
    }
    val xx = (x - offsetX) / extendedSettings.scale
    val yy = (y - offsetY) / extendedSettings.scale
    if (xx >= 0 && yy >= 0 && xx < settings.width && yy < settings.height)
      Some(PointerInput.Position(xx, yy))
    else None
  }

  // Initialization

  def this(settings: Canvas.Settings) = {
    this()
    this.init(settings)
  }

  protected def unsafeInit(): Unit = {
    val containerDiv = dom.document.createElement("div").asInstanceOf[dom.HTMLDivElement]
    jsCanvas = dom.document.createElement("canvas").asInstanceOf[JsCanvas]
    containerDiv.appendChild(jsCanvas)
    childNode = parentNode.appendChild(containerDiv)
    ctx =
      jsCanvas.getContext("2d", new js.Object { val alpha: Boolean = false }).asInstanceOf[dom.CanvasRenderingContext2D]
    registerGlobalListener[Event](
      "fullscreenchange",
      (_: Event) => if (dom.document.fullscreenElement == null) changeSettings(settings.copy(fullScreen = false))
    )
    registerGlobalListener[KeyboardEvent](
      "keydown",
      (ev: KeyboardEvent) => {
        JsKeyMapping.getKey(ev.keyCode).foreach(k => keyboardInput = keyboardInput.press(k))
      }
    )
    registerGlobalListener[KeyboardEvent](
      "keyup",
      (ev: KeyboardEvent) => JsKeyMapping.getKey(ev.keyCode).foreach(k => keyboardInput = keyboardInput.release(k))
    )

    def handlePress()              = { pointerInput = pointerInput.move(cleanPointerPos).press }
    def handleRelease()            = { pointerInput = pointerInput.move(cleanPointerPos).release }
    def handleMove(x: Int, y: Int) = {
      rawPointerPos = (x, y)
    }
    registerGlobalListener[PointerEvent](
      "pointerdown",
      (ev: PointerEvent) => {
        handleMove(ev.clientX.toInt, ev.clientY.toInt)
        handlePress()
      }
    )
    registerGlobalListener[PointerEvent](
      "pointerup",
      (ev: PointerEvent) => {
        handleMove(ev.clientX.toInt, ev.clientY.toInt)
        handleRelease()
      }
    )
    registerGlobalListener[PointerEvent]("pointercancel", (_: PointerEvent) => handleRelease())
    jsCanvas.addEventListener[PointerEvent](
      "pointermove",
      (ev: PointerEvent) => {
        handleMove(ev.clientX.toInt, ev.clientY.toInt)
      }
    )
  }

  protected def unsafeApplySettings(newSettings: Canvas.Settings): LowLevelCanvas.ExtendedSettings = {
    val oldSettings      = settings
    val clearColorStr    = s"rgb(${newSettings.clearColor.r},${newSettings.clearColor.g},${newSettings.clearColor.b})"
    val extendedSettings =
      LowLevelCanvas.ExtendedSettings(newSettings, dom.window.screen.width.toInt, dom.window.screen.height.toInt)
    jsCanvas.width = newSettings.width
    jsCanvas.height = newSettings.height
    jsCanvas.style =
      s"width:${extendedSettings.scaledWidth}px;height:${extendedSettings.scaledHeight}px;image-rendering:pixelated;"
    ctx.imageSmoothingEnabled = false

    jsCanvas.parentElement.style =
      if (newSettings.fullScreen)
        s"display:flex;justify-content:center;align-items:center;background:$clearColorStr;"
      else ""
    surface = new ImageDataOpaqueSurface(ctx.getImageData(0, 0, newSettings.width, newSettings.height))

    if (oldSettings.fullScreen != newSettings.fullScreen) {
      if (newSettings.fullScreen) {
        jsCanvas.parentElement.requestFullscreen()
        // Set a safe fallback on unexpected fullscreen exits
        if (oldSettings.fullScreen == false) {
          jsCanvas.parentElement.onfullscreenchange = (_: Event) =>
            if (dom.document.fullscreenElement == null && settings.fullScreen == true) {
              changeSettings(oldSettings)
            }
        }
      } else if (dom.document.fullscreenElement != null && !js.isUndefined(dom.document.fullscreenElement)) {
        jsCanvas.parentElement.onfullscreenchange = (_: Event) => ()
        dom.document.exitFullscreen()
      }
    }
    ctx.fillStyle = clearColorStr
    ctx.fillRect(0, 0, newSettings.width, newSettings.height)
    surface.fill(newSettings.clearColor)
    extendedSettings
  }

  // Cleanup

  protected def unsafeDestroy(): Unit = if (childNode != null) {
    parentNode.removeChild(childNode)
    childNode = null
    unregisterGlobalListeners()
  }

  // Canvas operations

  def clear(buffers: Set[Canvas.Buffer]): Unit = {
    if (buffers.contains(Canvas.Buffer.KeyboardBuffer)) {
      keyboardInput = keyboardInput.clearPressRelease()
    }
    if (buffers.contains(Canvas.Buffer.PointerBuffer)) {
      pointerInput = pointerInput.clearEvents()
    }
    if (buffers.contains(Canvas.Buffer.Backbuffer)) {
      surface.fill(settings.clearColor)
    }
  }

  def redraw(): Unit = {
    ctx.putImageData(surface.data, 0, 0)
  }

  def getKeyboardInput(): KeyboardInput = keyboardInput
  def getPointerInput(): PointerInput   = pointerInput.move(cleanPointerPos)

  // Html Canvas specific operations

  /** Returns the <canvas> element currently being used by this canvas.
    *
    *  Can be None if the canvas is not initialized.
    */
  def getCanvasElement(): Option[JsCanvas] =
    if (isCreated()) Option(jsCanvas)
    else None
}
