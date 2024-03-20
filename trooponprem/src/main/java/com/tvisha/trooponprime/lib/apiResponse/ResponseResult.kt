package com.iq.atoms.data.base

/**
 * A generic class that holds a value with its loading status.
 * @param <T>
 */
sealed class ResponseResult<out T : Any> {

    data class Success<out T : Any>(val data: T) : ResponseResult<T>()
    data class Timeout(val timeoutMsg: String) : ResponseResult<Nothing>(){
        override fun toString(): String {
            return timeoutMsg
        }
    }
    data class Error(val message: String) : ResponseResult<Nothing>() {
        override fun toString(): String {
            return message
        }
    }

    override fun toString(): String {
        return when (this) {
            is Success<*> -> "Success[data=$data]"
            is Timeout -> "Timeout: $timeoutMsg"
            is Error -> message
        }
    }
}