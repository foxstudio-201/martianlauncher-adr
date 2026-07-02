package com.foxstudio.martianlauncher.utils.network

import android.content.Context
import com.foxstudio.martianlauncher.R
import io.ktor.client.plugins.ResponseException
import io.ktor.http.HttpStatusCode

fun ResponseException.toLocal(): Pair<Int, Array<Any>> {
    val statusCode = response.status
    val codeString = statusCode.value.toString()
    val textRes = when (statusCode) {
        HttpStatusCode.BadRequest -> R.string.error_bad_request
        HttpStatusCode.Unauthorized -> R.string.error_unauthorized
        HttpStatusCode.Forbidden -> R.string.error_forbidden
        HttpStatusCode.NotFound -> R.string.error_notfound
        HttpStatusCode.NotAcceptable -> R.string.error_not_acceptable
        HttpStatusCode.RequestTimeout -> R.string.error_request_timeout
        HttpStatusCode.Conflict -> R.string.error_conflict
        HttpStatusCode.Gone -> R.string.error_gone
        HttpStatusCode.TooManyRequests -> R.string.error_too_many_requests
        HttpStatusCode.InternalServerError -> R.string.error_internal_server_error
        HttpStatusCode.BadGateway -> R.string.error_bad_gateway
        HttpStatusCode.ServiceUnavailable -> R.string.error_service_unavailable
        HttpStatusCode.GatewayTimeout -> R.string.error_gateway_timeout
        else -> return Pair(R.string.empty_holder, arrayOf("($codeString) ${statusCode.description}"))
    }
    return Pair(textRes, arrayOf(codeString))
}

fun ResponseException.toLocal(context: Context): String {
    val localRes = toLocal()
    return context.getString(localRes.first, localRes.second)
}

