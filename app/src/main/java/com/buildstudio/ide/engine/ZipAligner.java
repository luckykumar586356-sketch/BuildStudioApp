package com.buildstudio.ide.engine;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * High-performance 4-byte ZipAlign implementation in pure Java.
 * Ensures all STORED entries (such as classes.dex and resources.arsc)
 * are strictly aligned on 4-byte boundaries so modern Android Package Manager
 * installs the APK without rejection.
 */
public class ZipAligner {
    private static final int DEFAULT_ALIGNMENT = 4;

    private static class EntryInfo {
        ZipEntry entry;
        byte[] data;
        long localHeaderOffset;
    }

    public static void align(File inZip, File outZip) throws IOException {
        align(inZip, outZip, DEFAULT_ALIGNMENT);
    }

    public static void align(File inZip, File outZip, int alignment) throws IOException {
        try (ZipFile zipFile = new ZipFile(inZip);
             FileOutputStream fos = new FileOutputStream(outZip);
             CountingOutputStream cos = new CountingOutputStream(fos)) {

            List<EntryInfo> entries = new ArrayList<>();
            Enumeration<? extends ZipEntry> e = zipFile.entries();

            while (e.hasMoreElements()) {
                ZipEntry entry = e.nextElement();
                EntryInfo info = new EntryInfo();
                info.entry = entry;

                try (InputStream is = zipFile.getInputStream(entry)) {
                    info.data = readAllBytes(is, (int) entry.getSize());
                }
                entries.add(info);
            }

            for (EntryInfo info : entries) {
                ZipEntry entry = info.entry;
                byte[] nameBytes = entry.getName().getBytes("UTF-8");
                byte[] extra = entry.getExtra();
                if (extra == null) extra = new byte[0];

                boolean isStored = entry.getMethod() == ZipEntry.STORED;
                int padding = 0;

                if (isStored) {
                    long dataOffset = cos.getCount() + 30 + nameBytes.length + extra.length;
                    int remainder = (int) (dataOffset % alignment);
                    if (remainder != 0) {
                        padding = alignment - remainder;
                    }
                }

                byte[] newExtra = new byte[extra.length + padding];
                System.arraycopy(extra, 0, newExtra, 0, extra.length);
                entry.setExtra(newExtra);

                info.localHeaderOffset = cos.getCount();

                writeLocalHeader(cos, entry, nameBytes, newExtra);
                cos.write(info.data);
            }

            long cdOffset = cos.getCount();
            for (EntryInfo info : entries) {
                writeCentralDirectoryEntry(cos, info.entry, info.localHeaderOffset);
            }
            long cdSize = cos.getCount() - cdOffset;

            writeEOCD(cos, entries.size(), cdSize, cdOffset);
        }
    }

    private static void writeLocalHeader(OutputStream os, ZipEntry entry, byte[] nameBytes, byte[] extra) throws IOException {
        write32(os, 0x04034b50L);
        write16(os, entry.getMethod() == ZipEntry.STORED ? 10 : 20);
        write16(os, 0);
        write16(os, entry.getMethod());
        write16(os, 0);
        write16(os, 0);
        write32(os, entry.getCrc());
        write32(os, entry.getCompressedSize() == -1 ? entry.getSize() : entry.getCompressedSize());
        write32(os, entry.getSize());
        write16(os, nameBytes.length);
        write16(os, extra.length);
        os.write(nameBytes);
        os.write(extra);
    }

    private static void writeCentralDirectoryEntry(OutputStream os, ZipEntry entry, long localHeaderOffset) throws IOException {
        byte[] nameBytes = entry.getName().getBytes("UTF-8");
        byte[] extra = entry.getExtra();
        if (extra == null) extra = new byte[0];

        write32(os, 0x02014b50L);
        write16(os, 20);
        write16(os, entry.getMethod() == ZipEntry.STORED ? 10 : 20);
        write16(os, 0);
        write16(os, entry.getMethod());
        write16(os, 0);
        write16(os, 0);
        write32(os, entry.getCrc());
        write32(os, entry.getCompressedSize() == -1 ? entry.getSize() : entry.getCompressedSize());
        write32(os, entry.getSize());
        write16(os, nameBytes.length);
        write16(os, extra.length);
        write16(os, 0);
        write16(os, 0);
        write16(os, 0);
        write32(os, 0L);
        write32(os, localHeaderOffset);
        os.write(nameBytes);
        os.write(extra);
    }

    private static void writeEOCD(OutputStream os, int entryCount, long cdSize, long cdOffset) throws IOException {
        write32(os, 0x06054b50L);
        write16(os, 0);
        write16(os, 0);
        write16(os, entryCount);
        write16(os, entryCount);
        write32(os, cdSize);
        write32(os, cdOffset);
        write16(os, 0);
    }

    private static void write16(OutputStream os, int value) throws IOException {
        os.write(value & 0xFF);
        os.write((value >> 8) & 0xFF);
    }

    private static void write32(OutputStream os, long value) throws IOException {
        os.write((int) (value & 0xFF));
        os.write((int) ((value >> 8) & 0xFF));
        os.write((int) ((value >> 16) & 0xFF));
        os.write((int) ((value >> 24) & 0xFF));
    }

    private static byte[] readAllBytes(InputStream is, int expectedSize) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(expectedSize > 0 ? expectedSize : 4096);
        byte[] buffer = new byte[16 * 1024];
        int read;
        while ((read = is.read(buffer)) != -1) baos.write(buffer, 0, read);
        return baos.toByteArray();
    }

    private static class CountingOutputStream extends OutputStream {
        private final OutputStream out;
        private long count = 0;

        CountingOutputStream(OutputStream out) {
            this.out = out;
        }

        @Override
        public void write(int b) throws IOException {
            out.write(b);
            count++;
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            out.write(b, off, len);
            count += len;
        }

        public long getCount() {
            return count;
        }

        @Override
        public void close() throws IOException {
            out.close();
        }
    }
}
