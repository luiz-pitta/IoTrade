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
 * Classe responsável pelas chamadas ao servidor através dos cabeçalhos definidos (@GET e @POST)
 *
 * @author Luiz Guilherme Pitta
 */
public interface RetrofitInterface {

    /**
     * @return Retorna as informações dos sensores.
     */
    @GET("get_sensor_price_information/")
    Observable<Response> getSensorPrice();

    /**
     * @return Retorna as informações dos sensores.
     */
    @POST("get_connect_price_information/")
    Observable<Response> getConnectPrice(@Body ConnectPrice connectPrice);

    /**
     * @return Retorna o usuário do servidor.
     */
    @GET("get_user/")
    Observable<Response> getUser();

    /**
     * @return Retorna as informações dos serviços.
     */
    @POST("get_services_information/")
    Observable<Response> getServices(@Body ServiceIoT serviceIoT);

    /**
     * @return Retorna uma mensagem que tudo ocorreu certo na atualização dos dados.
     */
    @POST("update_user_budget/")
    Observable<Response> updateUserBudget(@Body SensorPrice sensorPrice);

    /**
     * @return Retorna uma mensagem que tudo ocorreu certo na atualização dos dados.
     */
    @POST("set_actuator_state/")
    Observable<Response> setActuatorState(@Body SensorPrice sensorPrice);

    /**
     * @return Retorna uma mensagem que tudo ocorreu certo na atualização dos dados.
     */
    @POST("update_sensor_rating/")
    Observable<Response> updateSensorRating(@Body Response response);

    /**
     * @return Retorna uma mensagem que tudo ocorreu certo na atualização dos dados.
     */
    @POST("update_sensor_information/")
    Observable<Response> updateSensorInformation(@Body SensorPrice sensorPrice);

    /**
     * @return Retorna os serviços sem analytics do algoritmo de matchmaking.
     */
    @POST("get_sensor_matchmaking/")
    Observable<Response> getSensorAlgorithm(@Body ObjectServer objectServer);

    /**
     * @return Retorna os serviços com analytics do algoritmo de matchmaking.
     */
    @POST("get_sensor_matchmaking_analytics/")
    Observable<Response> getSensorAlgorithmAnalytics(@Body ObjectServer objectServer);

    /**
     * @return Retorna os serviços com analytics do algoritmo de matchmaking.
     */
    @POST("get_new_analytics/")
    Observable<Response> getNewAnalytics(@Body ObjectServer objectServer);

    @POST("register_location")
    Observable<Response> setLocationMobileHub(@Body User user);

    @POST("register_analytics")
    Observable<Response> setAnalyticsMobileHub(@Body User user);
}
