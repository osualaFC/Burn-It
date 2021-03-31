package com.example.burn_it.di

import android.content.Context
import android.content.SharedPreferences
import androidx.room.Room
import com.example.burn_it.api.WeatherService
import com.example.burn_it.db.RunDatabase
import com.example.burn_it.utils.Constants.BASE_URL
import com.example.burn_it.utils.Constants.KEY_FIRST_TIME_TOGGLE
import com.example.burn_it.utils.Constants.KEY_NAME
import com.example.burn_it.utils.Constants.KEY_WEIGHT
import com.example.burn_it.utils.Constants.PERCENTAGE
import com.example.burn_it.utils.Constants.POSITION
import com.example.burn_it.utils.Constants.RUNNING_DATABASE_NAME
import com.example.burn_it.utils.Constants.SHARED_PREFERENCES_NAME
import com.example.burn_it.utils.Constants.TARGET
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Named
import javax.inject.Singleton


@Module
@InstallIn(ApplicationComponent::class)
object AppModule {
    @Singleton
    @Provides
    fun provideRunDatabase(
        @ApplicationContext app: Context
    ) = Room.databaseBuilder(
        app,
        RunDatabase::class.java,
        RUNNING_DATABASE_NAME
    ).build()

    @Singleton
    @Provides
    fun provideRunDao(db: RunDatabase) = db.getRunDao()

    @Singleton
    @Provides
    fun provideSharedPreferences(@ApplicationContext app: Context) =
        app.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)

    @Singleton
    @Provides
    fun provideName(sharedPref: SharedPreferences) = sharedPref.getString(KEY_NAME, "") ?: ""

    @Singleton
    @Provides
    fun provideWeight(sharedPref: SharedPreferences) = sharedPref.getFloat(KEY_WEIGHT, 80f)

    @Singleton
    @Provides
    fun provideFirstTimeToggle(sharedPref: SharedPreferences) =
        sharedPref.getBoolean(KEY_FIRST_TIME_TOGGLE, true)

    @Singleton
    @Provides
    @Named("target")
    fun provideTarget(sharedPref: SharedPreferences) = sharedPref.getFloat(TARGET, 1f)

    @Singleton
    @Provides
    @Named("position")
    fun providePosition(sharedPref: SharedPreferences) = sharedPref.getFloat(POSITION, 0F)

    @Singleton
    @Provides
    @Named("percentage")
    fun providePercentage(sharedPref: SharedPreferences) = sharedPref.getFloat(PERCENTAGE, 0F)

    @Provides
    @Singleton
    fun provideLogger(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
    }


    @Provides
    @Singleton
    fun providesConverterFactory(): Converter.Factory {
        return GsonConverterFactory.create()
    }


    @Provides
    @Singleton
    fun provideClient(logger: HttpLoggingInterceptor): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(logger)
            .build()
    }

    @Provides
    @Singleton
    fun provideService(client: OkHttpClient, converterFactory: Converter.Factory): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(converterFactory)
            .build()
    }

    @Provides
    @Singleton
    fun provideWeatherService(retrofit: Retrofit): WeatherService {
        return retrofit.create(WeatherService::class.java)
    }

}