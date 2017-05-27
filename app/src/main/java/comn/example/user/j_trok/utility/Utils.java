package comn.example.user.j_trok.utility;

import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.firebase.auth.FirebaseUser;

import java.io.File;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Date;

import comn.example.user.j_trok.R;
import comn.example.user.j_trok.models.User;

/**
 * Created by Larry Akah on 5/19/17.
 */

public class Utils {

    private static final String TAG = Utils.class.getName();
    public static final String PREF_RECORDING_DURATION = "RECORDING_DURATION";
    public static final String PREF_SHOW_HINTS = "SHOW_HINTS_PREF";
    public static final String ANALYTICS_PARAM__TUTORIAL_ID = "TUTORIAL_ID";
    public static final String ANALYTICS_PARAM__TUTORIAL_NAME = "HOME_TUTORIAL";
    public static final String ANALYTICS_PARAM__TUTORIAL_CATEGORY = "TUTORIALS";
    public static final String CURRENT_USER = "CURRENT_USER";
    public static final String ANALYTICS_PARAM__LAUNCH_ID = "LAUNCH_ID";
    public static final String ANALYTICS_PARAM__LAUNCH_NAME = "APP_LAUNCH_NAME";
    public static final String ANALYTICS_PARAM__LAUNCH_CATEGORY = "APP_LUNCH_CATEGORY";

    /**
     * Get simple time elapsed and represent as human readable
     * @param c context to fetch string resources
     * @param previousTimestamp previous timestamp in the past
     * @return string time for elapsed time since @param previousTimestamp
     */
    public static String getTimeDifference(Context  c, long previousTimestamp ){
        long currentTimeStamp = System.currentTimeMillis();

        long diff = Math.abs(currentTimeStamp - previousTimestamp); //avoid negative values however
        if (diff > 86 * Math.pow(10,6))
            return SimpleDateFormat.getDateTimeInstance().format(new Date(previousTimestamp));
        //now do calculations and round up to nearest second, minute or hour
        double intervalInSeconds = diff / Math.pow(10, 6); //convert to seconds
        if (intervalInSeconds < 60)
            return c.getString(R.string.timeinterval, Math.round(intervalInSeconds), "s"); //time in seconds
        if (intervalInSeconds < 3600)
            return c.getString(R.string.timeinterval, Math.round((intervalInSeconds / 60 )), "m"); //time in minutes
        if (intervalInSeconds > 3600)
            return c.getString(R.string.timeinterval, Math.round((intervalInSeconds / 3600 )), "h"); //time in hours
        return SimpleDateFormat.getDateTimeInstance().format(new Date(previousTimestamp));
    }

    public static User getUserConfig(@NonNull  FirebaseUser user){
        User muser = new User();
        muser.setUserEmail(user.getEmail());
        muser.setUserName(user.getDisplayName());
        muser.setUserCountry("");
        muser.setUserCity("");
        muser.setUserId(user.getUid());
        muser.setUserProfilePhoto(user.getPhotoUrl().toString());
        return muser;
    }

    public static void deleteFilesAtPath( File parentDir )
    {
        File[] files = parentDir.listFiles();
        if ( files != null && files.length > 0 )
        {
            Log.d(TAG, "Deleting Empty Files in " + parentDir );
            for ( File file: files )
            {
                if ( file.getName().endsWith( ".mp4" ) && file.length() == 0 )
                {

                    File myFile = new File( parentDir + "/" + file.getName() );
                    Log.d(TAG, "Delete file: " + myFile.getAbsolutePath());
                    myFile.delete();
                }
            }
        }
    }

    public static void deleteEmptyVideos( Context ctx )
    {
        deleteFilesAtPath( new File(getVideoDirPath( ctx )) );
    }

    public static File getSdCard()
    {
        return Environment.getExternalStorageDirectory();
    }

    public static String getVideoDirPath( Context ctx )
    {
        return getSdCard().getAbsolutePath() + "/Android/data/" + ctx.getPackageName() + "/media/videos";
    }

    public static String getImageDirPath( Context ctx )
    {
        return getSdCard().getAbsolutePath() + "/Android/data/" + ctx.getPackageName() + "/media/images";
    }

    @SuppressLint("NewApi")
    public static String getFilePath(Context context, Uri uri) throws URISyntaxException {
        String selection = null;
        String[] selectionArgs = null;
        // Uri is different in versions after KITKAT (Android 4.4), we need to
        if (Build.VERSION.SDK_INT >= 19 && DocumentsContract.isDocumentUri(context.getApplicationContext(), uri)) {
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                return Environment.getExternalStorageDirectory() + "/" + split[1];
            } else if (isDownloadsDocument(uri)) {
                final String id = DocumentsContract.getDocumentId(uri);
                uri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
            } else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];
                if ("image".equals(type)) {
                    uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }
                selection = "_id=?";
                selectionArgs = new String[]{
                        split[1]
                };
            }
        }
        if ("content".equalsIgnoreCase(uri.getScheme())) {
            String[] projection = {
                    MediaStore.Images.Media.DATA
            };
            Cursor cursor = null;
            try {
                cursor = context.getContentResolver()
                        .query(uri, projection, selection, selectionArgs, null);
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                if (cursor.moveToFirst()) {
                    return cursor.getString(column_index);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }
        return null;
    }

    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }
}
