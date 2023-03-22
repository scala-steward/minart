package eu.joaocosta.minart.audio

/** Oscilator used to create periodic waves.
  *
  * @param generator function used to generate a wave based on a frequency
  */
final case class Oscilator(generator: Double => AudioWave) extends (Double => AudioWave) {

  /** Generates an AudioWave with a certain frequency
    *
    *  @param frequency audio wave frequency
    */
  def apply(frequency: Double): AudioWave = generate(frequency)

  /** Generates an AudioWave with a certain frequency and amplitude
    *  @param frequency audio wave frequency
    *  @param ampitude amplitude of the generated wave
    */
  def generate(frequency: Double, amplitude: Double = 1.0): AudioWave =
    if (frequency == 0.0) AudioWave.silence
    else if (amplitude == 1.0) generator(frequency)
    else generate(frequency).map(_ * amplitude)

  /** Generates a audio clip with a certain duration.
    *
    * It also allows rounding the duration to the closest cycle, to smoothly merge results from oscilators.
    *
    * @param duration duration of the clip
    * @param frequncy frequency to play
    * @param amplitude amplitude of the wave
    * @param roundDuration weather the duration should be rounded down to match a oscilator cycle
    */
  def generateClip(
      duration: Double,
      frequency: Double,
      amplitude: Double = 1.0,
      roundDuration: Boolean = false
  ): AudioClip = {
    val finalDuration = if (roundDuration) ((duration * frequency).toInt / frequency) else duration
    generate(frequency, amplitude).take(finalDuration)
  }

  /** Maps the waves generated by this oscilator
    *
    * @param f function that maps one amplitude to another
    * @return a new oscilator that generates the transformed waves
    */
  def map(f: Double => Double): Oscilator =
    Oscilator(frequency => generator(frequency).map(f))
}

object Oscilator {

  /** Sin wave oscilator */
  val sin: Oscilator =
    Oscilator { frequency =>
      val k = frequency * 2 * math.Pi
      AudioWave(t => math.sin(k * t))
    }

  /** Square wave oscilator */
  val square: Oscilator = sin.map(math.signum)

  private def floorMod(x: Double, y: Double): Double = {
    val rem = x % y
    if (rem >= 0) rem
    else rem + y
  }

  /** Sawtooth wave oscilator */
  val sawtooth: Oscilator =
    Oscilator(frequency => AudioWave(t => 2 * floorMod(t * frequency, 1) - 1))
}