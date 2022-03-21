package eu.joaocosta.minart.graphics

import eu.joaocosta.minart.graphics.RenderLoop
import eu.joaocosta.minart.runtime._

/** A render loop that takes a side-effectful renderFrame operation. */
object ImpureRenderLoop extends RenderLoop.Builder[Function1, Function2] {
  def statefulRenderLoop[S](
      renderFrame: (Canvas, S) => S,
      frameRate: LoopFrequency,
      terminateWhen: S => Boolean = (_: S) => false
  ): RenderLoop[S] = {
    new RenderLoop[S] {
      def run(runner: LoopRunner, canvasManager: CanvasManager, canvasSettings: Canvas.Settings, initialState: S) = {
        val canvas = canvasManager.init(canvasSettings)
        runner
          .finiteLoop(
            (state: S) => renderFrame(canvas, state),
            (newState: S) => terminateWhen(newState) || !canvas.isCreated(),
            frameRate,
            () => if (canvas.isCreated()) canvas.close()
          )
          .run(initialState)
      }
    }
  }

  def statelessRenderLoop(
      renderFrame: Canvas => Unit,
      frameRate: LoopFrequency
  ): RenderLoop[Unit] =
    statefulRenderLoop[Unit]((c: Canvas, _: Unit) => renderFrame(c), frameRate)

  def singleFrame(renderFrame: Canvas => Unit): RenderLoop[Unit] = new RenderLoop[Unit] {
    def run(
        runner: LoopRunner,
        canvasManager: CanvasManager,
        canvasSettings: Canvas.Settings,
        initialState: Unit
    ): Unit = {
      val canvas = canvasManager.init(canvasSettings)
      runner.singleRun(() => renderFrame(canvas), () => if (canvas.isCreated()) canvas.close()).run()
    }
  }
}
