package com.example.gavin.realmtest;

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

public class MainActivity1 extends AppCompatActivity implements View.OnClickListener {
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
                createdTest1();
                realmInsert(mRealm);
                break;
            case R.id.retrieve://查询
                //retrieveUser();
//                retrieveTest1();
                select();
                break;
            case R.id.update:
                updateUser();
                break;
            case R.id.delete:
                deleteUser();
                break;
        }
    }

    private void select() {
        //查询一个数据库中第一个book
        User user = mRealm.where(User.class)
                .equalTo("id","1")//相当于where name='史记'
                .or()//或，连接查询条件；没有这个方法时，默认是隐式地被逻辑和(&)组合
                .equalTo("name","司马迁")//相当于 author='司马迁'
                .findFirst();

        //整体相当于select * from (test.realm) where name='史记' or author='司马迁' limit 1;
        //Book book = realm.where(Book.class).findFirst();

        Log.d(TAG, "select: "+user.toString());
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
    }

    private boolean isTest = true;
    private int i = 0;



    private void createdTest1() {
        RealmAsyncTask transaction = mRealm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                User user = realm.createObject(User.class);
                user.setName("Michale");
                user.setId(i + "");

                Dog dog1 = realm.createObject(Dog.class);
                dog1.setAge(1);
                dog1.setName("二哈");
                user.getDogs().add(dog1);

                Dog dog2 = realm.createObject(Dog.class);
                dog2.setAge(2);
                dog2.setName("阿拉撕家");
                user.getDogs().add(dog2);
                i++;
            }
        });
        mRealm.beginTransaction();//开启事务
        User user = mRealm.createObject(User.class);
        user.setName("Gavin");
        user.setId("3");
        mRealm.commitTransaction();//提交事务
        //使用copyToRealmOrUpdate
        /*final User user = new User();
        user.setId("6");
        user.setName("Jack");
        mRealm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.copyToRealm(user);
            }
        });*/
    }


    private void retrieveTest1() {
        //User user = mRealm.where(User.class).findFirst();
        //RealmResults<User> userList = mRealm.where(User.class).findAll();
        RealmResults<User> userList = mRealm.where(User.class)
                .equalTo("name", "Gavin")
                .equalTo("dogs.name", "二哈")
                .findAllAsync();
        //Log.i(TAG, userList.toString());
        showUser(userList);
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
                //user.setAge(26);
            }
        });
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
            Log.i(TAG, u.toString());
        }
    }

}
