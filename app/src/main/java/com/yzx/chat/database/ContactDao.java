package com.yzx.chat.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.yzx.chat.bean.ContactBean;
import com.yzx.chat.bean.ContactRemarkBean;
import com.yzx.chat.bean.UserBean;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by YZX on 2017年11月24日.
 * 每一个不曾起舞的日子,都是对生命的辜负.
 */


public class ContactDao extends AbstractDao<ContactBean> {

    private static final String TABLE_NAME = "Contact";

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


    public static final String CREATE_TABLE_SQL =
            "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " ("
                    + COLUMN_NAME_ContactID + " TEXT NOT NULL , "
                    + COLUMN_NAME_RemarkName + " TEXT,"
                    + COLUMN_NAME_Description + " TEXT,"
                    + COLUMN_NAME_Telephone + " TEXT,"
                    + COLUMN_NAME_Tags + " TEXT,"
                    + COLUMN_NAME_UploadFlag + " INTEGER,"
                    + "PRIMARY KEY (" + COLUMN_NAME_ContactID + ")"
                    + ")";

    public ContactDao(ReadWriteHelper helper) {
        super(helper);
    }

    public ContactBean getContact(String contactID) {
        if (TextUtils.isEmpty(contactID)) {
            return null;
        }
        SQLiteDatabase database = mReadWriteHelper.openReadableDatabase();
        Cursor cursor = database.rawQuery("SELECT * FROM " + TABLE_NAME + " INNER JOIN " + UserDao.TABLE_NAME + " ON " + TABLE_NAME + "." + COLUMN_NAME_ContactID + "=" + UserDao.TABLE_NAME + "." + UserDao.COLUMN_NAME_UserID + " AND " + TABLE_NAME + "." + COLUMN_NAME_ContactID + "=?", new String[]{contactID});
        ContactBean contact = null;
        while (cursor.moveToNext()) {
            contact = toEntity(cursor);
        }
        cursor.close();
        mReadWriteHelper.closeReadableDatabase();
        return contact;
    }

    public List<ContactBean> loadAllContacts() {
        SQLiteDatabase database = mReadWriteHelper.openReadableDatabase();
        Cursor cursor = database.rawQuery("SELECT * FROM " + TABLE_NAME + " INNER JOIN " + UserDao.TABLE_NAME + " ON " + TABLE_NAME + "." + COLUMN_NAME_ContactID + "=" + UserDao.TABLE_NAME + "." + UserDao.COLUMN_NAME_UserID, null);
        List<ContactBean> contactList = new ArrayList<>(cursor.getCount());
        while (cursor.moveToNext()) {
            contactList.add(toEntity(cursor));
        }
        cursor.close();
        mReadWriteHelper.closeReadableDatabase();
        return contactList;
    }

    public boolean insertAllContacts(List<ContactBean> contactList) {
        if (contactList == null || contactList.size() == 0) {
            return true;
        }
        List<UserBean> userList = new LinkedList<>();
        for (ContactBean contact : contactList) {
            userList.add(contact.getUserProfile());
        }
        return new UserDao(mReadWriteHelper).replaceAll(userList) && insertAll(contactList);
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
    protected String[] toWhereArgsOfKey(ContactBean entity) {
        return new String[]{entity.getUserProfile().getUserID()};
    }

    @Override
    protected void parseToContentValues(ContactBean entity, ContentValues values) {
        values.put(COLUMN_NAME_ContactID, entity.getUserProfile().getUserID());
        ContactRemarkBean remark = entity.getRemark();
        if (remark != null) {
            values.put(COLUMN_NAME_RemarkName, remark.getRemarkName());
            values.put(COLUMN_NAME_Description, remark.getDescription());
            values.put(COLUMN_NAME_UploadFlag, remark.getUploadFlag());

            List<String> telephone = remark.getTelephone();
            if (telephone != null && telephone.size() > 0) {
                StringBuilder stringBuilder = new StringBuilder();
                for (int i = 0, size = telephone.size(); i < size; i++) {
                    stringBuilder.append(telephone.get(i));
                    if (i != size - 1) {
                        stringBuilder.append(";");
                    }
                }
                values.put(COLUMN_NAME_Telephone, stringBuilder.toString());
            }

            List<String> tags = remark.getTags();
            if (tags != null && tags.size() > 0) {
                StringBuilder stringBuilder = new StringBuilder();
                for (int i = 0, size = tags.size(); i < size; i++) {
                    stringBuilder.append(tags.get(i));
                    if (i != size - 1) {
                        stringBuilder.append(";");
                    }
                }
                values.put(COLUMN_NAME_Tags, stringBuilder.toString());
            }
        }
    }

    @Override
    protected ContactBean toEntity(Cursor cursor) {
        ContactBean contact = new ContactBean();
        ContactRemarkBean remark = new ContactRemarkBean();

        remark.setRemarkName(cursor.getString(cursor.getColumnIndex(COLUMN_NAME_RemarkName)));
        remark.setDescription(cursor.getString(cursor.getColumnIndex(COLUMN_NAME_Description)));
        remark.setUploadFlag(cursor.getInt(cursor.getColumnIndex(COLUMN_NAME_UploadFlag)));

        String telephone = cursor.getString(cursor.getColumnIndex(COLUMN_NAME_Telephone));
        if (!TextUtils.isEmpty(telephone)) {
            String[] telephones = telephone.split(";");
            remark.setTelephone(new ArrayList<>(Arrays.asList(telephones)));
        }

        String tag = cursor.getString(cursor.getColumnIndex(COLUMN_NAME_Tags));
        if (!TextUtils.isEmpty(tag)) {
            String[] tags = tag.split(";");
            remark.setTags(new ArrayList<>(Arrays.asList(tags)));
        }

        contact.setRemark(remark);
        contact.setUserProfile(UserDao.toEntityFromCursor(cursor));
        return contact;
    }
}
