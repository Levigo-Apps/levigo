package org.getcarebase.carebase.api;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class AccessGUDIDAPIService {
    private final static Retrofit retrofit = new Retrofit.Builder()
            .baseUrl("https://accessgudid.nlm.nih.gov/api/v2/")
            .addConverterFactory(GsonConverterFactory.create())
            .build();


    public static <S> S createService(Class<S> serviceClass) {
        return retrofit.create(serviceClass);
    }
}
