package li.cil.oc.server.component

import li.cil.oc.api.network.{Arguments, Context, LuaCallback}
import li.cil.oc.common.tileentity.BundledRedstone

class BundledRedstoneCard(override val owner: BundledRedstone) extends RedstoneCard(owner) {

  @LuaCallback(value = "getBundledInput", direct = true)
  def getBundledInput(context: Context, args: Arguments): Array[AnyRef] = {
    val side = checkSide(args, 0)
    val color = checkColor(args, 1)
    result(owner.bundledInput(side, color))
  }

  @LuaCallback(value = "getBundledOutput", direct = true)
  def getBundledOutput(context: Context, args: Arguments): Array[AnyRef] = {
    val side = checkSide(args, 0)
    val color = checkColor(args, 1)
    result(owner.bundledOutput(side, color))
  }

  @LuaCallback("setBundledOutput")
  def setBundledOutput(context: Context, args: Arguments): Array[AnyRef] = {
    val side = checkSide(args, 0)
    val color = checkColor(args, 1)
    val value = args.checkInteger(2)
    owner.bundledOutput(side, color, value)
    result(owner.bundledOutput(side, color))
  }

  // ----------------------------------------------------------------------- //

  private def checkColor(args: Arguments, index: Int): Int = {
    val color = args.checkInteger(index)
    if (color < 0 || color > 15)
      throw new IllegalArgumentException("invalid color")
    color
  }
}
