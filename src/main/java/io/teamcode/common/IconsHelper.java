package io.teamcode.common;

import io.teamcode.common.vcs.svn.RepositoryEntryType;

import java.security.spec.ECField;
import java.util.Arrays;
import java.util.List;

/**
 * Created by chiang on 2017. 4. 10..
 */
public class IconsHelper {

    private static final List<String> CODE_EXTS
            = Arrays.asList("sh", "css", "scss", "js");

    private static final List<String> IMAGE_EXTS
            = Arrays.asList("jpg", "jpeg", "jif", "jfif", "jp2", "jpx", "j2k", "j2c", "png", "gif", "tif", "tiff", "svg", "ico", "bmp");

    private static final List<String> ARCHIVE_EXTS
            = Arrays.asList("zip", "zipx", "tar", "gz", "bz", "bzip", "xz", "rar", "7z");

    private static final List<String> AUDIO_EXTS
            = Arrays.asList("mp3", "wma", "ogg", "oga", "wav", "flac", "aac");

    private static final List<String> VIDEO_EXTS
            = Arrays.asList("mp4", "m4p", "m4v", "mpg", "mp2", "mpeg", "mpe", "mpv", "mpg", "mpeg", "m2v", "avi", "mkv", "flv", "ogv", "mov", "3gp", "3g2");

    private static final List<String> WORD_EXTS
            = Arrays.asList("doc", "dot", "docx", "docm", "dotx", "dotm", "docb");

    private static final List<String> EXCEL_EXTS
            = Arrays.asList("xls", "xlt", "xlm", "xlsx", "xlsm", "xltx", "xltm", "xlsb", "xla", "xlam", "xll", "xlw");

    /*
    when '.ppt', '.pot', '.pps', '.pptx', '.pptm', '.potx', '.potm',
          '.ppam', '.ppsx', '.ppsm', '.sldx', '.sldm'
        icon_class = ''
     */
    private static final List<String> POWERPOINT_EXTS
            = Arrays.asList("ppt", "pot", "pps", "pptx", "pptm", "potx", "potm", "ppam", "ppsx", "ppsm", "sldx", "sldm");

    public static final String fileTypeIconClass(RepositoryEntryType type, String fileName) {
        if (type == RepositoryEntryType.DIRECTORY) {
            return "folder";
        }
        else {
            String ext = Strings.getFilenameExtension(fileName);
            String iconClass;
            if (ext != null) {
                ext = ext.toLowerCase();
                if (ext.equals("pdf")) {
                    iconClass = "file-pdf-o";
                }
                else if (CODE_EXTS.contains(ext)) {
                    iconClass = "file-code-o";
                }
                else if (IMAGE_EXTS.contains(ext)) {
                    iconClass = "file-image-o";
                } else if (ARCHIVE_EXTS.contains(ext)) {
                    iconClass = "file-archive-o";
                } else if (AUDIO_EXTS.contains(ext)) {
                    iconClass = "file-audio-o";
                } else if (VIDEO_EXTS.contains(ext)) {
                    iconClass = "file-video-o";
                } else if (WORD_EXTS.contains(ext)) {
                    iconClass = "file-word-o";
                } else if (EXCEL_EXTS.contains(ext)) {
                    iconClass = "file-excel-o";
                } else if (POWERPOINT_EXTS.contains(ext)) {
                    iconClass = "file-powerpoint-o";
                } else {
                    iconClass = "file-text-o";
                }
            } else {
                iconClass = "file-text-o";
            }

            return iconClass;
        }
    }

    /*

    if type == 'folder'
      icon_class = 'folder'
    elsif mode == '120000'
      icon_class = 'share'
    else
     # Guess which icon to choose based on file extension.
      # If you think a file extension is missing, feel free to add it on PR
      case File.extname(name).downcase
      when '.pdf'
        icon_class = 'file-pdf-o'
      when '.jpg', '.jpeg', '.jif', '.jfif',
          '.jp2', '.jpx', '.j2k', '.j2c',
          '.png', '.gif', '.tif', '.tiff',
          '.svg', '.ico', '.bmp'
        icon_class = 'file-image-o'
      when '.zip', '.zipx', '.tar', '.gz', '.bz', '.bzip',
          '.xz', '.rar', '.7z'
        icon_class = 'file-archive-o'
      when '.mp3', '.wma', '.ogg', '.oga', '.wav', '.flac', '.aac'
        icon_class = 'file-audio-o'
      when '.mp4', '.m4p', '.m4v',
          '.mpg', '.mp2', '.mpeg', '.mpe', '.mpv',
          '.mpg', '.mpeg', '.m2v',
          '.avi', '.mkv', '.flv', '.ogv', '.mov',
          '.3gp', '.3g2'
        icon_class = 'file-video-o'
      when '.doc', '.dot', '.docx', '.docm', '.dotx', '.dotm', '.docb'
        icon_class = 'file-word-o'
      when '.xls', '.xlt', '.xlm', '.xlsx', '.xlsm', '.xltx', '.xltm',
          '.xlsb', '.xla', '.xlam', '.xll', '.xlw'
        icon_class = 'file-excel-o'
      when '.ppt', '.pot', '.pps', '.pptx', '.pptm', '.potx', '.potm',
          '.ppam', '.ppsx', '.ppsm', '.sldx', '.sldm'
        icon_class = 'file-powerpoint-o'
      else
        icon_class = 'file-text-o'
      end
    end
     */
}
