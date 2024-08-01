package cn.yapeteam.yolbi.module.impl.combat;

import cn.yapeteam.yolbi.event.impl.network.EventPacketReceive;
import cn.yapeteam.yolbi.event.impl.network.EventPacketSend;
import cn.yapeteam.yolbi.event.impl.player.EventMotion;
import cn.yapeteam.yolbi.module.Module;
import cn.yapeteam.yolbi.module.ModuleCategory;
import cn.yapeteam.yolbi.module.values.impl.ModeValue;
import cn.yapeteam.yolbi.utils.misc.TimerUtil;
import cn.yapeteam.yolbi.utils.network.PacketUtil;

import io.netty.buffer.Unpooled;
import net.minecraft.block.Block;
import net.minecraft.item.ItemAppleGold;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.*;
import net.minecraft.network.play.server.S2FPacketSetSlot;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;

import net.minecraft.item.ItemSword;



public class NoSlow extends Module {
    boolean slow;
    private int stackSize;
    boolean canCanCelC08;
    TimerUtil timer = new TimerUtil();
    long delay = 100L;
    boolean fasterDelay;
    private boolean sent = false;
    private boolean canEat = false;
    private boolean a = false;
    private int s = 0;
    private boolean smartSpeed = false;
    private final ModeValue<String> mode = new ModeValue<>("Mode", "Packet", "Packet", "GrimAc", "HuaYuTing");

    public NoSlow() {
        super("NoSlow", ModuleCategory.COMBAT);
        addValues(mode);
    }

    @Override
    public void onEnable() {
        this.sent = false;
    }

    public void onPacketReceive(EventPacketReceive event) {
        S2FPacketSetSlot s2f;
        Packet<?> packet = event.getPacket();
        ItemStack itemStack = NoSlow.mc.player.getHeldItem(EnumHand.MAIN_HAND);


        if (this.mode.is("GrimAc") && packet instanceof S2FPacketSetSlot && itemStack.getItem() instanceof ItemAppleGold && !itemStack.getItem().hasEffect(itemStack) && (s2f = (S2FPacketSetSlot) packet).func_149175_c() == 0 && s2f.func_149174_e().getItem() == NoSlow.mc.player.getHeldItem(EnumHand.MAIN_HAND).getItem()) {
           // NoSlow.mc.player.inventory.getCurrentItem().stackSize = s2f.func_149174_e().stackSize; idk
            this.slow = false;
        }

    }


    public void onPacketSend(EventPacketSend event) {
        Packet packet = event.getPacket();
        if (this.mode.is("GrimAc")) {
            if (NoSlow.mc.player == null || NoSlow.mc.world == null || !NoSlow.mc.world.isRemote || NoSlow.mc.player.getHeldItem(EnumHand.MAIN_HAND) == null) {
                return;
            }
            ItemStack itemStack = NoSlow.mc.player.getHeldItem(EnumHand.MAIN_HAND);

                if (packet instanceof C08PacketPlayerBlockPlacement && ((C08PacketPlayerBlockPlacement) packet).getPosition().getY() == -1 && !this.slow) {
                    PacketUtil.sendPacket(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.DROP_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN));
                    this.slow = true;
                }
                if (packet instanceof C07PacketPlayerDigging && ((C07PacketPlayerDigging) packet).getStatus() == C07PacketPlayerDigging.Action.RELEASE_USE_ITEM) {

                    this.slow = true;
                }

        }
    }
    public void onUpdate(EventMotion e) {
        String curMode = mode.getValue();
        switch (curMode) {
            case "GrimAc": {
                {
                    //跑剑

                    PacketUtil.sendPacket(new C09PacketHeldItemChange(mc.player.inventory.currentItem + 1));

                    PacketUtil.sendPacket(new C17PacketCustomPayload("L", new PacketBuffer(Unpooled.buffer())));
                    PacketUtil.sendPacket(new C09PacketHeldItemChange((mc.player.inventory.currentItem)));


                    PacketUtil.sendPacket(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN));


                    PacketUtil.sendPacket(new C0FPacketConfirmTransaction());
                    PacketUtil.sendPacket(new C09PacketHeldItemChange());


                    // mc.getNetHandler().addToSendQueue(new C17PacketCustomPayload("FuckYou", new PacketBuffer(Unpooled.buffer())));
                    //mc.getNetHandler().addToSendQueue(new C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem));
                }
                if (mc.player.getHeldItem(EnumHand.MAIN_HAND).getItem() instanceof ItemSword) {
                    PacketUtil.sendPacket(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN));
                    PacketUtil.sendPacket(new C0EPacketClickWindow(0, 36, 0, 2, new ItemStack(Block.getBlockById(166)), (short) 0));
                }
            }

            PacketUtil.sendPacket(new C08PacketPlayerBlockPlacement(NoSlow.mc.player.inventory.getCurrentItem()));

            break;
        }
        }
    }






