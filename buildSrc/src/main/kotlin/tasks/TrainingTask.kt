package tasks

import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.logging.HttpLoggingInterceptor
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

open class TrainingTask : DefaultTask() {
    lateinit var messageText: String
    lateinit var webhookUrl: String

    @TaskAction
    fun sendMessage() {
        println(messageText)
    }
}