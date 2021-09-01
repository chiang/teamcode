package io.teamcode.util;

import org.apache.commons.io.IOUtils;
import org.apache.tika.Tika;
import org.apache.tika.mime.MimeTypes;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Created by chiang on 2017. 3. 17..
 */
public abstract class FileSystemUtils {

    private static final int BUFFER_SIZE = 4096;

    public static final File getUserHomeDir() {
        String userHomePath = System.getProperty("user.home");

        return new File(userHomePath);
    }

    public static final File getDefaultHomeDir() {

        return new File(getUserHomeDir(), "teamcode-home");
    }

    public static final String detectMimeType(final String fileName) {
        // Try to detect based on the file name only for efficiency
        Tika tika = new Tika();
        String fileNameDetect = tika.detect(fileName);

        return fileNameDetect;
    }

    //FIXME test performance?
    public static final String detectMimeType(File file) throws IOException {
        // Try to detect based on the file name only for efficiency
        Tika tika = new Tika();
        //String fileNameDetect = tika.detect(file.getName());
        //if(!fileNameDetect.equals(MimeTypes.OCTET_STREAM)) {
//            return fileNameDetect;
  //      }

        // Then check the file content if necessary
        String fileContentDetect = tika.detect(file);
        if(!fileContentDetect.equals(MimeTypes.OCTET_STREAM)) {
            return fileContentDetect;
        }

        // Specification says to return null if we could not
        // conclusively determine the file type
        return null;
    }

    public static final void unzip(final File zipFile, final File destinationDir) throws IOException {
        unzip(new FileInputStream(zipFile), destinationDir);
    }

    public static final void unzip(final InputStream inputStream, final File destinationDir) throws IOException {
        if (!destinationDir.exists()) {
            if (!destinationDir.mkdirs())
                throw new IOException(String.format("'%s' 디렉터리를 만들 수 없습니다.", destinationDir.getAbsolutePath()));
        }
        ZipInputStream zipIn = new ZipInputStream(inputStream);
        ZipEntry entry = zipIn.getNextEntry();
        // iterates over entries in the zip file
        File file;
        while (entry != null) {
            file = new File(destinationDir, entry.getName());
            if (!entry.isDirectory()) {
                // if the entry is a file, extracts it
                extractFile(zipIn, file);
            } else {
                // if the entry is a directory, make the directory
                file.mkdir();
            }
            zipIn.closeEntry();
            entry = zipIn.getNextEntry();
        }
        zipIn.close();
    }

    private static final void extractFile(ZipInputStream zipIn, File file) throws IOException {
        BufferedOutputStream bos = null;
        try {
            bos = new BufferedOutputStream(new FileOutputStream(file));
            byte[] bytesIn = new byte[BUFFER_SIZE];
            int read = 0;
            while ((read = zipIn.read(bytesIn)) != -1) {
                bos.write(bytesIn, 0, read);
            }
        } finally {
            if (bos != null)
                IOUtils.closeQuietly(bos);
        }
    }

    public static final String encodeFilename(final String filename) {
        try{
            URI uri = new URI(null, null, filename, null);
            String encodedName = uri.toASCIIString();
            return encodedName;
        }
        catch(URISyntaxException ex){
            return filename;
        }
    }
}
