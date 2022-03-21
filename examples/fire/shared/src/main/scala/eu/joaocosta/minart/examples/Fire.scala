package eu.joaocosta.minart.examples

import eu.joaocosta.minart.backend.defaults._
import eu.joaocosta.minart.graphics._
import eu.joaocosta.minart.input._
import eu.joaocosta.minart.runtime._

object Fire {
  val canvasSettings = Canvas.Settings(width = 128, height = 128, scale = 4)

  def main(args: Array[String]): Unit = {

    var temperatureMod = 1.0

    def automata(backbuffer: Vector[Array[Color]], x: Int, y: Int, w: Int): Color = {
      val neighbors =
        (math.max(0, x - 1) to math.min(x + 1, w - 1)).toList.map { xx =>
          backbuffer(y + 1)(xx)
        }
      val randomLoss  = 0.8 + (scala.util.Random.nextDouble() / 5)
      val temperature = ((neighbors.map(c => (c.r + c.g + c.b) / 3).sum / 3) * randomLoss).toInt
      Color(
        math.min(255, temperature * 1.6 * temperatureMod).toInt,
        (temperature * 0.8 * temperatureMod).toInt,
        (temperature * 0.6 * temperatureMod).toInt
      )
    }

    ImpureRenderLoop
      .statelessRenderLoop(
        canvas => {
          val keys = canvas.getKeyboardInput()
          if (keys.isDown(KeyboardInput.Key.Up)) temperatureMod = math.min(temperatureMod + 0.1, 1.0)
          else if (keys.isDown(KeyboardInput.Key.Down)) temperatureMod = math.max(0.1, temperatureMod - 0.1)
          // Add bottom fire root
          for {
            x <- (0 until canvas.width)
            y <- (canvas.height - 4 until canvas.height)
          } {
            canvas.putPixel(x, y, Color(255, 255, 255))
          }

          // Add middle fire root
          for {
            x <- (0 until canvas.width)
            y <- (0 until canvas.height)
            dist = math.pow(x - canvas.width / 2, 2) + math.pow(y - canvas.height / 2, 2)
          } {
            if (dist > 25 && dist <= 100) canvas.putPixel(x, y, Color(255, 255, 255))
          }

          // Evolve fire
          val backbuffer = canvas.getPixels()
          for {
            x <- (0 until canvas.width)
            y <- (0 until (canvas.height - 1)).reverse
          } {
            val color = automata(backbuffer, x, y, canvas.width)
            canvas.putPixel(x, y, color)
          }
          canvas.redraw()
        },
        LoopFrequency.hz60
      )
      .run(canvasSettings)
  }
}
