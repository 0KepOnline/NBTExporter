package net.scenariopla.nbtexporter.command;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.List;

import javax.annotation.Nullable;

import org.apache.commons.io.IOUtils;

import com.google.common.collect.Lists;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.SyntaxErrorException;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.GameRules;

import net.scenariopla.nbtexporter.NBTExporter;
import net.scenariopla.nbtexporter.init.DirectoryInit;

public class CommandExportItemStackNBT extends CommandNBTExporter {
    private static final String FILE_EXTENSION = ".nbt";
    
    @Override
    public String getName() {
        return "exportnbt";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "commands.nbtexporter.exportnbt.usage";
    }
    
    @Override
    public boolean checkPermission(MinecraftServer server,
                                   ICommandSender sender) {
        return true;
    }
    
    private enum ExportItemStackNBTExceptions {
        SYNTAX_ERROR_USAGE,
        SYNTAX_ERROR_STRING_TERMINATION,
        
        HELD_ITEM_ERROR_EMPTY,
        NBT_ERROR_EMPTY,
        IO_ERROR_WRITE,
        IO_ERROR_WRITE_ACCESS
    }
    
    private static CommandException exceptions(ExportItemStackNBTExceptions exception) {
        return exceptions(exception, new Object[0]);
    }
    
    private static CommandException exceptions(ExportItemStackNBTExceptions exception,
                                               Object[] args) {
        switch (exception) {
        case SYNTAX_ERROR_USAGE:
            return new WrongUsageException("commands.nbtexporter.global.usage", args);
        case SYNTAX_ERROR_STRING_TERMINATION:
            return new SyntaxErrorException("commands.nbtexporter.global.syntaxError.stringTermination", args);
        
        case HELD_ITEM_ERROR_EMPTY:
            return new CommandException("commands.nbtexporter.global.heldItemError.empty", args);
        case NBT_ERROR_EMPTY:
            return new CommandException("commands.nbtexporter.exportnbt.nbtError.empty", args);
        case IO_ERROR_WRITE:
            return new CommandException("commands.nbtexporter.exportnbt.ioError.write", args);
        case IO_ERROR_WRITE_ACCESS:
            return new CommandException("commands.nbtexporter.exportnbt.ioError.writeAccess", args);
        }
        return null;
    }
    
    @Override
    public List<String> getTabCompletions(MinecraftServer server,
                                          ICommandSender sender,
                                          String[] args,
                                          @Nullable BlockPos targetPos) {
        if (sender instanceof EntityPlayer) {
            final ItemStack heldItem = ((EntityPlayer) sender).getHeldItemMainhand();
            if (!heldItem.isEmpty()) {
                final String heldItemName = heldItem.getDisplayName();
                final boolean heldItemNameHasSpaces = heldItemName.contains(" ");
                if (args.length == 1) {
                    if (heldItemNameHasSpaces) {
                        return Lists.newArrayList('"' + heldItemName + '"');
                    }
                    return Lists.newArrayList(heldItemName);
                } else if (args.length > 1 && heldItemNameHasSpaces) {
                    String[] argsExpected = ('"' + heldItemName + '"').split(" ");
                    if (args.length <= argsExpected.length) {
                        for (int argNum = 0; argNum < args.length - 1; argNum++) {
                            if (!args[argNum].equals(argsExpected[argNum])) {
                                return Lists.newArrayList();
                            }
                        }
                        final StringBuilder stringBuilder = new StringBuilder();
                        for (int argNum = args.length - 1; argNum < argsExpected.length; argNum++) {
                            stringBuilder.append(argsExpected[argNum]).append(" ");
                        }
                        return Lists.newArrayList(stringBuilder.toString().trim());
                    }
                }
            }
        }
        return Lists.newArrayList();
    }
    
    @Override
    public void execute(MinecraftServer server,
                        ICommandSender sender,
                        String[] args) throws CommandException {
        if (sender instanceof EntityPlayer) {
            final File[] modOutputFiles = DirectoryInit.initModDirectory();
            String filename = null;
            if (args.length >= 1) {
                final String argsString = buildString(args, 0);
                if (argsString.startsWith("\"")) {
                    if (argsString.substring(1).contains("\"")) {
                        filename = argsString.substring(1, argsString.substring(1).indexOf('\"') + 1);
                    } else {
                        throw exceptions(ExportItemStackNBTExceptions.SYNTAX_ERROR_STRING_TERMINATION,
                                         new Object[] {argsString});
                    }
                } else if (args.length == 1) {
                    filename = argsString;
                } else {
                    throw exceptions(ExportItemStackNBTExceptions.SYNTAX_ERROR_USAGE);
                }
            }
            
            if (filename == null) {
                filename = NBTExporter.Reference.DATE_FORMAT.format(new Date());
            } else {
                filename = replaceIllegalCharacters(trimExtension(filename, FILE_EXTENSION), '_');
            }
            
            final EntityPlayer player = (EntityPlayer) sender;
            final ItemStack heldItem = player.getHeldItemMainhand();
            if (heldItem.isEmpty()) {
                throw exceptions(ExportItemStackNBTExceptions.HELD_ITEM_ERROR_EMPTY);
            }
            
            final NBTTagCompound nbtTagCompound = heldItem.getTagCompound();
            if (nbtTagCompound == null) {
                throw exceptions(ExportItemStackNBTExceptions.NBT_ERROR_EMPTY);
            }
            final GameRules gameRules = NBTExporter.Reference.MINECRAFT.world.getGameRules();
            
            for (File existingFile : modOutputFiles) {
                if (existingFile.getName().equals(filename + FILE_EXTENSION)) {
                    int namesakeFilesCount = 1;
                    for (File existingFileNamesake : modOutputFiles) {
                        try {
                            if (existingFileNamesake.getName().startsWith(filename + "_")) {
                                int namesakeFileCountSuffix = Integer.parseInt(existingFileNamesake.getName()
                                                                               .replace(filename + "_", "")
                                                                               .replace(FILE_EXTENSION, ""));
                                if (namesakeFileCountSuffix >= namesakeFilesCount) {
                                    namesakeFilesCount = namesakeFileCountSuffix + 1;
                                }
                            } else {
                                continue;
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            continue;
                        }
                    }
                    filename += String.format("_%d", namesakeFilesCount);
                    break;
                }
            }
            final String filenameWithExtension = filename + FILE_EXTENSION;
            
            final File outputFile = new File(DirectoryInit.modDir, filenameWithExtension);
            try {
                if (outputFile.exists() && !outputFile.canWrite()) {
                    throw exceptions(ExportItemStackNBTExceptions.IO_ERROR_WRITE_ACCESS,
                                     new Object[] {filenameWithExtension});
                }
                outputFile.createNewFile();
                OutputStream outputStream = null;
                try {
                    outputStream = new FileOutputStream(outputFile);
                    CompressedStreamTools.writeCompressed(nbtTagCompound, outputStream);
                    if (gameRules.getBoolean("sendCommandFeedback")) {
                        player.sendMessage(new TextComponentTranslation("commands.nbtexporter.exportnbt.success",
                                                                        heldItem.getTextComponent(),
                                                                        filenameWithExtension));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    IOUtils.closeQuietly(outputStream);
                }
            } catch (Exception e) {
                e.printStackTrace();
                throw exceptions(ExportItemStackNBTExceptions.IO_ERROR_WRITE,
                                 new Object[] {filenameWithExtension});
            }
        }
    }
}
