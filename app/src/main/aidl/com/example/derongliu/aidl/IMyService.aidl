package com.example.derongliu.aidl;

import com.example.derongliu.aidl.Student;

interface IMyService {

    List<Student> getStudent();

    void addStudent(in Student student);
}
