package org.telegram.bot.cbox;

import java.util.Objects;

/**
 * @author ahmad
 */
public final class FileVersion {

    private final String fileName;
    private final int version;

    public FileVersion(String fileName, int version) {
        this.fileName = fileName;
        this.version = version;
    }

    public String getFileName() {
        return fileName;
    }

    public int getVersion() {
        return version;
    }

    @Override
    public int hashCode() {
        return Objects.hash(fileName, version);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || !(obj instanceof FileVersion)) {
            return false;
        }
        FileVersion other = (FileVersion) obj;
        return fileName.equals(other.fileName) && version == other.version;
    }

}
