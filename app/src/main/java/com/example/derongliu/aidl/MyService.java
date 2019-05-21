package com.example.derongliu.aidl;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

import com.example.derongliu.opengltest.MainActivity;
import com.example.derongliu.opengltest.R;

import java.util.ArrayList;
import java.util.List;

public class MyService extends Service {

    private static final String PACKAGE_SAY_HI = "com.aidl.test";
    private List<Student> students = new ArrayList<>();
    private boolean canRun = true;


    private final IMyService.Stub binder = new IMyService.Stub() {
        @Override
        public List<Student> getStudent() throws RemoteException {
            synchronized (students) {
                return students;
            }
        }

        @Override
        public void addStudent(Student student) throws RemoteException {
            synchronized (student) {
                if (!students.contains(student)) {
                    students.add(student);
                }
            }
        }

        @Override
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
          /*  String packageName = null;
            String[] packages = MyService.this.getPackageManager().
                    getPackagesForUid(getCallingUid());
            if (packages != null && packages.length > 0) {
                packageName = packages[0];
            }
            if (!PACKAGE_SAY_HI.equals(packageName)) {
                return false;
            }*/
            return super.onTransact(code, data, reply, flags);
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        new Thread(null, new ServiceWorker(), "BackgroundService").start();
        synchronized (students) {
            for (int i = 1; i < 6; i++) {
                Student student = new Student();
                student.name = "student#" + i;
                student.age = i * 5;
                student.sex = i % 2 == 0 ? "男" : "女";
                student.sno = i;
                students.add(student);
            }
        }

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.createNotificationChannel(new NotificationChannel("aidl", "aidl", NotificationManager.IMPORTANCE_HIGH));
        }
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, MainActivity.class), 0);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, "aidl")
                .setContentTitle("服务已启动")
                .setContentText("正在通信中")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setOngoing(true)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(contentIntent);
        Notification notification = notificationBuilder.build();
        notificationManager.notify(100, notification);
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        canRun = false;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }


    class ServiceWorker implements Runnable {
        long counter = 0;

        @Override
        public void run() {
            // do background processing here.....
            while (canRun) {
                counter++;
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
