package cn.carhouse.dbsample;

import android.app.Application;

import cn.carhouse.db.core.QuickDaoFactory;

public class BaseApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // 数据库初始化
        QuickDaoFactory.getInstance().initPublicSQLite(this);
    }
}
