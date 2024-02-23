package org.opendroneid.android.app.network.service;

import org.opendroneid.android.app.network.models.drone.DroneDetectionPost;
import org.opendroneid.android.app.network.models.drone.DroneDetectionResponse;
import org.opendroneid.android.app.network.models.sensor.SensorsPostRequest;
import org.opendroneid.android.app.network.models.user.UserForgotPassword;
import org.opendroneid.android.app.network.models.user.UserForgotPasswordResponse;
import org.opendroneid.android.app.network.models.user.UserLogin;
import org.opendroneid.android.app.network.models.user.UserLoginSuccessResponse;
import org.opendroneid.android.app.network.models.user.UserRegistration;
import org.opendroneid.android.app.network.models.user.UserRegistrationResponse;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface ApiService {

    @POST("register")
    Call<UserRegistrationResponse> postUserRegister(@Body UserRegistration data);

    @POST("login")
    Call<UserLoginSuccessResponse> postUserLogin(@Body UserLogin data);

    @POST("forgotten-password")
    Call<UserForgotPasswordResponse> postUserForgotPassword(@Body UserForgotPassword data);

    @POST("user/sources")
    Call<ResponseBody> postSensor(@Header("Authorization") String bearerToken, @Body SensorsPostRequest postRequest);

    @POST("detection")
    Call<DroneDetectionResponse> postDetection(@Body DroneDetectionPost detection);

}