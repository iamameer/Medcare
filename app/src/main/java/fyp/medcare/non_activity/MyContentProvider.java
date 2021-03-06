/*
 * MyContentProvider: ContentProvider class to interact with database
 *
 * Methods          :  insert() : insert a record to database
 *                     query()  : perform a query
 *                     delete() : delete a record from database
 *                     update() : (not implemented) updates database
 */

package fyp.medcare.non_activity;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

public class MyContentProvider extends ContentProvider {

    //Global Variables
    private DBHelper myDB;

    private static final String AUTHORITY = "fyp.medcare.non_activity.MyContentProvider";
    public static final Uri CONTENT_URI =
            Uri.parse("content://" + AUTHORITY + "/" + HospitalContract.HospitalEntry.TABLE_NAME);

    public static final int HOSPITAL = 1;
    public static final int HOSPITAL_ID = 2;

    private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sURIMatcher.addURI(AUTHORITY, HospitalContract.HospitalEntry.TABLE_NAME, HOSPITAL);
        sURIMatcher.addURI(AUTHORITY, HospitalContract.HospitalEntry.TABLE_NAME+"/#", HOSPITAL_ID);
    }

    //empty constructor
    public MyContentProvider() {
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Implement this to handle requests to delete one or more rows.
        int uriType = sURIMatcher.match(uri);
        SQLiteDatabase sqlDB = myDB.getWritableDatabase();
        int rowsDeleted = 0;

        switch (uriType){
            case HOSPITAL:
                rowsDeleted = sqlDB.delete(HospitalContract.HospitalEntry.TABLE_NAME,
                        selection,
                        selectionArgs);
                break;
            case HOSPITAL_ID:
                String id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)){
                    rowsDeleted = sqlDB.delete(HospitalContract.HospitalEntry.TABLE_NAME,
                            HospitalContract.HospitalEntry.COLUMN_NAME_ID+"="+id,
                            null);
                }else{
                    rowsDeleted = sqlDB.delete(HospitalContract.HospitalEntry.TABLE_NAME,
                            HospitalContract.HospitalEntry.COLUMN_NAME_ID+"="+id,
                            selectionArgs);
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown URI:"+uri);
        }
        getContext().getContentResolver().notifyChange(uri,null);
        return rowsDeleted;
    }

    @Override
    public String getType(Uri uri) {
        //Implement this to handle requests for the MIME type of the data
        // at the given URI.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        //Implement this to handle requests to insert a new row.
        int uriType = sURIMatcher.match(uri);
        SQLiteDatabase sqlDB = myDB.getWritableDatabase();

        long id = 0;
        switch (uriType){
            case HOSPITAL:
                id = sqlDB.insert(HospitalContract.HospitalEntry.TABLE_NAME,null,values);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI:"+uri);
        }
        getContext().getContentResolver().notifyChange(uri,null);
        return Uri.parse(HospitalContract.HospitalEntry.TABLE_NAME+"/"+id);
    }

    @Override
    public boolean onCreate() {
        //Initialize your content provider on startup.
        myDB = new DBHelper(getContext(),null,null,1);
        return false;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        //Handle query requests from clients.
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        queryBuilder.setTables(HospitalContract.HospitalEntry.TABLE_NAME);

        int uriType = sURIMatcher.match(uri);

        switch (uriType){
            case HOSPITAL_ID:
                queryBuilder.appendWhere(HospitalContract.HospitalEntry.COLUMN_NAME_ID+"="
                        +uri.getLastPathSegment());
                break;

            case HOSPITAL:
                break;
            default:
                throw new IllegalArgumentException("Unknown URI");
        }

        Cursor cursor = queryBuilder.query(myDB.getReadableDatabase(),
                projection,selection,selectionArgs,null,null,sortOrder);
        cursor.setNotificationUri(getContext().getContentResolver(),uri);
        return cursor;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        //Handle requests to update one or more rows.
        int uriType = sURIMatcher.match(uri);
        SQLiteDatabase sqlDB = myDB.getWritableDatabase();

        int rowsUpdated = 0;

        switch (uriType){
            case HOSPITAL:
                rowsUpdated =
                        sqlDB.update(HospitalContract.HospitalEntry.TABLE_NAME,
                                values,
                                selection,
                                selectionArgs);
                break;
            case HOSPITAL_ID:
                String id = uri.getLastPathSegment();
                if(TextUtils.isEmpty(selection)){
                    rowsUpdated =
                            sqlDB.update(HospitalContract.HospitalEntry.TABLE_NAME,
                                    values,
                                    HospitalContract.HospitalEntry.COLUMN_NAME_ID+"="+id,
                                    null);
                }else{
                    rowsUpdated =
                            sqlDB.update(HospitalContract.HospitalEntry.TABLE_NAME,
                                    values,
                                    HospitalContract.HospitalEntry.COLUMN_NAME_ID+"="+id
                                            + " and "
                                            + selection,
                                    selectionArgs);
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: "+uri);
        }
        getContext().getContentResolver().notifyChange(uri,null);
        return rowsUpdated;
    }
}
