package net.scenariopla.nbtexporter.command;

import org.apache.commons.lang3.StringUtils;

import net.minecraft.command.CommandBase;

import net.scenariopla.nbtexporter.NBTExporterReference;

public abstract class CommandNBTExporter extends CommandBase {
    public static boolean hasExtension(final String originalString,
                                       final String extension) {
        if (StringUtils.equalsIgnoreCase(StringUtils.right(originalString,
                                                           extension.length()),
                                         extension)) {
            return true;
        }
        return false;
    }
    
    public static String trimExtension(final String originalString,
                                       final String extension) {
        if (StringUtils.equalsIgnoreCase(StringUtils.right(originalString,
                                                           extension.length()),
                                         extension)) {
            return originalString.substring(0, originalString.length() - extension.length());
        }
        return originalString;
    }
    
    public static String replaceIllegalCharacters(final String nameWithExtension,
                                                  final char replacedChar) {
        return NBTExporterReference.FILE_NAME_CHAR_REPLACE_PATTERN.matcher(nameWithExtension)
                                                                  .replaceAll(String.valueOf(replacedChar));
    }
}
