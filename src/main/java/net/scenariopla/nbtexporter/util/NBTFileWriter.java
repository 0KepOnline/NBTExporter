package net.scenariopla.nbtexporter.util;

import java.io.BufferedOutputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.GZIPOutputStream;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

public class NBTFileWriter {
    private static void writeSingle(final Tag tag,
                                    final DataOutput output)
    throws IOException {
        final byte id = tag.getId();
        output.writeByte(id);
        if (id != 0) {
            output.writeUTF("");
            tag.write(output);
        }
    }
    
    public static void writeCompound(final CompoundTag compound,
                                     final DataOutput output)
    throws IOException {
        writeSingle(compound, output);
    }

    public static void writeCompound(final CompoundTag compound,
                                     final File file)
    throws IOException {
        DataOutputStream dataOutputStream = new DataOutputStream(
                                                new FileOutputStream(file)
                                            );
        try {
            writeCompound(compound, dataOutputStream);
        } finally {
            dataOutputStream.close();
        }
    }

    public static void writeCompoundGZIP(final CompoundTag compound,
                                         final OutputStream outputStream)
    throws IOException {
        DataOutputStream dataOutputStream = new DataOutputStream(
                                                new BufferedOutputStream(
                                                    new GZIPOutputStream(outputStream)
                                                )
                                            );
        try {
            writeCompound(compound, dataOutputStream);
        } finally {
            dataOutputStream.close();
        }
    }

    public static void writeCompoundGZIP(final CompoundTag compound,
                                         final File file)
    throws IOException {
        DataOutputStream dataOutputStream = new DataOutputStream(
                                                new BufferedOutputStream(
                                                    new GZIPOutputStream(
                                                        new FileOutputStream(file)
                                                    )
                                                )
                                            );
        try {
            writeCompound(compound, dataOutputStream);
        } finally {
            dataOutputStream.close();
        }
    }
}
