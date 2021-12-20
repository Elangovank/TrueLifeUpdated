package com.truelife.chat.placespicker

import  com.truelife.R
import com.truelife.chat.placespicker.model.PlacesResponse
import com.truelife.chat.utils.TimeHelper
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import com.truelife.TLApplication
import kotlinx.coroutines.Deferred
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

private val CLIENT_ID = TLApplication.context()?.getString(R.string.foursquare_client_id)
private val CLIENT_SECRET = TLApplication.context()?.getString(R.string.foursquare_client_secret)
private const val BASE_URL = "https://api.foursquare.com/v2/venues/"



interface PlacesWebService {
    @GET("search/")
    fun getNearbyPlaces(
            @Query("ll") latLng: String
    ): Deferred<PlacesResponse>


    companion object {
        operator fun invoke(
        ): PlacesWebService {
            val requestInterceptor = Interceptor { chain ->

                val url = chain.request()
                        .url()
                        .newBuilder()
                        .addQueryParameter("client_id", CLIENT_ID)
                        .addQueryParameter("client_secret", CLIENT_SECRET)
                        .addQueryParameter("v", TimeHelper.getYYYYMMDD())
                        .build()
                val request = chain.request()
                        .newBuilder()
                        .url(url)
                        .build()

                return@Interceptor chain.proceed(request)
            }

            val okHttpClient = OkHttpClient.Builder()
                    .addInterceptor(requestInterceptor)
                    .build()

            return Retrofit.Builder()
                    .client(okHttpClient)
                    .baseUrl(BASE_URL)
                    .addCallAdapterFactory(CoroutineCallAdapterFactory())
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                    .create(PlacesWebService::class.java)
        }
    }
}