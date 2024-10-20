package com.example.myapplication.network;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class ApiClinet {
    private static Retrofit retrofit=null;
    public static Retrofit getClient(){
        if (retrofit==null){
            retrofit=new Retrofit.Builder().baseUrl("https://fcm.googleapis.com/fmc/").addConverterFactory(ScalarsConverterFactory.create()).build();

        }
        return  retrofit;
    }
}
