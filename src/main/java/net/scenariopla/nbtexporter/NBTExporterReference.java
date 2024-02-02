package net.scenariopla.nbtexporter;

import java.text.SimpleDateFormat;
import java.util.regex.Pattern;

import net.minecraft.client.Minecraft;

public class NBTExporterReference {
    public static final String MODID = "nbtexporter";
    public static final String NAME = "NBT Exporter";
    public static final String VERSION = "1.0";
    
    public static final String MODDIR = MODID;
    
    public static final Minecraft MINECRAFT = Minecraft.getMinecraft();
    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss");
    
    public static final String FILE_NAME_CHAR_REPLACE = "<>:\"/\\|?*\r\n\t\b\f";
    public static final Pattern FILE_NAME_CHAR_REPLACE_PATTERN = Pattern.compile("[" + FILE_NAME_CHAR_REPLACE + "]");
}
