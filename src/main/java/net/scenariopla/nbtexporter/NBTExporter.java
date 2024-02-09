package net.scenariopla.nbtexporter;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.regex.Pattern;

import net.minecraft.client.Minecraft;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import net.scenariopla.nbtexporter.command.ExportItemStackNBTCommand;
import net.scenariopla.nbtexporter.init.DirectoryInit;

@Mod(NBTExporter.Reference.MODID)
public class NBTExporter {
    public class Reference {
        public static final String MODID = "nbtexporter";
        public static final String NAME = "NBT Exporter";
        public static final String VERSION = "1.2";
        
        public static final String MODDIR = MODID;
        
        public static final Minecraft MINECRAFT = Minecraft.getInstance();
        public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss");
    
        public static final String FILE_NAME_CHAR_REPLACE = "<>:\"/\\|?*\r\n\t\b\f";
        public static final Pattern FILE_NAME_CHAR_REPLACE_PATTERN = Pattern.compile("[" + FILE_NAME_CHAR_REPLACE + "]");
    }
    
    public static File[] modFiles = null;

    public NBTExporter() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::init);
        MinecraftForge.EVENT_BUS.register(this);
    }
    
    public void init(FMLClientSetupEvent event) {
        DirectoryInit.modDir = new File(Reference.MINECRAFT.gameDirectory, Reference.MODDIR);
        modFiles = DirectoryInit.initModDirectory();
    }
    
    @Mod.EventBusSubscriber
    public class EventHandler {
        @SubscribeEvent
        @OnlyIn(Dist.CLIENT)
        public static void onRegisterCommands(RegisterClientCommandsEvent event) {
            ExportItemStackNBTCommand.register(event.getDispatcher());
        }
    }
}
