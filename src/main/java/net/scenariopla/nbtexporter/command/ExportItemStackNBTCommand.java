package net.scenariopla.nbtexporter.command;

import java.io.File;
import java.util.Date;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import net.scenariopla.nbtexporter.NBTExporter;
import net.scenariopla.nbtexporter.init.DirectoryInit;
import net.scenariopla.nbtexporter.util.NBTFileWriter;

public class ExportItemStackNBTCommand extends NBTExporterCommand {
    private static final String FILE_EXTENSION = ".nbt";
    
    private enum ExportItemStackNBTExceptions {
        SYNTAX_ERROR_USAGE,
        SYNTAX_ERROR_STRING_TERMINATION,
        
        HELD_ITEM_ERROR_EMPTY,
        NBT_ERROR_EMPTY,
        IO_ERROR_WRITE,
        IO_ERROR_WRITE_ACCESS
    }
    
    private static SimpleCommandExceptionType exceptions(ExportItemStackNBTExceptions exception) {
        return exceptions(exception, new Object[0]);
    }
    
    private static SimpleCommandExceptionType exceptions(ExportItemStackNBTExceptions exception, Object[] args) {
        switch (exception) {
        case SYNTAX_ERROR_USAGE:
            return new SimpleCommandExceptionType(
                       Component.translatable("commands.nbtexporter.global.usage",
                                new Object[] {Component.translatable("commands.nbtexporter.exportnbt.usage", args)})
                   );
        case SYNTAX_ERROR_STRING_TERMINATION:
            return new SimpleCommandExceptionType(
                       Component.translatable("commands.nbtexporter.exportnbt.syntaxError.stringTermination", args)
                   );
        case HELD_ITEM_ERROR_EMPTY:
            return new SimpleCommandExceptionType(
                       Component.translatable("commands.nbtexporter.global.heldItemError.empty", args)
                   );
        case NBT_ERROR_EMPTY:
            return new SimpleCommandExceptionType(
                       Component.translatable("commands.nbtexporter.exportnbt.nbtError.empty", args)
                   );
        case IO_ERROR_WRITE:
            return new SimpleCommandExceptionType(
                       Component.translatable("commands.nbtexporter.exportnbt.ioError.write", args)
                   );
        case IO_ERROR_WRITE_ACCESS:
            return new SimpleCommandExceptionType(
                       Component.translatable("commands.nbtexporter.exportnbt.ioError.writeAccess", args)
                   );
        }
        return null;
    }
    
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("exportnbt")
                            .executes(context -> {
                                return execute(context, getFilename());
                            })
                            .then(Commands.argument("filename", StringArgumentType.greedyString())
                                  .suggests((context, builder) -> {
                                      String suggestion = "";
                                      if (context.getSource().getEntity() instanceof Player) {
                                          final Player player = (Player) context.getSource().getEntity();
                                          final ItemStack heldItem = player.getMainHandItem();
                                          if (!heldItem.isEmpty()) {
                                              String heldItemName = heldItem.getDisplayName()
                                                                            .getString();
                                              heldItemName = heldItemName.substring(1, heldItemName.length() - 1);
                                              if (heldItemName.contains(" ")) {
                                                  suggestion = '"' + heldItemName + '"';
                                              } else {
                                                  suggestion = heldItemName;
                                              }
                                          }
                                      }
                                      return builder.suggest(suggestion).buildFuture();
                                  })
                                  .requires(source -> source.getEntity() instanceof Player)
                                  .executes(context -> {
                                      return execute(context, getFilename(context));
                                  })));
    }
    
    private static String getFilename(CommandContext<CommandSourceStack> context)
    throws CommandSyntaxException {
        final String argsString = StringArgumentType.getString(context, "filename");
        String filename = null;
        if (argsString.length() >= 1) {
            if (argsString.startsWith("\"")) {
                if (argsString.substring(1).contains("\"")) {
                    filename = argsString.substring(1, argsString.substring(1).indexOf('\"') + 1);
                } else {
                    throw exceptions(ExportItemStackNBTExceptions.SYNTAX_ERROR_STRING_TERMINATION,
                                     new Object[] {argsString}).create();
                }
            } else {
                if (!argsString.contains(" ")) {
                    filename = argsString;
                } else {
                    throw exceptions(ExportItemStackNBTExceptions.SYNTAX_ERROR_USAGE).create();
                }
            }
        }
        
        if (filename == null) {
            return getFilename();
        } else {
            return replaceIllegalCharacters(trimExtension(filename, FILE_EXTENSION), '_');
        }
    }
    
    private static String getFilename() {
        return NBTExporter.Reference.DATE_FORMAT.format(new Date());
    }
    
    private static int execute(CommandContext<CommandSourceStack> context, String filename)
    throws CommandSyntaxException {
        final CommandSourceStack sourceStack = context.getSource();
        final File[] modOutputFiles = DirectoryInit.initModDirectory();
        
        final Player player = (Player) sourceStack.getEntity();
        final ItemStack heldItem = player.getMainHandItem();
        if (heldItem.isEmpty()) {
            throw exceptions(ExportItemStackNBTExceptions.HELD_ITEM_ERROR_EMPTY).create();
        }
        
        final CompoundTag nbtTagCompound = heldItem.getTag();
        if (nbtTagCompound == null) {
            throw exceptions(ExportItemStackNBTExceptions.NBT_ERROR_EMPTY).create();
        }
        
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
                                 new Object[] {filenameWithExtension}).create();
            }
            outputFile.createNewFile();
            NBTFileWriter.writeCompoundGZIP(nbtTagCompound, outputFile);
            final Component successMessage = Component.translatable("commands.nbtexporter.exportnbt.success",
                                                                    heldItem.getDisplayName(),
                                                                    filenameWithExtension);
            sourceStack.sendSuccess(successMessage, false);
            return 1;
        } catch (Exception e) {
            e.printStackTrace();
            throw exceptions(ExportItemStackNBTExceptions.IO_ERROR_WRITE,
                             new Object[] {filenameWithExtension}).create();
        }
    }
}
