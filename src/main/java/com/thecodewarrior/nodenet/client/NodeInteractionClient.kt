package com.thecodewarrior.nodenet.client

import com.thecodewarrior.nodenet.common.item.INodeInteractingItem
import com.thecodewarrior.nodenet.common.node.Node
import com.thecodewarrior.nodenet.common.node.NodeTraceResult
import com.thecodewarrior.nodenet.common.node.rayTraceNodes
import net.minecraft.client.Minecraft
import net.minecraft.client.settings.KeyBinding
import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumHand
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.RayTraceResult
import net.minecraft.util.math.Vec3d
import net.minecraftforge.common.MinecraftForge

object NodeInteractionClient {
    init { MinecraftForge.EVENT_BUS.register(this) }
    var nodeMouseOver: NodeTraceResult? = null

    @JvmStatic
    fun getMouseOver(partialTicks: Float) {
        val player = Minecraft.getMinecraft().player ?: return
        nodeMouseOver = player.rayTraceNodes(partialTicks)

        if(nodeMouseOver != null && player.heldEquipment.any { it.item is INodeInteractingItem }) {
            Minecraft.getMinecraft().objectMouseOver = RayTraceResult(RayTraceResult.Type.MISS, Vec3d.ZERO,
                    EnumFacing.UP, BlockPos.ORIGIN)
        }
    }

    /**
     * return true to skip setting `Minecraft.objectMouseOver` in `EntityRenderer.getMouseOver`
     */
    @JvmStatic
    fun shouldSkipSettingObjectMouseOver(partialTicks: Float): Boolean {
        return nodeMouseOver != null && Minecraft.getMinecraft().player.heldEquipment.any { it.item is INodeInteractingItem }
    }

    private var rightDown = false
    private var leftDown = false
    private var middleDown = false

    /**
     * Return true to skip normal handling
     */
    @JvmStatic
    fun processKeyBinds(): Boolean {
        val mc = Minecraft.getMinecraft()
        val nodeInteractingHand = mc.player.heldEquipment.indexOfFirst { it.item is INodeInteractingItem }
        if(nodeInteractingHand < 0) {
            return false
        }

        if(!rightDown && mc.gameSettings.keyBindUseItem.isKeyDown) {
            if(nodeMouseOver != null) {
                rightDown = mc.gameSettings.keyBindUseItem.isKeyDown
                (mc.player.heldItemMainhand.item as? INodeInteractingItem)
                    ?.rightClickBegan(nodeMouseOver)
            }
        } else if(rightDown && !mc.gameSettings.keyBindUseItem.isKeyDown) {
            rightDown = false
            (mc.player.heldItemMainhand.item as? INodeInteractingItem)
                ?.rightClickEnded(nodeMouseOver)
        } else if(rightDown) {
            (mc.player.heldItemMainhand.item as? INodeInteractingItem)
                    ?.rightClickTick(nodeMouseOver)
        }

        if(!middleDown && mc.gameSettings.keyBindPickBlock.isKeyDown) {
            if(nodeMouseOver != null) {
                middleDown = mc.gameSettings.keyBindPickBlock.isKeyDown
                (mc.player.heldItemMainhand.item as? INodeInteractingItem)
                    ?.middleClickBegan(nodeMouseOver)
            }
        } else if(middleDown && !mc.gameSettings.keyBindPickBlock.isKeyDown) {
            middleDown = false
            (mc.player.heldItemMainhand.item as? INodeInteractingItem)
                ?.middleClickEnded(nodeMouseOver)
        } else if(middleDown) {
            (mc.player.heldItemMainhand.item as? INodeInteractingItem)
                ?.middleClickTick(nodeMouseOver)
        }

        if(!leftDown && mc.gameSettings.keyBindAttack.isKeyDown) {
            if(nodeMouseOver != null) {
                leftDown = mc.gameSettings.keyBindAttack.isKeyDown
                (mc.player.heldItemMainhand.item as? INodeInteractingItem)
                    ?.leftClickBegan(nodeMouseOver)
            }
        } else if(leftDown && !mc.gameSettings.keyBindAttack.isKeyDown) {
            leftDown = false
            (mc.player.heldItemMainhand.item as? INodeInteractingItem)
                ?.leftClickEnded(nodeMouseOver)
        } else if(leftDown) {
            (mc.player.heldItemMainhand.item as? INodeInteractingItem)
                ?.leftClickTick(nodeMouseOver)
        }

        if(rightDown || leftDown || middleDown) {
            mc.player.activeHand = EnumHand.values()[nodeInteractingHand]
        } else {
            mc.player.resetActiveHand()
        }

        if(rightDown) mc.gameSettings.keyBindUseItem.clearPressed()
        if(leftDown) mc.gameSettings.keyBindAttack.clearPressed()
        if(middleDown) mc.gameSettings.keyBindPickBlock.clearPressed()

//        if (mc.player.getHeldItem(mc.player.activeHand).item is INodeInteractingItem) {
//            // process continuous use of item
//        }
//
//        if (mc.player.isHandActive) {
//
//            if (!mc.gameSettings.keyBindUseItem.isKeyDown) {
//                mc.playerController.onStoppedUsingItem(mc.player);
//            }
//
//            label109@ while (true) {
//                if (!mc.gameSettings.keyBindAttack.isPressed) {
//                    while (mc.gameSettings.keyBindUseItem.isPressed) {
//                    }
//
//                    while (true) {
//                        if (mc.gameSettings.keyBindPickBlock.isPressed) {
//                            continue
//                        }
//
//                        break@label109
//                    }
//                }
//            }
//        } else {
//            while (mc.gameSettings.keyBindAttack.isPressed) {
//                mc.clickMouse();
//            }
//
//            while (mc.gameSettings.keyBindUseItem.isPressed) {
//                mc.rightClickMouse();
//            }
//
//            while (mc.gameSettings.keyBindPickBlock.isPressed) {
//                mc.middleClickMouse();
//            }
//        }
//
//        val player = Minecraft.getMinecraft().player
//        val isCustomInteractable = nodeMouseOver != null &&
//                &&
//                !EnumHand.values().any { player.getHeldItem(it).item !is INodeInteractingItem }
        return false
    }


}

fun KeyBinding.clearPressed() {
    while(this.isPressed) {}
}
