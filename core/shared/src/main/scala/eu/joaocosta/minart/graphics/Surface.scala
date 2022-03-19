package eu.joaocosta.minart.graphics

/** A Surface is an object that contains a set of pixels.
  */
trait Surface {

  /** The surface width */
  def width: Int

  /** The surface height */
  def height: Int

  /** A view over this surface */
  def view: SurfaceView = new SurfaceView.IdentityView(this)

  /** Gets the color from the this surface.
    * This operation can be perfomance intensive, so it might be worthwile
    * to either use `getPixels` to fetch multiple pixels at the same time or
    * to implement this operation on the application code.
    *
    * @param x pixel x position
    * @param y pixel y position
    * @return pixel color
    */
  def getPixel(x: Int, y: Int): Option[Color]

  /** Returns the pixels from this surface.
    * This operation can be perfomance intensive, so it might be worthwile
    * to implement this operation on the application code.
    *
    * @return color matrix
    */
  def getPixels(): Vector[Array[Color]]

  /** Copies this surface into a new surface stored in RAM */
  def toRamSurface(): RamSurface =
    new RamSurface(getPixels())
}

object Surface {

  /** A surface that can be drawn on using mutable operations.
    */
  trait MutableSurface extends Surface {

    /** Put a pixel in the surface with a certain color.
      *
      * @param x pixel x position
      * @param y pixel y position
      * @param color `Color` to apply to the pixel
      */
    def putPixel(x: Int, y: Int, color: Color): Unit

    /** Fill the surface with a certain color
      *
      * @param color `Color` to fill the surface with
      */
    def fill(color: Color): Unit

    private def unsafeBlit(
        that: Surface,
        mask: Option[Color],
        x: Int,
        y: Int,
        cx: Int,
        cy: Int,
        minX: Int,
        minY: Int,
        maxX: Int,
        maxY: Int
    ): Unit = {
      val thatPixels = that.getPixels()
      var dy         = minY
      mask match {
        case None =>
          while (dy < maxY) {
            val line  = thatPixels(dy + cy)
            val destY = dy + y
            var dx    = minX
            while (dx < maxX) {
              val destX = dx + x
              val color = line(dx + cx)
              putPixel(destX, destY, color)
              dx += 1
            }
            dy += 1
          }
        case Some(maskColor) =>
          while (dy < maxY) {
            val line  = thatPixels(dy + cy)
            val destY = dy + y
            var dx    = minX
            while (dx < maxX) {
              val destX = dx + x
              val color = line(dx + cx)
              if (color != maskColor) putPixel(destX, destY, color)
              putPixel(destX, destY, color)
              dx += 1
            }
            dy += 1
          }
      }
    }

    /** Draws a surface on top of this surface.
      *
      * @param that surface to draw
      * @param mask color to use as a mask (pixels with this color won't be merged)
      * @param x leftmost pixel on the destination surface
      * @param y topmost pixel on the destination surface
      * @param cx leftmost pixel on the source surface
      * @param cy topmost pixel on the source surface
      * @param cw clip width of the source surface
      * @param ch clip height of the source surface
      */
    def blit(
        that: Surface,
        mask: Option[Color] = None
    )(x: Int, y: Int, cx: Int = 0, cy: Int = 0, cw: Int = that.width, ch: Int = that.height): Unit = {
      // Handle clips with a negative origin
      if (cx < 0) blit(that)(x - cx, y, 0, cy, cw + cx, ch)
      else if (cy < 0) blit(that)(x, y - cy, cx, 0, cw, ch + cy)
      else {
        // Where can we start reading the source (handle cases where x or y are negative)
        val minX = math.max(0, -x)
        val minY = math.max(0, -y)

        // How far can we read from the source (adjusts cw/ch to cx/cy)
        val clampCw = math.max(0, math.min(cw, that.width - cx))
        val clampCh = math.max(0, math.min(ch, that.height - cy))

        // How far can we read from the source (handle cases where x or y are positive)
        val maxX = math.min(clampCw, this.width - x)
        val maxY = math.min(clampCh, this.height - y)

        if (that.isInstanceOf[SurfaceView]) {
          unsafeBlit(that.view.clip(cx, cy, clampCw, clampCh), mask, x, y, 0, 0, 0, 0, clampCw, clampCh)
        } else {
          unsafeBlit(that, mask, x, y, cx, cy, minX, minY, maxX, maxY)
        }
      }
    }

    /** Draws a surface on top of this surface and masks the pixels with a certain color.
      *
      * @param that surface to draw
      * @param mask color to usa as a mask
      * @param x leftmost pixel on the destination surface
      * @param y topmost pixel on the destination surface
      * @param cx leftmost pixel on the source surface
      * @param cy topmost pixel on the source surface
      * @param cw clip width of the source surface
      * @param ch clip height of the source surface
      */
    @deprecated("Use blit instead")
    def blitWithMask(
        that: Surface,
        mask: Color
    )(x: Int, y: Int, cx: Int = 0, cy: Int = 0, cw: Int = that.width, ch: Int = that.height): Unit = {
      blit(that, Some(mask))(x, y, cx, cy, cw, ch)
    }
  }
}
