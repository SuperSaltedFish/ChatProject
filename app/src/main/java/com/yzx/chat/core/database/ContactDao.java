package com.yzx.chat.core.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.yzx.chat.core.entity.ContactEntity;
import com.yzx.chat.core.entity.TagEntity;
import com.yzx.chat.core.entity.UserEntity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by YZX on 2017年11月24日.
 * 每一个不曾起舞的日子,都是对生命的辜负.
 */


public class ContactDao extends AbstractDao<ContactEntity> {

    static final String TABLE_NAME = "Contact";

    private static final String COLUMN_NAME_ContactID = "ContactID";
    private static final String COLUMN_NAME_RemarkName = "RemarkName";
    private static final String COLUMN_NAME_Description = "Description";
    private static final String COLUMN_NAME_Telephone = "TelephoneList";
    private static final String COLUMN_NAME_Tags = "Tags";
    private static final String COLUMN_NAME_UploadFlag = "UploadFlag";

    private static final int COLUMN_INDEX_ContactID = 0;
    private static final int COLUMN_INDEX_RemarkName = 1;
    private static final int COLUMN_INDEX_Description = 2;
    private static final int COLUMN_INDEX_Telephone = 3;
    private static final int COLUMN_INDEX_Tags = 4;
    private static final int COLUMN_INDEX_UploadFlag = 5;


