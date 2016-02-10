package com.soundcloud.android.crop;

import android.content.ContentResolver;
import android.media.ExifInterface;
import android.net.Uri;
import android.util.*;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Metadata;
import com.drew.metadata.MetadataException;
import com.drew.metadata.exif.ExifIFD0Directory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;


/**
 * Created by jpadilla on 10/2/16.
 */
public class ExifUtil {

    private static final String TAG = ExifUtil.class.getSimpleName();


    public static int convertExifRotationToDegrees(int exifRotation) {

        switch (exifRotation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                return 90;
            case ExifInterface.ORIENTATION_ROTATE_180:
                return 180;
            case ExifInterface.ORIENTATION_ROTATE_270:
                return 270;
            default:
                return ExifInterface.ORIENTATION_UNDEFINED;
        }
    }

    public static int convertDegreesToExifRotation(int degrees) {

        int exifRotation;

        switch (degrees) {
            case 90:
                exifRotation = ExifInterface.ORIENTATION_ROTATE_90;
                break;
            case 180:
                exifRotation = ExifInterface.ORIENTATION_ROTATE_180;
                break;
            case 270:
                exifRotation = ExifInterface.ORIENTATION_ROTATE_270;
                break;
            default:
                exifRotation = ExifInterface.ORIENTATION_NORMAL;
        }

        return exifRotation;
    }

    public static boolean writeExifRotation(File destFile, int degrees) {

        int exifRotation = convertDegreesToExifRotation(degrees);

        try {
            ExifInterface exifDest = new ExifInterface(destFile.getAbsolutePath());
            exifDest.setAttribute(ExifInterface.TAG_ORIENTATION, String.valueOf(exifRotation));
            exifDest.saveAttributes();
            return true;
        } catch (IOException e) {
            android.util.Log.e(TAG, "Error setting Exif data", e);
            return false;
        }
    }

    public static boolean copyExifRotation(ContentResolver resolver, Uri sourceUri, File destFile) {
        if (sourceUri == null || destFile == null) return false;
        int srcRotation = readExifRotation(resolver, sourceUri);
        return writeExifRotation(destFile, srcRotation);
    }

    // Reads EXIF orientation in degrees
    public static int readExifRotation(ContentResolver resolver, Uri uriFile) {

        int orientation = 0;
        InputStream is = null;

        try {
            is = resolver.openInputStream(uriFile);
            return readExifRotation(is);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            CropUtil.closeSilently(is);
        }

        return orientation;
    }

    // Reads EXIF orientation in degrees
    public static int readExifRotation(File file) {

        int orientation = 0;

        try {
            Metadata metadata = ImageMetadataReader.readMetadata(file);
            orientation = convertExifRotationToDegrees(readRawExifRotation(metadata));
        } catch (ImageProcessingException | IOException e) {
            e.printStackTrace();
        }

        return orientation;
    }

    // This method does not close the input stream passed as argument
    private static int readExifRotation(InputStream is) {

        int orientation = 0;

        try {
            Metadata metadata = ImageMetadataReader.readMetadata(is);
            orientation = convertExifRotationToDegrees(readRawExifRotation(metadata));
        } catch (ImageProcessingException | IOException e) {
            e.printStackTrace();
        }

        return orientation;
    }

    private static int readRawExifRotation(final Metadata metadata) {
        int rawExifValue = 0;
        ExifIFD0Directory directory = metadata.getFirstDirectoryOfType(ExifIFD0Directory.class);
        if (directory != null) {
            try {
                rawExifValue = directory.getInt(ExifIFD0Directory.TAG_ORIENTATION);
            } catch (MetadataException e) {
                e.printStackTrace();
            }
        }
        return rawExifValue;
    }
}
