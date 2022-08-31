//> using scala "3.1.3"
//> using lib "eu.joaocosta::minart::0.4.3-SNAPSHOT"

/** On some occasions, we might need to change our canvas settings.
  * Here's how to do it.
  */
import eu.joaocosta.minart.backend.defaults._
import eu.joaocosta.minart.graphics._
import eu.joaocosta.minart.input.KeyboardInput.Key
import eu.joaocosta.minart.runtime._

/** Let's define some settings.
  * Note that one of those will actually go fullScreen.
  */
val settingsA = Canvas.Settings(width = 128, height = 128, scale = 4, clearColor = Color(128, 255, 128))
val settingsB = Canvas.Settings(width = 640, height = 480, scale = 1, clearColor = Color(128, 255, 128))
val settingsC = Canvas.Settings(width = 128, height = 128, scale = 4, fullScreen = true, clearColor = Color(128, 255, 128))
val settingsD = Canvas.Settings(width = 640, height = 480, scale = 1, fullScreen = true, clearColor = Color(0, 0, 0))

ImpureRenderLoop
  .statelessRenderLoop(
    canvas => {
      val keyboardInput = canvas.getKeyboardInput()
      canvas.clear()
      // To change the canvas settings, simply call changeSettings
      if (keyboardInput.keysPressed(Key.A)) canvas.changeSettings(settingsA)
      else if (keyboardInput.keysPressed(Key.B)) canvas.changeSettings(settingsB)
      else if (keyboardInput.keysPressed(Key.C)) canvas.changeSettings(settingsC)
      else if (keyboardInput.keysPressed(Key.D)) canvas.changeSettings(settingsD)
      // The canvas width and height will be updated accordingly
      for {
        x <- (0 until canvas.width)
        y <- (0 until canvas.height)
      } {
        val color =
          Color((255 * x.toDouble / canvas.width).toInt, (255 * y.toDouble / canvas.height).toInt, 255)
        canvas.putPixel(x, y, color)
      }
      canvas.redraw()
    },
    LoopFrequency.Uncapped
  )
  .run(settingsA)
