package eu.joaocosta.minart.audio

import eu.joaocosta.minart.backend.defaults.*

/** Multi-channel mono audio player.
  *
  * Can play and stop audio clips and audio waves on distinct channels.
  */
trait AudioPlayer {

  /** Enqueues an audio clip to be played later in channel 0.
    *
    *  @param clip audio clip to play
    */
  final def play(clip: AudioClip): Unit = play(clip, 0)

  /** Enqueues an audio clip to be played later in a certain channel.
    *
    *  @param clip audio clip to play
    *  @param channel channel where to play the audio clip
    */
  def play(clip: AudioClip, channel: Int): Unit

  /** Enqueues an audio wave to be played later in channel 0.
    * The Audio Wave will play infinitely until stop() is called.
    *
    *  @param wave audio wave to play
    */
  final def play(wave: AudioWave): Unit = play(wave, 0)

  /** Enqueues an audio wave to be played later in a certain channel.
    * The Audio Wave will play infinitely until stop() is called.
    *
    * @param wave audio wave to play
    * @param channel channel where to play the audio wave
    */
  final def play(wave: AudioWave, channel: Int): Unit =
    play(AudioClip(wave, Double.PositiveInfinity), channel)

  /** Checks if this player still has data to be played.
    *
    *  @return true of the player is still playing, false otherwise
    */
  def isPlaying(): Boolean

  /** Checks if a channel still has data to be played.
    *
    *  @param channel channel to check
    *  @return true of the channel is still playing, false otherwise
    */
  def isPlaying(channel: Int): Boolean

  /** Stops playback and removes all enqueued waves.
    */
  def stop(): Unit

  /** Stops playback and removes all enqueued waves in a certain channel.
    *
    *  @param channel channel to stop
    */
  def stop(channel: Int): Unit

  /** Stops channel 0 and plays an audio clip right away.
    *
    *  @param clip audio clip to play
    */
  final def playNow(clip: AudioClip): Unit = playNow(clip, 0)

  /** Stops a certain channel and plays an audio clip right away.
    *
    *  @param clip audio clip to play
    *  @param channel channel where to play the audio clip
    */
  final def playNow(clip: AudioClip, channel: Int): Unit = {
    stop(channel)
    play(clip, channel)
  }

  /** Stops channel 0 and plays an audio wave.
    * The Audio Wave will play infinitely until stop() is called.
    *
    *  @param wave audio wave to play
    */
  final def playNow(wave: AudioWave): Unit = playNow(wave, 0)

  /** Stops a certain channel and plays an audio wave.
    * The Audio Wave will play infinitely until stop() is called.
    *
    * @param wave audio wave to play
    * @param channel channel where to play the audio wave
    */
  final def playNow(wave: AudioWave, channel: Int): Unit =
    playNow(AudioClip(wave, Double.PositiveInfinity), channel)

  /** Gets the mixing definitions for a channel.
    *
    * @param channel channel to check
    */
  def getChannelMix(channel: Int): AudioMix

  /** Sets the mixing definitions for a channel.
    *
    * @param mix the new mixing definitions
    * @param channel channel to update
    */
  def setChannelMix(mix: AudioMix, channel: Int): Unit

  /** Updates the mixing definitions for a channel based on the current definitions.
    *
    * @param f update function
    * @param channel channel to update
    * @return the new audio mix
    */
  final def updateChannelMix(f: AudioMix => AudioMix, channel: Int): AudioMix = {
    val currentMix = getChannelMix(channel)
    val newMix     = f(currentMix)
    setChannelMix(newMix, channel)
    newMix
  }

}

object AudioPlayer {

  /** Returns a new [[AudioPlayer]] for the default backend for the target platform.
    *
    * @param settings settings for this audio player, such as the sample rate and buffer size
    * @return [[AudioPlayer]] using the default backend for the target platform
    */
  def create(settings: AudioPlayer.Settings)(using backend: DefaultBackend[Any, LowLevelAudioPlayer]): AudioPlayer =
    LowLevelAudioPlayer.create().init(settings)

  final case class Settings(sampleRate: Int = 44100, bufferSize: Int = 4096)
}
