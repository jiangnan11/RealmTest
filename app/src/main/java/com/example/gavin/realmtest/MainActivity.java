package com.example.gavin.realmtest;

import android.database.CursorJoiner;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import io.realm.DynamicRealm;
import io.realm.DynamicRealmObject;
import io.realm.FieldAttribute;
import io.realm.Realm;
import io.realm.RealmAsyncTask;
import io.realm.RealmConfiguration;
import io.realm.RealmMigration;
import io.realm.RealmObjectSchema;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import io.realm.RealmSchema;
import io.realm.Sort;

//使用方法 http://blog.csdn.net/wa172126691/article/details/53103973

//空间优化 https://blog.csdn.net/u012003460/article/details/74625600
public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private final String TAG = "MainActivity";
    private Realm mRealm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initRealm();
        //createUser();
        //retrieveUser();
        //updateUser();

    }

    /**
     * 初始化Realm
     */

    private void initRealm() {
        initView();
        Realm.init(this);
        //mRealm = Realm.getDefaultInstance();
        RealmConfiguration configuration = new RealmConfiguration.Builder()
                .name("test.realm")//配置名字
                .schemaVersion(2)//版本号
                .migration(new CustomMigration())//升级数据库
                //.deleteRealmIfMigrationNeeded()
                .build();

        mRealm = Realm.getInstance(configuration);
        String str = mRealm.getPath();
        Log.d(TAG, str);
    }


    /**
     * 升级数据库
     */
    class CustomMigration implements RealmMigration {
        @Override
        public void migrate(final DynamicRealm realm, long oldVersion, long newVersion) {
            RealmSchema schema = realm.getSchema();
            /************************************************
             // Version 0
             class User
             String name;
             int    age;

             // Version 1
             class User
             @Required int    id;
             String name;
             ************************************************/
            if (oldVersion == 0 && newVersion == 1) {
                RealmObjectSchema personSchema = schema.get("User");
                //新增@Required的id
                personSchema
                        .addField("id", String.class, FieldAttribute.REQUIRED)
                        .transform(new RealmObjectSchema.Function() {
                            @Override
                            public void apply(DynamicRealmObject obj) {
                                obj.set("id", "1");//为id设置值
                            }
                        })
                        .removeField("age");//移除age属性
                oldVersion++;
            }

            /************************************************
             // Version 0
             class User
             @Required int    id;
             String name;

             // Version 1
             class Cat
             String name;
             int age;

             class User
             @Required int    id;
             String name;
             RealmList<Dog> cats;
             ************************************************/
            if (oldVersion == 1 && newVersion == 2) {
                //创建Dog表
                RealmObjectSchema dogSchema = schema.create("Dog");
                dogSchema.addField("name", String.class);
                dogSchema.addField("age", int.class);

                //User中添加dogs属性
                schema.get("User")
                        .addRealmListField("dogs", dogSchema)
                        .transform(new RealmObjectSchema.Function() {
                            @Override
                            public void apply(DynamicRealmObject obj) {
                                //为已存在的数据设置Cat类
                                DynamicRealmObject dog = realm.createObject("Dog");
                                dog.set("name", "二哈");
                                dog.set("age", 2);
                                obj.getList("dogs").add(dog);
                            }
                        });
                oldVersion++;
            }
            if(oldVersion == 2 && newVersion == 3) {

            }
        }
    }

    private void initView() {
        findViewById(R.id.create).setOnClickListener(this);
        findViewById(R.id.retrieve).setOnClickListener(this);
        findViewById(R.id.update).setOnClickListener(this);
        findViewById(R.id.delete).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.create:
//                createUser();
//                createdTest1();
                realmInsert(mRealm);
                break;
            case R.id.retrieve://查询
                //retrieveUser();
//                retrieveTest1();
                select();
                break;
            case R.id.update:
//                updateUser();
                realmUpdate(mRealm);
                break;
            case R.id.delete:
//                deleteUser();
                delete();
                break;
        }
    }

    private void delete() {
        mRealm.beginTransaction();
        User user = mRealm.where(User.class).equalTo("name", "李斯").findFirst();//获取符合条件的数据
        if(user !=null){
            user.deleteFromRealm();//删除
        }
        mRealm.commitTransaction();
    }

    //查询
    private void select() {
        //查询一个数据库中第一个book
//        User user = mRealm.where(User.class)
//                .equalTo("id","1")//相当于where name='史记'
//                .or()//或，连接查询条件；没有这个方法时，默认是隐式地被逻辑和(&)组合
//                .equalTo("name","司马迁")//相当于 author='司马迁'
//                .findFirst();

        //整体相当于select * from (test.realm) where name='史记' or author='司马迁' limit 1;
        //Book book = realm.where(Book.class).findFirst();

        //查询多条数据
        RealmResults<User> result = mRealm.where(User.class).findAll();
//Result.sort(“name”);
//        result.sort("name", Sort.DESCENDING);//降序
        result.sort("name", Sort.ASCENDING);//升序

        showUser(result);
//        mRealm.close();

//        Log.d(TAG, "select: "+user.toString());
    }

    //添加一条数据到数据库
    private void realmInsert(Realm realm) {
        Log.d(TAG, "realmInsert: ");
        realm.beginTransaction();//必须先开启事务
        User user = realm.createObject(User.class);
        user.setId("1");
        user.setName("张三");
        realm.commitTransaction();//提交事务
        user = realm.where(User.class).findFirst();

        realm.beginTransaction();//必须先开启事务
        User user1 = realm.createObject(User.class);
        user1.setId("2");
        user1.setName("李斯");
        realm.commitTransaction();//提交事务
        user1 = realm.where(User.class).findFirst();
    }

    private boolean isTest = true;
    private int i = 0;








    /**
     * 查
     */
    private void retrieveUser() {
        //查找所有
        RealmResults<User> userList = mRealm.where(User.class).findAll();
        showUser(userList);
        if (isTest) return;
        //查找第一个
        User user2 = mRealm.where(User.class).findFirst();
        //根据条件查找
        RealmResults<User> user3 = mRealm.where(User.class).equalTo("name", "Gavin").findAll();
        //根据dog找user
        RealmResults<User> user4 = mRealm.where(User.class).equalTo("dogs.name", "二哈").findAll();
        //先找到名叫Gavin的user，再从中找出名叫二哈的user
        RealmResults<User> user5 = mRealm.where(User.class).equalTo("name", "Gavin").findAll();
        RealmResults<User> user6 = user5.where().equalTo("dogs.name", "二哈").findAll();
        //还可以这么写
        RealmResults<User> user7 = mRealm.where(User.class)
                .equalTo("name", "Gavin")
                .equalTo("dogs.name", "二哈")
                .findAll();

        //or的使用
        RealmQuery<User> query = mRealm.where(User.class);
        query.equalTo("name", "Gavin");
        query.or().equalTo("name", "Eric");
        RealmResults<User> user8 = query.findAll();
        //还可以这样
        RealmResults<User> user9 = mRealm.where(User.class)
                .equalTo("name", "Gavin")
                .or().equalTo("name", "Eric")
                .findAll();
    }

    /**
     * 更新
     */
    private void updateUser() {
        mRealm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                //先查找后得到User对象
                User user = mRealm.where(User.class).findFirst();
                Log.d(TAG, "execute: "+user.toString());
                user.setName("李磊");
                Log.d(TAG, "execute: "+user.toString());
                //user.setAge(26);
            }
        });
        retrieveUser();
    }

    private void realmUpdate(Realm realm){
//        realm.beginTransaction();//开启事务
//        User b = new User();//新建实例
//        b.setId("1");//book类新加的int属性，用@PrimaryKey注解作为主键
//        b.setName("更新的名字");
//        realm.copyToRealmOrUpdate(b);//修改操作
//        realm.commitTransaction();//提交事务


//        RLMResults *tanDogs = [Dog objectsWhere:@"color = 'tan' AND name BEGINSWITH 'B'"];
//        [User objectsWhere:@"color = 'tan' AND name BEGINSWITH 'B'"];
        RealmResults<User> userResults=mRealm.where(User.class).contains("name","李磊").findAll();//查询符合条件的所有数据

        showUser(userResults);
        for (int j = 0; j < userResults.size(); j++) {
            mRealm.beginTransaction();
            userResults.get(j).setName("李强");
            mRealm.commitTransaction();
        }
        showUser(userResults);
    }

    /**
     * 删除
     */
    private void deleteUser() {
        //先查找到数据
        final RealmResults<User> user = mRealm.where(User.class).findAll();
        mRealm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                user.get(0).deleteFromRealm();
            }
        });
        //或者
        mRealm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                user.deleteFromRealm(0);
            }
        });

    }

    private void showUser(RealmResults<User> user) {
        Toast.makeText(this, "size : " + user.size(), Toast.LENGTH_SHORT).show();
        for (User u : user) {
            Log.d(TAG, u.toString());
        }
    }

}
