package net.scenariopla.nbtexporter;

import java.io.File;

import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import net.scenariopla.nbtexporter.command.CommandExportItemStackNBT;
import net.scenariopla.nbtexporter.init.DirectoryInit;

@Mod(modid   = NBTExporterReference.MODID,
     name    = NBTExporterReference.NAME,
     version = NBTExporterReference.VERSION)
public class NBTExporterMain {
    public static File[] modFiles = null;
    
    @EventHandler
    @SideOnly(Side.CLIENT)
    public void init(FMLInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(this);
        ClientCommandHandler.instance.registerCommand(new CommandExportItemStackNBT());
    }

    @EventHandler
    @SideOnly(Side.CLIENT)
    public void onInit(FMLInitializationEvent event) {
        DirectoryInit.modDir = new File(NBTExporterReference.MINECRAFT.mcDataDir,
                                        NBTExporterReference.MODDIR);
        modFiles = DirectoryInit.initModDirectory();
    }
}
