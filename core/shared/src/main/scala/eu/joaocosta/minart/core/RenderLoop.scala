package eu.joaocosta.minart.core

import eu.joaocosta.minart.backend.defaults.DefaultBackend

/**
 * The `RenderLoop` contains a set of helpful methods to implement basic render
 * loops in a platform agonstic way.
 */
trait RenderLoop[F1[-_, +_], F2[-_, -_, +_]] {

  /**
   * Creates a render loop that terminates when a certain condition is reached.
   *
   * Each loop iteration receives and passes an updated state.
   *
   * @param canvasManager Canvas manager to use in the render loop
   * @param initialState Initial state when the loop starts
   * @param renderFrame Operation to render the frame and update the state
   * @param terminateWhen Loop termination check
   * @param frameRate Frame rate limit
   */
  def finiteRenderLoop[S](
    canvasManager: CanvasManager,
    initialState: S,
    renderFrame: F2[Canvas, S, S],
    terminateWhen: S => Boolean,
    frameRate: FrameRate): Unit

  /**
   * Creates a render loop that never terminates.
   *
   * Each loop iteration receives and passes an updated state.
   *
   * @param canvasManager Canvas manager to use in the render loop
   * @param initialState Initial state when the loop starts
   * @param renderFrame Operation to render the frame and update the state
   * @param frameRate Frame rate limit
   */
  def infiniteRenderLoop[S](
    canvasManager: CanvasManager,
    initialState: S,
    renderFrame: F2[Canvas, S, S],
    frameRate: FrameRate): Unit

  /**
   * Creates a render loop that never terminates.
   *
   * @param canvasManager Canvas manager to use in the render loop
   * @param renderFrame Operation to render the frame
   * @param frameRate Frame rate limit
   */
  def infiniteRenderLoop(
    canvasManager: CanvasManager,
    renderFrame: F1[Canvas, Unit],
    frameRate: FrameRate): Unit

  /**
   * Renders a single frame
   *
   * @param canvas Canvas manager to use in the render loop
   * @param renderFrame Operation to render the frame and update the state
   */
  def singleFrame(
    canvasManager: CanvasManager,
    renderFrame: F1[Canvas, Unit]): Unit
}

object RenderLoop {

  trait ImpureRenderLoop extends RenderLoop[Function1, Function2] {
    def infiniteRenderLoop[S](
      canvasManager: CanvasManager,
      initialState: S,
      renderFrame: (Canvas, S) => S,
      frameRate: FrameRate): Unit =
      finiteRenderLoop(canvasManager, initialState, renderFrame, (_: S) => false, frameRate)

    def infiniteRenderLoop(
      canvasManager: CanvasManager,
      renderFrame: Canvas => Unit,
      frameRate: FrameRate): Unit =
      infiniteRenderLoop(canvasManager, (), (c: Canvas, _: Unit) => renderFrame(c), frameRate)

  }

  /**
   * Returns [[RenderLoop]] for the default backend for the target platform.
   */
  def default()(implicit d: DefaultBackend[Any, ImpureRenderLoop]): ImpureRenderLoop =
    d.defaultValue(())
}
