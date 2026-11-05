# ===========================================================================
# ProGuard/R8 правила для приложения MyApplication
# ===========================================================================

# ---------------------------------------------------------------------------
# 1. Общие правила и отладка (Crashlytics / Timber)
# ---------------------------------------------------------------------------

# Сохраняем информацию о номерах строк и именах исходных файлов.
# Это критически важно для корректного чтения стек-трейсов в Firebase Crashlytics
# и логах Timber в Release-сборках.

-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile


# Сохраняем сигнатуры (Generics) и информацию о внутренних классах.
# Критически важно для Retrofit (чтобы знать точный тип возврата, например, Archive<Event>)
# и Kotlinx Serialization.

-keepattributes Signature, InnerClasses, EnclosingMethod


# Сохраняем аннотации во время выполнения.
# Необходимо для корректной работы Dagger/Hilt и Retrofit.

-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
-keepattributes AnnotationDefault

# ---------------------------------------------------------------------------
# 2. Kotlinx Serialization
# ---------------------------------------------------------------------------

# Kotlinx Serialization использует рефлексию для поиска Companion объектов 
# и сгенерированных классов $$serializer. Если R8 их удалит или переименует,
# приложение упадет с JsonDecodingException / SerializationException.


# Сохраняем базовые классы и аннотации сериализации
-keep class kotlinx.serialization.** { *; }
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Сохраняем сгенерированные плагином классы $$serializer для доменных моделей и DTO
-keep,includedescriptorclasses class com.example.**$$serializer { *; }

# Сохраняем Companion объекты в моделях домена (domain) и данных (data)
-keepclassmembers class com.example.domain.model.** {
    *** Companion;
}
-keepclasseswithmembers class com.example.domain.model.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keepclassmembers class com.example.data.** {
    *** Companion;
}
-keepclasseswithmembers class com.example.data.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# ---------------------------------------------------------------------------
# 3. Retrofit 2 и OkHttp
# ---------------------------------------------------------------------------

# Retrofit создает реализации интерфейсов API через динамические Proxy-классы (Reflection).
# R8 не должен удалять методы, помеченные аннотациями @GET, @POST и т.д., 
# а также должен сохранять типы параметров и возвращаемых значений.


-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.annotation.**
-dontwarn kotlin.Unit

-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }

# Сохраняем методы в интерфейсах API, помеченные аннотациями Retrofit
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}

# Игнорируем предупреждения о Kotlin Extensions в Retrofit, если они не используются напрямую
-dontwarn retrofit2.KotlinExtensions
-dontwarn retrofit2.KotlinExtensions$*

# ---------------------------------------------------------------------------
# 4. Kotlin Coroutines
# ---------------------------------------------------------------------------

# Корутины используют специфические механизмы для обработки исключений и диспетчеров.
# Эти правила предотвращают удаление внутренних классов, необходимых для работы Flow и suspend-функций.
-dontwarn kotlinx.coroutines.**
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}

# ---------------------------------------------------------------------------
# 5. Kotlin Standard Library & Reflection
# ---------------------------------------------------------------------------

# Сохраняем метаданные Kotlin, необходимые для работы некоторых библиотек через рефлексию
-keep class kotlin.Metadata { *; }
-dontwarn kotlin.reflect.jvm.internal.**

# ---------------------------------------------------------------------------
# 6. Hilt / Dagger (Fallback правила)
# ---------------------------------------------------------------------------

# Обычно Hilt поставляет свои правила через consumer-rules.pro, 
# но для гарантии стабильности Release-сборки дублируем базовые сохранения.
-dontwarn dagger.**
-keep class dagger.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper { *; }