package net.scenariopla.nbtexporter.init;

import java.io.File;

public class DirectoryInit {
    public static File modDir;

    private static File[] listFiles(final File dir, boolean isOptional) {
        try {
            return dir.listFiles();
        } catch (SecurityException e) {
            if (!isOptional) {
                e.printStackTrace();
                throw new RuntimeException("Unable to reach the \"" + dir.getName() + "\" directory.");
            }
            return null;
        }
    }
    
    private static void initDirectory(final File dir) {
        try {
            if (!dir.exists()) {
                try {
                    dir.mkdir();
                }
                catch (Exception e) {
                    e.printStackTrace();
                    throw new RuntimeException("Failed to create the \"" + dir.getName() + "\" directory.");
                }
            }
        } catch (SecurityException e) {
            e.printStackTrace();
            throw new RuntimeException("Unable to reach the \"" + dir.getName() + "\" directory.");
        }
    }
    
    public static File[] initModDirectory() {
        initDirectory(modDir);
        return listFiles(modDir, false);
    }
    
    public static File[] initModChildDirectory(final String childDirName) {
        final File modChildDir = new File(modDir, childDirName);
        initDirectory(modChildDir);
        return listFiles(modChildDir, true);
    }
}
