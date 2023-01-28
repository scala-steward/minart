package eu.joaocosta.minart.graphics

import scala.concurrent.{ExecutionContext, Future}

import eu.joaocosta.minart.backend.defaults._
import eu.joaocosta.minart.backend.subsystem._
import eu.joaocosta.minart.runtime._

/** Render loop that keeps an internal state that is passed to every iteration.
  */
trait RenderLoop[State, Subsystem] {

  /** Runs this render loop with a custom loop runner and a set of subsystems.
    *
    * @param runner custom loop runner to use
    * @param createSubsystems operation to create the subsystems
    */
  def run(
      runner: LoopRunner,
      createSubsystems: () => Subsystem
  ): Future[State]

  /** Runs this render loop usinf the default loop runner and subsystems.
    */
  final def run()(implicit
      lr: DefaultBackend[Any, LoopRunner],
      ss: DefaultBackend[Any, Subsystem]
  ): Future[State] =
    run(LoopRunner(), () => ss.defaultValue())
}

object RenderLoop {

  /** Contains a set of helpful methods to implement basic render
    * loops in a platform agonstic way.
    *
    * @tparam F1 effect type for stateless loops
    * @tparam F2 effect type for stateful loops
    */
  trait Builder[F1[-_, +_], F2[-_, -_, +_]] {

    /** Creates a render loop that keeps and updates a state on every iteration,
      *  terminating when a certain condition is reached.
      *
      * @param renderFrame operation to render the frame and update the state
      * @param terminateWhen loop termination check
      */
    def statefulLoop[State, Settings, Subsystem <: LowLevelSubsystem[Settings]](
        renderFrame: F2[Subsystem, State, State],
        terminateWhen: State => Boolean = (_: State) => false
    ): RenderLoop.Definition[State, Settings, Subsystem]

    /** Creates a render loop that keeps no state.
      *
      * @param renderFrame operation to render the frame
      * @param frameRate frame rate limit
      */
    def statelessLoop[Settings, Subsystem <: LowLevelSubsystem[Settings]](
        renderFrame: F1[Subsystem, Unit]
    ): RenderLoop.Definition[Unit, Settings, Subsystem]

    def statefulRenderLoop[State](
        renderFrame: F2[Canvas, State, State],
        terminateWhen: State => Boolean = (_: State) => false
    ): RenderLoop.Definition[State, Canvas.Settings, LowLevelCanvas] =
      statefulLoop[State, Canvas.Settings, LowLevelCanvas](
        renderFrame,
        terminateWhen
      )

    def statelessRenderLoop(
        renderFrame: F1[Canvas, Unit]
    ): RenderLoop.Definition[Unit, Canvas.Settings, LowLevelCanvas] =
      statelessLoop[Canvas.Settings, LowLevelCanvas](
        renderFrame
      )
  }

  /** Render loop definition that takes the initial settings, initial state
    * and loop frequency.
    */
  trait Definition[State, Settings, Subsystem] {

    /** Applies the following definitions to the render loop */
    def withDefinitions(
        initialSettings: Settings,
        frameRate: LoopFrequency,
        initialState: State
    ): RenderLoop[State, Subsystem]

    /** Applies the following definitions to the render loop */
    def withDefinitions(
        initialSettings: Settings,
        frameRate: LoopFrequency
    )(implicit ev: Unit =:= State): RenderLoop[State, Subsystem] = withDefinitions(initialSettings, frameRate, ev(()))

  }
}
