package com.yzx.chat.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.yzx.chat.bean.ContactBean;
import com.yzx.chat.bean.ContactRemarkBean;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by YZX on 2017年11月24日.
 * 每一个不曾起舞的日子,都是对生命的辜负.
 */

public class ContactDao extends AbstractDao<ContactBean> {

    protected static final String TABLE_NAME = "Contact";

    public static final String COLUMN_NAME_ContactOf = "ContactOf";
    private static final String COLUMN_NAME_UserID = "UserID";
    public static final String COLUMN_NAME_Nickname = "Nickname";
    private static final String COLUMN_NAME_Avatar = "Avatar";
    public static final String COLUMN_NAME_RemarkName = "RemarkName";
    private static final String COLUMN_NAME_Description = "Description";
    private static final String COLUMN_NAME_Telephone = "Telephone";
    private static final String COLUMN_NAME_Tags = "Tags";
    private static final String COLUMN_NAME_UploadFlag = "UploadFlag";

    private static final int COLUMN_INDEX_ContactOf = 0;
    private static final int COLUMN_INDEX_UserID = 1;
    private static final int COLUMN_INDEX_Nickname = 2;
    private static final int COLUMN_INDEX_Avatar = 3;
    private static final int COLUMN_INDEX_RemarkName = 4;
    private static final int COLUMN_INDEX_Description = 5;
    private static final int COLUMN_INDEX_Telephone = 6;
    private static final int COLUMN_INDEX_Tags = 7;
    private static final int COLUMN_INDEX_UploadFlag = 8;

    public static final String CREATE_TABLE_SQL =
            "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " ("
                    + COLUMN_NAME_ContactOf + "  TEXT NOT NULL,"
                    + COLUMN_NAME_UserID + " TEXT NOT NULL , "
                    + COLUMN_NAME_Nickname + " TEXT NOT NULL,"
                    + COLUMN_NAME_Avatar + " TEXT,"
                    + COLUMN_NAME_RemarkName + " TEXT,"
                    + COLUMN_NAME_Description + " TEXT,"
                    + COLUMN_NAME_Telephone + " TEXT,"
                    + COLUMN_NAME_Tags + " TEXT,"
                    + COLUMN_NAME_UploadFlag + " INTEGER,"
                    + "PRIMARY KEY (" + COLUMN_NAME_ContactOf + ", " + COLUMN_NAME_UserID + ")"
                    + ")";


    public List<ContactBean> loadAllContacts(String contactOf) {
        if (TextUtils.isEmpty(contactOf)) {
            return null;
        }
        SQLiteDatabase database = mHelper.openReadableDatabase();
        Cursor cursor = database.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE " + COLUMN_NAME_ContactOf + "=?", new String[]{contactOf});
        List<ContactBean> contactList = new ArrayList<>(cursor.getCount());
        while (cursor.moveToNext()) {
            contactList.add(toEntity(cursor));
        }
        cursor.close();
        mHelper.closeReadableDatabase();
        return contactList;
    }

    public void loadAllContactsTo(List<ContactBean> dataList, String contactOf) {
        if (TextUtils.isEmpty(contactOf)) {
            return;
        }
        SQLiteDatabase database = mHelper.openReadableDatabase();
        Cursor cursor = database.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE " + COLUMN_NAME_ContactOf + "=?", new String[]{contactOf});
        while (cursor.moveToNext()) {
            dataList.add(toEntity(cursor));
        }
        cursor.close();
        mHelper.closeReadableDatabase();
    }


    @Override
    protected String getTableName() {
        return TABLE_NAME;
    }

    @Override
    protected String getWhereClauseOfKey() {
        return COLUMN_NAME_ContactOf + "=? and " + COLUMN_NAME_UserID + "=?";
    }

    @Override
    protected String[] toWhereArgsOfKey(ContactBean entity) {
        return new String[]{entity.getContactOf(), entity.getUserID()};
    }

    @Override
    protected ContentValues toContentValues(ContactBean entity, ContentValues values) {
        values.put(COLUMN_NAME_ContactOf, entity.getContactOf());
        values.put(COLUMN_NAME_UserID, entity.getUserID());
        values.put(COLUMN_NAME_Nickname, entity.getNickname());
        values.put(COLUMN_NAME_Avatar, entity.getAvatar());
        ContactRemarkBean remark = entity.getRemark();
        if (remark != null) {
            values.put(COLUMN_NAME_RemarkName, remark.getRemarkName());
            values.put(COLUMN_NAME_Description, remark.getDescription());
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
        return values;
    }

    @Override
    protected ContactBean toEntity(Cursor cursor) {
        ContactBean bean = new ContactBean();
        bean.setContactOf(cursor.getString(COLUMN_INDEX_ContactOf));
        bean.setUserID(cursor.getString(COLUMN_INDEX_UserID));
        bean.setNickname(cursor.getString(COLUMN_INDEX_Nickname));
        bean.setAvatar(cursor.getString(COLUMN_INDEX_Avatar));
        ContactRemarkBean remark = new ContactRemarkBean();
        remark.setRemarkName(cursor.getString(COLUMN_INDEX_RemarkName));
        remark.setDescription(cursor.getString(COLUMN_INDEX_Description));
        remark.setUploadFlag(cursor.getInt(COLUMN_INDEX_UploadFlag));

        String telephone = cursor.getString(COLUMN_INDEX_Telephone);
        if (!TextUtils.isEmpty(telephone)) {
            String[] telephones = telephone.split(";");
            remark.setTelephone(new ArrayList<>(Arrays.asList(telephones)));
        }

        String tag = cursor.getString(COLUMN_INDEX_Tags);
        if (!TextUtils.isEmpty(tag)) {
            String[] tags = tag.split(";");
            remark.setTags(new ArrayList<>(Arrays.asList(tags)));
        }
        bean.setRemark(remark);
        return bean;
    }
}
