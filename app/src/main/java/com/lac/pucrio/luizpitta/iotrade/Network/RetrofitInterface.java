package com.lac.pucrio.luizpitta.iotrade.Network;

import com.lac.pucrio.luizpitta.iotrade.Models.ConnectPrice;
import com.lac.pucrio.luizpitta.iotrade.Models.ObjectServer;
import com.lac.pucrio.luizpitta.iotrade.Models.Response;
import com.lac.pucrio.luizpitta.iotrade.Models.SensorPrice;
import com.lac.pucrio.luizpitta.iotrade.Models.ServiceIoT;
import com.lac.pucrio.luizpitta.iotrade.Models.User;

import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import rx.Observable;

/**
 * Functions to communicate with Server using POST and GET
 * @author Luiz Guilherme Pitta
 */
public interface RetrofitInterface {

    /**
     * @return Returns the sensor information.
     */
    @GET("get_sensor_price_information/")
    Observable<Response> getSensorPrice();

    /**
     * @return Returns the connection provider information.
     */
    @POST("get_connect_price_information/")
    Observable<Response> getConnectPrice(@Body ConnectPrice connectPrice);

    /**
     * @return Returns user information
     */
    @GET("get_user/")
    Observable<Response> getUser();

    /**
     * @return Returns the service information.
     */
    @POST("get_services_information/")
    Observable<Response> getServices(@Body ServiceIoT serviceIoT);

    /**
     * Updates User Budget
     */
    @POST("update_user_budget/")
    Observable<Response> updateUserBudget(@Body SensorPrice sensorPrice);

    /**
     * Updates Actuator Information
     */
    @POST("set_actuator_state/")
    Observable<Response> setActuatorState(@Body SensorPrice sensorPrice);

    /**
     * Updates Sensor Rating
     */
    @POST("update_sensor_rating/")
    Observable<Response> updateSensorRating(@Body Response response);

    /**
     * Updates Sensor Information
     */
    @POST("update_sensor_information/")
    Observable<Response> updateSensorInformation(@Body SensorPrice sensorPrice);

    /**
     * @return Returns the services without analytics of the matchmaking algorithm.
     */
    @POST("get_sensor_matchmaking/")
    Observable<Response> getSensorAlgorithm(@Body ObjectServer objectServer);

    /**
     * @return Returns the services without analytics of the matchmaking algorithm.
     */
    @POST("get_actuator_matchmaking/")
    Observable<Response> getNewConnectionActuator(@Body ObjectServer objectServer);

    /**
     * @return Returns the services with analytics of the matchmaking algorithm.
     */
    @POST("get_sensor_matchmaking_analytics/")
    Observable<Response> getSensorAlgorithmAnalytics(@Body ObjectServer objectServer);

    /**
     * @return Returns an analytics provider using a matchmaking algorithm.
     */
    @POST("get_new_analytics/")
    Observable<Response> getNewAnalytics(@Body ObjectServer objectServer);

    /**
     * Updates Connectivity Provider information.
     */
    @POST("register_location")
    Observable<Response> setLocationMobileHub(@Body User user);

    /**
     * Updates Analytics Provider information.
     */
    @POST("register_analytics")
    Observable<Response> setAnalyticsMobileHub(@Body User user);
}





