package net.scenariopla.nbtexporter;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.regex.Pattern;

import net.minecraft.client.Minecraft;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import net.scenariopla.nbtexporter.command.CommandExportItemStackNBT;
import net.scenariopla.nbtexporter.init.DirectoryInit;

@Mod(modid   = NBTExporter.Reference.MODID,
     name    = NBTExporter.Reference.NAME,
     version = NBTExporter.Reference.VERSION)
public class NBTExporter {
    public static class Reference {
        public static final String MODID = "nbtexporter";
        public static final String NAME = "NBT Exporter";
        public static final String VERSION = "1.2";
        
        public static final String MODDIR = MODID;
        
        public static final Minecraft MINECRAFT = Minecraft.getMinecraft();
        public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss");
        
        public static final String FILE_NAME_CHAR_REPLACE = "<>:\"/\\|?*\r\n\t\b\f";
        public static final Pattern FILE_NAME_CHAR_REPLACE_PATTERN = Pattern.compile("[" + FILE_NAME_CHAR_REPLACE + "]");
    }
    
    public static File[] modFiles = null;

    @EventHandler
    @SideOnly(Side.CLIENT)
    public void init(FMLInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(this);
        ClientCommandHandler.instance.registerCommand(new CommandExportItemStackNBT());
        
        DirectoryInit.modDir = new File(Reference.MINECRAFT.mcDataDir,
                                        Reference.MODDIR);
        modFiles = DirectoryInit.initModDirectory();
    }
}
