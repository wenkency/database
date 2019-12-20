package cn.carhouse.db.core;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import cn.carhouse.db.subdb.PrivateDBSupport;
import cn.carhouse.db.utils.QuickDaoUtils;


/**
 * 数据库查询
 */
public class QuickDaoFactory {
    // 公共数据库：包名/database/user.db
    // 私有数据库: 包名/database/userId/login.db


    private static QuickDaoFactory mInstance;
    // 公共库操作
    private SQLiteDatabase mSqLiteDatabase;
    protected Context mContext;

    // 定义一个用于实现分库的数据库操作对象
    protected SQLiteDatabase mSubSQLiteDatabase;
    // 设计一个数据库连接池
    protected Map<String, QuickDao> mCacheMap = Collections.synchronizedMap(new HashMap<String, QuickDao>());

    private String mUserId;


    protected QuickDaoFactory() {
    }

    public static QuickDaoFactory getInstance() {
        if (mInstance == null) {
            synchronized (QuickDaoFactory.class) {
                if (mInstance == null) {
                    mInstance = new QuickDaoFactory();
                }
            }
        }
        return mInstance;
    }

    /**
     * 初始化方法:主要是创建公有的SQLiteDatabase
     */
    public void initPublicSQLite(Context context) {
        this.mContext = context;
        mSqLiteDatabase = QuickDaoUtils.getPublicSQLiteDatabase(context);
    }

    /**
     * 初始化方法:主要是创建私有的SQLiteDatabase
     */
    private void initSubSQLite(String userId) {
        if (TextUtils.isEmpty(userId)) {
            return;
        }
        this.mUserId = userId;
        mSubSQLiteDatabase = QuickDaoUtils.getPrivateSQLiteDatabase(mContext, userId);
    }

    /**
     * 获取数据库操作对象
     */
    public <T extends QuickDao<M>, M> T getQuickDao(String key, Class<T> daoClazz, Class<M> clazz, SQLiteDatabase sqLiteDatabase) {
        if (sqLiteDatabase == null) {
            throw new RuntimeException("SQLiteDatabase is null ");
        }
        T dao = null;
        if (mCacheMap.get(key) != null) {
            dao = (T) mCacheMap.get(key);
            dao.init(sqLiteDatabase);
            return dao;
        }
        try {
            dao = daoClazz.newInstance();
            dao.init(sqLiteDatabase, clazz);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        mCacheMap.put(key, dao);
        return dao;
    }


    /**
     * 获取数据库操作对象
     */
    public <T extends QuickDao<M>, M> T getQuickDao(Class<T> daoClazz, Class<M> clazz) {
        return getQuickDao(daoClazz.getSimpleName(), daoClazz, clazz, mSqLiteDatabase);
    }

    /**
     * 获取数据库操作对象
     */
    public <M> QuickDao<M> getQuickDao(Class<M> clazz) {
        return getQuickDao(QuickDaoImpl.class, clazz);
    }

    /**
     * 获取分库操作对象
     */
    public <T extends QuickDao<M>, M> T getSubQuickDao(Class<T> daoClazz, Class<M> clazz, PrivateDBSupport support) {
        // 获取登录用户的信息
        String userId = support.getId();
        initSubSQLite(userId);
        T quickDao = getQuickDao(userId, daoClazz, clazz, mSubSQLiteDatabase);
        quickDao.setSubQuickDao(true);
        return quickDao;
    }

    /**
     * 获取分库操作对象
     */
    public <M> QuickDao<M> getSubQuickDao(Class<M> clazz, PrivateDBSupport support) {
        return getSubQuickDao(QuickDaoImpl.class, clazz, support);
    }


    public void closeSQLiteDatabase() {
        if (mSqLiteDatabase != null && mSqLiteDatabase.isOpen()) {
            mSqLiteDatabase.close();
            mSqLiteDatabase = null;
        }
    }

    public void closeSubSQLiteDatabase() {
        if (mSubSQLiteDatabase != null && mSubSQLiteDatabase.isOpen()) {
            mSubSQLiteDatabase.close();
            mSubSQLiteDatabase = null;
        }
    }

    public void closeAll() {
        closeSQLiteDatabase();
        closeSubSQLiteDatabase();
    }

    public void clearCache() {
        closeAll();
        // 查一下
        initPublicSQLite(mContext);
        initSubSQLite(mUserId);
        // 更新查询
        for (Map.Entry<String, QuickDao> entry : mCacheMap.entrySet()) {
            QuickDao quickDao = entry.getValue();
            if (quickDao.isSubQuickDao()) {
                quickDao.init(mSubSQLiteDatabase);
            } else {
                quickDao.init(mSqLiteDatabase);
            }
        }
    }
}
