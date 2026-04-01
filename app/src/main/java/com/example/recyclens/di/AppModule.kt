package com.example.recyclens.di

import android.content.Context
import android.content.SharedPreferences

import com.example.recyclens.data.services.ApiService
import com.example.recyclens.data.repository.AuthRepositoryImpl
import com.example.recyclens.data.repository.ProfileRepositoryImpl
import com.example.recyclens.data.repository.ReportRepositoryImpl
import com.example.recyclens.domain.repository.AuthRepository
import com.example.recyclens.domain.repository.ProfileRepository
import com.example.recyclens.domain.repository.ReportRepository
import com.example.recyclens.domain.use_cases.auth.CheckAuthStatusUseCase
import com.example.recyclens.domain.use_cases.auth.GetLoggedInUserUseCase

import com.example.recyclens.domain.use_cases.auth.LogoutUseCase
import com.example.recyclens.domain.use_cases.auth.RequestOtpUseCase
import com.example.recyclens.domain.use_cases.auth.UpdateFcmTokenUseCase
import com.example.recyclens.domain.use_cases.auth.VerifyOtpUseCase
import com.example.recyclens.domain.use_cases.profile.GetUserProfileUseCase
import com.example.recyclens.domain.use_cases.profile.UpdateProfileUseCase
import com.example.recyclens.domain.use_cases.report.AcceptReportUseCase
import com.example.recyclens.domain.use_cases.report.CreateReportUseCase
import com.example.recyclens.domain.use_cases.report.GetMyReportsUseCase
import com.example.recyclens.domain.use_cases.report.GetReportDetailsUseCase
import com.example.recyclens.domain.use_cases.report.GetReportStatsUseCase
import com.example.recyclens.domain.use_cases.report.GetWasteReportsUseCase
import com.example.recyclens.domain.use_cases.report.MarkAsCleanedUseCase
import com.example.recyclens.domain.use_cases.report.VerifyImageUseCase
import com.example.recyclens.presentation.auth.AuthViewModel
import com.example.recyclens.presentation.home.HomeScreenViewModel
import com.example.recyclens.presentation.home.WorkerHomeViewModel
import com.example.recyclens.presentation.profile.CreateProfileViewModel
import com.example.recyclens.presentation.report.MyReportsViewModel
import com.example.recyclens.presentation.report.ReportDetailViewModel
import com.example.recyclens.presentation.report.ReportWasteViewModel
import com.example.recyclens.presentation.splash.SplashViewModel

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

private const val BASE_URL = "http://172.27.77.174:3000"
val appModule = module {

    single {
        // For logging network requests and responses
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    single<Retrofit> {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(get<OkHttpClient>())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    single<ApiService> {
        get<Retrofit>().create(ApiService::class.java)
    }

    single<AuthRepository> { AuthRepositoryImpl(get(), get()) }
    single<ProfileRepository> { ProfileRepositoryImpl(get(), get(), androidContext()) }
    single<ReportRepository> { ReportRepositoryImpl(get(), get(), androidContext()) }

    factory { RequestOtpUseCase(get()) }
    factory { VerifyOtpUseCase(get()) }
    factory { LogoutUseCase(get()) }
    factory { CheckAuthStatusUseCase(get()) }
    factory { UpdateFcmTokenUseCase(get()) }
    factory{ UpdateProfileUseCase(get()) }
    factory { GetUserProfileUseCase(get()) }
    factory { VerifyImageUseCase(get()) }
    factory { CreateReportUseCase(get()) }
    factory { GetReportStatsUseCase(get()) }
    factory { GetWasteReportsUseCase(get()) }
    factory { GetLoggedInUserUseCase(get()) }
    factory { AcceptReportUseCase(get()) }
    factory { GetReportDetailsUseCase(get()) }
    factory { MarkAsCleanedUseCase(get()) }
    factory { GetMyReportsUseCase(get()) }

    viewModel { AuthViewModel(get(), get(), get()) }
    viewModel { SplashViewModel(get()) }
    viewModel { CreateProfileViewModel(get()) }
    viewModel { HomeScreenViewModel(get(), get(), get(), get()) }
    viewModel { ReportWasteViewModel(get(), get()) }
    viewModel { WorkerHomeViewModel(get(), get()) }
    viewModel { ReportDetailViewModel(get(), get(), get(), get()) }
    viewModel { MyReportsViewModel(get()) }
    single<SharedPreferences> {
        androidContext().getSharedPreferences("CleanCityPrefs", Context.MODE_PRIVATE)
    }

}