package com.example.derongliu.aidl;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

public final class Student implements Parcelable {

    public int sno;
    public String name;
    public String sex;
    public int age;

    public Student() {
    }

    public Student(Parcel in) {
        readFromParcel(in);
    }

    public static final Parcelable.Creator<Student> CREATOR = new Parcelable.Creator<Student>() {
        @Override
        public Student createFromParcel(Parcel source) {
            return new Student(source);
        }

        @Override
        public Student[] newArray(int size) {
            return new Student[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(sno);
        dest.writeString(name);
        dest.writeString(sex);
        dest.writeInt(age);
    }

    public void readFromParcel(Parcel in) {
        sno = in.readInt();
        name = in.readString();
        sex = in.readString();
        age = in.readInt();
    }

    @NonNull
    @Override
    public String toString() {
        return "num:" + sno + "\n"
                + "name:" + name + "\n"
                + "age:" + age + "\n"
                + "sex:" + sex + "\n";
    }
}
