package li.cil.oc.server.component.traits

import li.cil.oc.util.BlockPosition
import li.cil.oc.util.ExtendedBlock._
import li.cil.oc.util.ExtendedWorld._
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.item.EntityMinecart
import net.minecraft.world.World
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.common.util.FakePlayer
import net.minecraftforge.common.util.ForgeDirection
import net.minecraftforge.event.world.BlockEvent
import net.minecraftforge.fluids.FluidRegistry

import scala.collection.convert.WrapAsScala._
import scala.reflect.ClassTag
import scala.reflect.classTag

trait WorldAware {
  protected def world: World

  protected def x: Int

  protected def y: Int

  protected def z: Int

  protected def fakePlayer: FakePlayer

  protected def entitiesInBlock[Type <: Entity : ClassTag](blockPos: BlockPosition) = {
    world.getEntitiesWithinAABB(classTag[Type].runtimeClass, blockPos.bounds).map(_.asInstanceOf[Type])
  }

  protected def entitiesOnSide[Type <: Entity : ClassTag](side: ForgeDirection) = {
    entitiesInBlock[Type](BlockPosition(x, y, z, Option(world)).offset(side))
  }

  protected def closestEntity[Type <: Entity : ClassTag](side: ForgeDirection) = {
    val blockPos = BlockPosition(x, y, z, Option(world)).offset(side)
    Option(world.findNearestEntityWithinAABB(classTag[Type].runtimeClass, blockPos.bounds, fakePlayer)).map(_.asInstanceOf[Type])
  }

  protected def blockContent(side: ForgeDirection) = {
    closestEntity[Entity](side) match {
      case Some(_@(_: EntityLivingBase | _: EntityMinecart)) =>
        (true, "entity")
      case _ =>
        val blockPos = BlockPosition(x, y, z, Option(world)).offset(side)
        val block = world.getBlock(blockPos)
        val metadata = world.getBlockMetadata(blockPos)
        if (block.isAir(blockPos)) {
          (false, "air")
        }
        else if (FluidRegistry.lookupFluidForBlock(block) != null) {
          val event = new BlockEvent.BreakEvent(blockPos.x, blockPos.y, blockPos.z, world, block, metadata, fakePlayer)
          MinecraftForge.EVENT_BUS.post(event)
          (event.isCanceled, "liquid")
        }
        else if (block.isReplaceable(blockPos)) {
          val event = new BlockEvent.BreakEvent(blockPos.x, blockPos.y, blockPos.z, world, block, metadata, fakePlayer)
          MinecraftForge.EVENT_BUS.post(event)
          (event.isCanceled, "replaceable")
        }
        else {
          (true, "solid")
        }
    }
  }
}