package org.opendroneid.android.app.network.service;

import org.opendroneid.android.app.network.models.user.UserLogin;
import org.opendroneid.android.app.network.models.user.UserLoginResponse;
import org.opendroneid.android.app.network.models.user.UserRegistrationResponse;
import org.opendroneid.android.app.network.models.user.UserRegistration;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
public interface ApiService {

    @POST("register")
    Call<UserRegistrationResponse>  postUserRegister(@Body UserRegistration data);

    @POST("login")
    Call<UserLoginResponse>  postUserLogin(@Body UserLogin data);
}