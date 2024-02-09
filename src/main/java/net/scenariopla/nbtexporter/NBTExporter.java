package net.scenariopla.nbtexporter;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.regex.Pattern;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.loader.api.FabricLoader;

import net.scenariopla.nbtexporter.command.ExportItemStackNBTCommand;
import net.scenariopla.nbtexporter.init.DirectoryInit;

public class NBTExporter implements ClientModInitializer {
    public class Reference {
        public static final String MODID = "nbtexporter";
        public static final String NAME = "NBT Exporter";
        public static final String VERSION = "1.2";
        
        public static final String MODDIR = MODID;
        
        public static final FabricLoader MINECRAFT = FabricLoader.getInstance();
        public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss");
    
        public static final String FILE_NAME_CHAR_REPLACE = "<>:\"/\\|?*\r\n\t\b\f";
        public static final Pattern FILE_NAME_CHAR_REPLACE_PATTERN = Pattern.compile("[" + FILE_NAME_CHAR_REPLACE + "]");
    }
    
    public static File[] modFiles = null;

    @Override
    public void onInitializeClient() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            ExportItemStackNBTCommand.register(dispatcher);
        });

        DirectoryInit.modDir = new File(Reference.MINECRAFT.getGameDir().toFile(), Reference.MODDIR);
        modFiles = DirectoryInit.initModDirectory();
    }
}
