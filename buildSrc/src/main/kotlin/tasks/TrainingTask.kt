package tasks

import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.logging.HttpLoggingInterceptor
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

open class SlackTask : DefaultTask() {
    lateinit var messageText: String
    lateinit var webhookUrl: String

    @TaskAction
    fun sendMessage() {
        val logger = HttpLoggingInterceptor(HttpLoggingInterceptor.Logger { m -> logger.debug(m) })
        logger.level = HttpLoggingInterceptor.Level.BODY
        val okhttp = OkHttpClient.Builder().addInterceptor(logger).build()
        val body = RequestBody.create(MediaType.parse("application/json"), "{\"text\":\"${messageText.replace("\"", "\\\"")}\"}")
        val request = Request.Builder()
            .url(webhookUrl)
            .post(body)
            .build()
        okhttp.newCall(request).execute()
    }
}