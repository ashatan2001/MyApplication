plugins {
    id("com.android.application") version "8.9.0" apply false
    id("com.android.library") version "8.9.0" apply false

    /** Версия Kotlin обновлена до 2.2.20 для поддержки метаданных 2.2.0 */
    id("org.jetbrains.kotlin.android") version "2.2.20" apply false
    id("org.jetbrains.kotlin.jvm") version "2.2.20" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.2.20" apply false
    id("org.jetbrains.kotlin.plugin.serialization") version "2.2.20" apply false
    id("com.google.dagger.hilt.android") version "2.58" apply false

    /** Версия KSP должна строго соответствовать выбранной версии Kotlin */
    id("com.google.devtools.ksp") version "2.2.20-2.0.4" apply false
}
