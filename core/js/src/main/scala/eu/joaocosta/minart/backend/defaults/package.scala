package eu.joaocosta.minart.backend

import eu.joaocosta.minart.runtime.Platform

package object defaults {
  implicit val defaultCanvas: DefaultBackend[Any, HtmlCanvas] =
    DefaultBackend.fromFunction((_) => new HtmlCanvas())

  implicit val defaultLoopRunner: DefaultBackend[Any, JsLoopRunner.type] =
    DefaultBackend.fromConstant(JsLoopRunner)

  implicit val defaultPlatform: DefaultBackend[Any, Platform.JS.type] =
    DefaultBackend.fromConstant(Platform.JS)

  implicit val defaultResourceLoader: DefaultBackend[Any, JsResourceLoader.type] =
    DefaultBackend.fromConstant(JsResourceLoader)
}
