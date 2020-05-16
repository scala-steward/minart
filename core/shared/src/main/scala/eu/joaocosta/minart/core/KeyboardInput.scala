package eu.joaocosta.minart.core

import eu.joaocosta.minart.core.KeyboardInput.Key

case class KeyboardInput(keysDown: Set[Key], keysPressed: Set[Key], keysReleased: Set[Key]) {
  def isDown(key: Key): Boolean = keysDown(key)
  def isUp(key: Key): Boolean = !keysDown(key)
  def press(key: Key): KeyboardInput = KeyboardInput(keysDown + key, keysPressed + key, keysReleased - key)
  def release(key: Key): KeyboardInput = KeyboardInput(keysDown - key, keysPressed - key, keysReleased + key)
  def clearPressRelease(): KeyboardInput = KeyboardInput(keysDown, Set(), Set())
}

object KeyboardInput {
  sealed trait Key
  object Key {
    final case object Up extends Key
    final case object Down extends Key
    final case object Left extends Key
    final case object Right extends Key
  }
}
