package com.example.newtrackingappjava.Remote;

import com.example.newtrackingappjava.Model.MyResponse;
import com.example.newtrackingappjava.Model.Request;
import com.google.firebase.messaging.FcmBroadcastProcessor;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface IFCMService {
    @Headers({
            "Content-Type:application/json",
            "Authorization:key=AAAAGsKwKuY:APA91bHmkAAcNoQpexgSHUD1OUG-nzoSUlHkSx3EzNhKUV-wLmTVbgtkGjypvONejYNQOwOXxcOdyVBGqa8HUl27E9x180j6_JZ2imzj3SBpA-kJMur2g_04l61YXThClxLm9h6-x_0s"

    })
    @POST("fcm/send")
    Observable<MyResponse> sendFriendRequestToUser(@Body Request body);
}
