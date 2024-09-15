package com.example.newtrackingappjava.Interface;

import java.util.List;

public interface IFirebaseLoadDone {
    void onFirebaseLoadUserNameDone(List<String> lstEmail, List<String> lstTransport);
    void onFirebaseLoadFailed(String message);
}
