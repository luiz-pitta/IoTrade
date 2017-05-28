package com.lac.pucrio.luizpitta.iotrade.Network;

import com.lac.pucrio.luizpitta.iotrade.Models.FilterServer;
import com.lac.pucrio.luizpitta.iotrade.Models.Response;
import com.lac.pucrio.luizpitta.iotrade.Models.SensorPrice;
import com.lac.pucrio.luizpitta.iotrade.Models.ServiceIoT;

import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import rx.Observable;

public interface RetrofitInterface {

    @GET("get_sensor_price_information/")
    Observable<Response> getSensorPrice();

    @GET("get_user/")
    Observable<Response> getUser();

    @POST("get_services_information/")
    Observable<Response> getServices(@Body ServiceIoT serviceIoT);

    @POST("get_services_information_filter/")
    Observable<Response> getServicesFilter(@Body FilterServer filterServer);

    @POST("update_user_budget/")
    Observable<Response> updateUserBudget(@Body SensorPrice sensorPrice);

    @POST("update_sensor_rating/")
    Observable<Response> updateSensorRating(@Body SensorPrice sensorPrice);

    @POST("update_sensor_information/")
    Observable<Response> updateSensorInformation(@Body SensorPrice sensorPrice);

    @POST("get_sensor_matchmaking/")
    Observable<Response> getSensorAlgorithm(@Body FilterServer filterServer);

    /*
    @POST("import_from_google")
    Observable<Response> registerActivityGoogle(@Body ArrayList<ServiceIoT> activitiesServer);

    @POST("set_notification/")
    Observable<User> setNotificationUser(@Body User user);

    @GET("appInfo")
    Observable<ArrayList<AppInfoServer>> getAppInfo();

    @POST("get_flag2/{id}")
    Observable<Response> getFlag2(@Path("id") long id, @Body FlagServer flagServer);

    @POST("get_act2/{id}")
    Observable<Response> getActivity2(@Path("id") long id, @Body ServiceIoT activityServer);

    @GET("get_contact_us/")
    Observable<Response> getContactUs();

    @GET("get_blocked_users/{email}")
    Observable<ArrayList<User>> getBlockedUsers(@Path("email") String email);

    @GET("get_friends_request/{email}")
    Observable<Response> getFriendRequest(@Path("email") String email);

    @POST("get_past_activities/{email}")
    Observable<ArrayList<ServiceIoT>> getPastActivities(@Path("email") String email, @Body DateTymo date);

    @GET("users/{email}")
    Observable<User> getProfile(@Path("email") String email);

    @POST("edit_activity_repeat_single/{id}")
    Observable<Response> editActivityRepeatSingle(@Path("id") long id, @Body ServiceIoT activityServer);*/

}
