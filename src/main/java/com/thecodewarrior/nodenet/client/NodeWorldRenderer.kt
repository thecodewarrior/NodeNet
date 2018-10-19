package com.thecodewarrior.nodenet.client

import com.teamwizardry.librarianlib.features.forgeevents.CustomWorldRenderEvent
import com.teamwizardry.librarianlib.features.helpers.vec
import com.teamwizardry.librarianlib.features.kotlin.*
import com.thecodewarrior.nodenet.common.entity.EntityNode
import com.thecodewarrior.nodenet.common.item.INodeInteractingItem
import com.thecodewarrior.nodenet.common.item.INodeVisibleItem
import com.thecodewarrior.nodenet.common.item.ModItems
import com.thecodewarrior.nodenet.drawing
import com.thecodewarrior.nodenet.edges
import com.thecodewarrior.nodenet.renderPosition
import com.thecodewarrior.nodenet.rotationPitch
import com.thecodewarrior.nodenet.rotationYaw
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.Vec3d
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import org.lwjgl.opengl.GL11
import java.awt.Color

object NodeWorldRenderer {
    init { MinecraftForge.EVENT_BUS.register(this) }

    @SubscribeEvent
    fun renderWorld(e: CustomWorldRenderEvent) {
        val player = Minecraft.getMinecraft().player
        if(player.heldItemMainhand.item !is INodeVisibleItem) return
        val entities = e.world.getEntities(EntityNode::class.java, { true })

        val tessellator = Tessellator.getInstance()
        val vb = tessellator.buffer

        GlStateManager.depthFunc(GL11.GL_ALWAYS)
        vb.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR)
        entities.forEach { entity ->
            val connected = entity.connectedEntities()
            if(connected.isNotEmpty()) {
                connected.forEach {
                    if(entity.hashCode() <= it.hashCode()) { // only render line for one or the other half
                        vb.pos(entity.positionVector).color(Color.cyan).endVertex()
                        vb.pos(it.positionVector).color(Color.cyan).endVertex()
                    }
                }
            }
        }
        tessellator.draw()
        GlStateManager.depthMask(true)
        drawForEach(entities, e.partialTicks) { entity ->
            if(NodeInteractionClient.nodeMouseOver?.entity == entity) {
                GlStateManager.depthFunc(GL11.GL_ALWAYS)
            } else {
                GlStateManager.depthFunc(GL11.GL_LEQUAL)
            }
            renderNode(entity, e.partialTicks)
        }
        GlStateManager.depthFunc(GL11.GL_LEQUAL)

        if(player.heldItemMainhand.item == ModItems.connector) {
            ModItems.connector.connectingFromNode?.let { e.world.getEntityByID(it) }?.let { source ->
                val startPos = source.positionVector

                val mouseOver = NodeInteractionClient.nodeMouseOver
                val endPos = if(mouseOver != null) {
                    mouseOver.entity.positionVector
                } else {
                    player.getPositionEyes(e.partialTicks) + (player.getLook(e.partialTicks) * 2)
                }

                GlStateManager.depthFunc(GL11.GL_ALWAYS)
                vb.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR)
                vb.pos(startPos).color(Color.green).endVertex()
                vb.pos(endPos).color(Color.green).endVertex()
                tessellator.draw()
                GlStateManager.depthFunc(GL11.GL_LEQUAL)
            }
        }

        GlStateManager.depthFunc(GL11.GL_LEQUAL)
    }

    fun drawForEach(entities: List<EntityNode>, partialTicks: Float, draw: (entity: EntityNode) -> Unit) {
        val tinyOffset = vec(1e-3, 1e-3, 1e-3)
        entities.forEach { entity ->
            GlStateManager.pushMatrix()

            val renderPos = entity.positionVector + tinyOffset
            GlStateManager.translate(renderPos.x, renderPos.y, renderPos.z)

            draw(entity)

            GlStateManager.popMatrix()
        }
    }

    fun renderNode(node: EntityNode, partialTicks: Float) {

        val relativePosition = node.positionVector - Minecraft.getMinecraft().player.renderPosition(partialTicks)
        val radius = node.visualRadius(relativePosition.lengthVector())
        val c = Color.RED.darker()

        drawing { tessellator, vb ->
            GlStateManager.glLineWidth(2f)
            GlStateManager.color(c.red/255f, c.green/255f, c.blue/255f, c.alpha/255f)
            vb.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION)
            AxisAlignedBB(vec(-radius, -radius, -radius), vec(radius, radius, radius)).edges.forEach {
                vb.pos(it.first).endVertex()
                vb.pos(it.second).endVertex()
            }
            tessellator.draw()
        }

        node.node.renderer?.render()
    }
}