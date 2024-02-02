package net.scenariopla.nbtexporter.command;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Date;

import org.apache.commons.io.IOUtils;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.SyntaxErrorException;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.GameRules;

import net.scenariopla.nbtexporter.NBTExporterReference;
import net.scenariopla.nbtexporter.init.DirectoryInit;

public class CommandExportItemStackNBT extends CommandNBTExporter {
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
                        throw new SyntaxErrorException(
                                      "commands.nbtexporter.exportnbt.syntaxError.stringTermination",
                                      new Object[] {argsString}
                                  );
                    }
                } else {
                    if (args.length == 1) {
                        filename = argsString;
                    } else {
                        throw new WrongUsageException("commands.nbtexporter.exportnbt.usage",
                                                      new Object[0]);
                    }
                }
            }
            
            if (filename == null) {
                filename = NBTExporterReference.DATE_FORMAT.format(new Date());
            } else {
                filename = replaceIllegalCharacters(trimExtension(filename, ".nbt"), '_');
            }
            
            final EntityPlayer player = (EntityPlayer) sender;
            final ItemStack heldItem = player.getHeldItemMainhand();
            if (heldItem.isEmpty()) {
                throw new CommandException("commands.nbtexporter.global.heldItemError.empty",
                                           new Object[0]);
            }
            
            final NBTTagCompound nbtTagCompound = heldItem.getTagCompound();
            if (nbtTagCompound == null) {
                throw new CommandException("commands.nbtexporter.exportnbt.nbtError.empty",
                                           new Object[0]);
            }
            final GameRules gameRules = NBTExporterReference.MINECRAFT.world.getGameRules();
            
            for (File existingFile : modOutputFiles) {
                if (existingFile.getName().equals(filename + ".nbt")) {
                    int namesakeFilesCount = 1;
                    for (File existingFileNamesake : modOutputFiles) {
                        try {
                            if (existingFileNamesake.getName().startsWith(filename + "_")) {
                                int namesakeFileCountSuffix = Integer.parseInt(existingFileNamesake.getName()
                                                                               .replace(filename + "_", "")
                                                                               .replace(".nbt", ""));
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
            final String filenameWithExtension = filename + ".nbt";
            
            final File outputFile = new File(DirectoryInit.modDir, filenameWithExtension);
            try {
                if (outputFile.exists() && !outputFile.canWrite()) {
                    throw new CommandException("commands.nbtexporter.exportnbt.ioError.writeAccess",
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
                throw new CommandException("commands.nbtexporter.exportnbt.ioError.write",
                                           new Object[] {filenameWithExtension});
            }
        }
    }
}
