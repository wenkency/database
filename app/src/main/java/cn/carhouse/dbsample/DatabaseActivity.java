package cn.carhouse.dbsample;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cn.carhouse.db.core.QuickDao;
import cn.carhouse.db.core.QuerySupport;
import cn.carhouse.db.core.QuickDaoFactory;
import cn.carhouse.dbsample.bean.Photo;
import cn.carhouse.dbsample.bean.User;
import cn.carhouse.dbsample.subdb.PrivateDBImpl;
import cn.carhouse.dbsample.subdb.UpdateDbUtils;
import cn.carhouse.dbsample.subdb.UserDao;


/**
 * 数据库测试类
 */
public class DatabaseActivity extends AppCompatActivity {
    PrivateDBImpl impl = PrivateDBImpl.getPrivateDBImpl();
    private UserDao mUserDao;
    UpdateDbUtils updateDbUtils = new UpdateDbUtils();
    private TextView tv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_database);
        tv = findViewById(R.id.tv);

        mUserDao = QuickDaoFactory.getInstance().getQuickDao(UserDao.class, User.class);
    }

    public void insert(View view) {
        List<User> users = new ArrayList<>();
        for (int i = 0; i < 1; i++) {
            User user = new User();
            user.setUserId(i + "");
            user.setName("Lven");
            user.setState(i);
            user.setPassword("Lven" + i);
            users.add(user);
        }
        final long begin = System.currentTimeMillis();
        mUserDao.insert(users, new QuickDao.OnInsertListener() {
            @Override
            public void onInserted() {
                long end = System.currentTimeMillis() - begin;
                tv.setText("insert-->" + end);
            }
        });
    }

    public void delete(View view) {
        // 删除 id=0 并且 name=Lven的
        User where = new User();
        where.setUserId("0");
        where.setName("Lven");
        int delete = mUserDao.delete(where);
        tv.setText("delete-->" + delete);
    }

    public void update(View view) {
        // update emp set salary=4000,job='ccc' where name='zs';
        User bean = new User();
        bean.setState(0);
        //  将ID =1 的名字改成lfw
        User where = new User();
        where.setUserId("1");
        int update = mUserDao.update(bean);
        tv.setText("update-->" + update);
    }

    public void defLogin(View view) {
        // 用户登录成功后调用
        User bean = new User();
        bean.setUserId(100 + "");
        bean.setName("LFW");
        bean.setState(1);
        mUserDao.onLogin(bean);
    }

    public void changeLogin(View view) {
        // 用户登录成功后调用
        User bean = new User();
        bean.setUserId(0 + "");
        bean.setName("LFW");
        bean.setState(1);
        mUserDao.onLogin(bean);
    }

    public void subInsert(View view) {
        Photo photo = new Photo();
        photo.setPath("data/data/my.jpg");
        photo.setTime(new Date().toString());
        QuickDao<Photo> photoDao = QuickDaoFactory.getInstance().getSubQuickDao(Photo.class, impl);
        long insert = photoDao.insert(photo);
        Toast.makeText(this, "执行成功!" + insert, Toast.LENGTH_LONG).show();
    }

    public void subQuery(View view) {
        QuickDao<Photo> photoDao = QuickDaoFactory.getInstance().getSubQuickDao(Photo.class, impl);
        List<Photo> photos = photoDao.query().queryAll();
        StringBuffer sb = new StringBuffer();
        for (Photo u : photos) {
            sb.append(u.toString() + "\n");
        }
        tv.setText("subQuery-->" + sb.toString());
    }

    public void query(View view) {
        QuerySupport<User> query = mUserDao.query();
        query.limit("0 , 10");
        List<User> list = query.query();
        StringBuffer sb = new StringBuffer();
        for (User u : list) {
            sb.append(u.toString() + "\n");
        }
        tv.setText("query-->" + sb.toString());
    }

    public void asyncQuery(View view) {
        QuerySupport<User> query = mUserDao.query();
        query.limit("0 , 10");
        query.asyncQuery(new QuerySupport.OnAsyncQueryListener<User>() {
            @Override
            public void onAsyncQueried(List<User> items) {
                StringBuffer sb = new StringBuffer();
                for (User u : items) {
                    sb.append(u.toString() + "\n");
                }
                tv.setText("asyncQuery-->" + sb.toString());
            }
        });
    }

    /**
     * 更新数据库版本
     */
    public void updateDb(View view) {
        updateDbUtils.update(this, "V002", "V003");
    }
}
