package com.example.myapplication

import android.app.Application
import android.util.Log
import com.example.myapplication.BuildConfig // Исправлено: добавлен явный импорт сгенерированного класса
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

/**
 * Главный класс приложения, инициализирующий зависимости Hilt и логирование.
 *
 * В режиме отладки используется стандартное дерево логирования Timber.
 * В релизном режиме логи уровня VERBOSE, DEBUG и INFO игнорируются,
 * а ошибки и предупреждения отправляются в Firebase Crashlytics.
 */
@HiltAndroidApp
class MyApplication : Application() {

    /**
     * Вызывается при создании приложения.
     * Настраивает стратегию логирования в зависимости от типа сборки.
     */
    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        } else {
            Timber.plant(CrashReportingTree())
        }
    }
}

/**
 * Дерево логирования для отправки критических событий в Firebase Crashlytics.
 * Игнорирует низкоуровневые логи для экономии трафика и снижения шума в отчетах.
 */
class CrashReportingTree : Timber.Tree() {
    /**
     * Обрабатывает событие логирования.
     *
     * @param priority Приоритет сообщения (VERBOSE, DEBUG, INFO, WARN, ERROR).
     * @param tag Тег лога (может быть null).
     * @param message Текстовое сообщение.
     * @param t Связанное исключение (может быть null).
     */
    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        if (priority == Log.VERBOSE || priority == Log.DEBUG || priority == Log.INFO) return

        Firebase.crashlytics.log(message)
        if (t != null) {
            Firebase.crashlytics.recordException(t)
        }
    }
}