    static final String CREATE_TABLE_SQL =
            "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " ("
                    + COLUMN_NAME_ContactID + " TEXT NOT NULL , "
                    + COLUMN_NAME_RemarkName + " TEXT,"
                    + COLUMN_NAME_Description + " TEXT,"
                    + COLUMN_NAME_Telephone + " TEXT,"
                    + COLUMN_NAME_Tags + " TEXT,"
                    + COLUMN_NAME_UploadFlag + " INTEGER,"
                    + "PRIMARY KEY (" + COLUMN_NAME_ContactID + ")"
                    + ")";

    private static final String MULTI_TABLE_SELECT = "SELECT * FROM " + TABLE_NAME + " INNER JOIN " + UserDao.TABLE_NAME + " ON " + TABLE_NAME + "." + COLUMN_NAME_ContactID + "=" + UserDao.TABLE_NAME + "." + UserDao.COLUMN_NAME_UserID;

    public ContactDao(ReadWriteHelper helper) {
        super(helper);
    }

    public ContactEntity getContact(String contactID) {
        if (TextUtils.isEmpty(contactID)) {
            return null;
        }
        SQLiteDatabase database = mReadWriteHelper.openReadableDatabase();
        Cursor cursor = database.rawQuery(MULTI_TABLE_SELECT + " AND " + TABLE_NAME + "." + COLUMN_NAME_ContactID + "=?", new String[]{contactID});
        ContactEntity contact = null;
        while (cursor.moveToNext()) {
            contact = toEntity(cursor);
        }
        cursor.close();
        mReadWriteHelper.closeReadableDatabase();
        return contact;
    }

    public ArrayList<ContactEntity> loadAllContacts() {
        SQLiteDatabase database = mReadWriteHelper.openReadableDatabase();
        Cursor cursor = database.rawQuery(MULTI_TABLE_SELECT, null);
        ArrayList<ContactEntity> contactList = new ArrayList<>(cursor.getCount());
        while (cursor.moveToNext()) {
            contactList.add(toEntity(cursor));
        }
        cursor.close();
        mReadWriteHelper.closeReadableDatabase();
        return contactList;
    }

    public boolean insertAllContacts(List<ContactEntity> contactList) {
        if (contactList == null || contactList.size() == 0) {
            return true;
        }
        List<UserEntity> userList = new LinkedList<>();
        for (ContactEntity contact : contactList) {
            userList.add(contact.getUserProfile());
        }
        return new UserDao(mReadWriteHelper).replaceAll(userList) && insertAll(contactList);
    }

    public HashSet<String> getAllTagsType() {
        SQLiteDatabase database = mReadWriteHelper.openReadableDatabase();
        Cursor cursor = database.query(true, TABLE_NAME, new String[]{COLUMN_NAME_Tags}, COLUMN_NAME_Tags + " IS NOT NULL", null, null, null, null, null);
        HashSet<String> tagsSet = new HashSet<>();
        String tags;
        while (cursor.moveToNext()) {
            tags = cursor.getString(0);
            if (!TextUtils.isEmpty(tags)) {
                tagsSet.addAll(Arrays.asList(tags.split(";")));
            }
        }
        cursor.close();
        mReadWriteHelper.closeReadableDatabase();
        return tagsSet;
    }

    public ArrayList<TagEntity> getAllTagAndMemberCount() {
        SQLiteDatabase database = mReadWriteHelper.openReadableDatabase();
        Cursor cursor = database.query(false, TABLE_NAME, new String[]{COLUMN_NAME_Tags}, COLUMN_NAME_Tags + " IS NOT NULL", null, null, null, null, null);
        HashMap<String, Integer> tagsMap = new HashMap<>(16);
        String tags;
        Integer value;
        while (cursor.moveToNext()) {
            tags = cursor.getString(0);
            if (!TextUtils.isEmpty(tags)) {
                for (String tag : tags.split(";")) {
                    value = tagsMap.get(tag);
                    if (value == null) {
                        tagsMap.put(tag, 1);
                    } else {
                        tagsMap.put(tag, value + 1);
                    }
                }
            }
        }
        cursor.close();
        mReadWriteHelper.closeReadableDatabase();
        ArrayList<TagEntity> tagList = new ArrayList<>(tagsMap.size());
        for (Map.Entry<String, Integer> entry : tagsMap.entrySet()) {
            tagList.add(new TagEntity(entry.getKey(), entry.getValue()));
        }
        return tagList;
    }

    @Override
    protected String getTableName() {
        return TABLE_NAME;
    }

    @Override
    protected String getWhereClauseOfKey() {
        return COLUMN_NAME_ContactID + "=?";
    }

    @Override
    protected String[] toWhereArgsOfKey(ContactEntity entity) {
        return new String[]{entity.getUserProfile().getUserID()};
    }

    @Override
    protected void parseToContentValues(ContactEntity entity, ContentValues values) {
        values.put(COLUMN_NAME_ContactID, entity.getContactID());
        values.put(COLUMN_NAME_RemarkName, entity.getRemarkName());
        values.put(COLUMN_NAME_Description, entity.getDescription());
        values.put(COLUMN_NAME_UploadFlag, entity.getUploadFlag());
        values.put(COLUMN_NAME_Telephone, listToString(entity.getTelephones()));
        values.put(COLUMN_NAME_Tags, listToString(entity.getTags()));
    }

    @Override
    protected ContactEntity toEntity(Cursor cursor) {
        ContactEntity contact = new ContactEntity();
        contact.setRemarkName(cursor.getString(cursor.getColumnIndex(COLUMN_NAME_RemarkName)));
        contact.setDescription(cursor.getString(cursor.getColumnIndex(COLUMN_NAME_Description)));
        contact.setUploadFlag(cursor.getInt(cursor.getColumnIndex(COLUMN_NAME_UploadFlag)));
        contact.setUserProfile(UserDao.toEntityFromCursor(cursor));

        String telephone = cursor.getString(cursor.getColumnIndex(COLUMN_NAME_Telephone));
        if (!TextUtils.isEmpty(telephone)) {
            String[] telephones = telephone.split(";");
            contact.setTelephones(new ArrayList<>(Arrays.asList(telephones)));
        }

        String tag = cursor.getString(cursor.getColumnIndex(COLUMN_NAME_Tags));
        if (!TextUtils.isEmpty(tag)) {
            String[] tags = tag.split(";");
            contact.setTags(new ArrayList<>(Arrays.asList(tags)));
        }
        return contact;
    }

    public static String listToString(List<String> strList) {
        int size = strList == null ? 0 : strList.size();
        if (size > 0) {
            StringBuilder stringBuilder = new StringBuilder(13 * size);
            for (int i = 0; i < size; i++) {
                stringBuilder.append(strList.get(i));
                if (i != size - 1) {
                    stringBuilder.append(";");
                }
            }
            return stringBuilder.toString();
        } else {
            return "";
        }
    }
}
