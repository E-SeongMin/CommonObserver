package com.geek.commonobserver

import android.util.Log
import com.geek.commonobserver.event.EventKey
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.full.superclasses

object CommonObserver {

    private val observerMap = ConcurrentHashMap<Class<*>, MutableSet<Any>>()

    fun <T : Any> registerObserver(observer: T, event: Class<*>? = null) {
        try {
            processObserver(event, observer) { add(it, observer) }
        } catch (e: Exception) {
            Log.e("CommonObserver", "registerObserver fail : ${e.message}", e)
        }
    }

    fun <T : Any> unregisterObserver(observer: T, event: Class<*>? = null) {
        try {
            processObserver(event, observer) { remove(it, observer) }
        } catch (e: Exception) {
            Log.e("CommonObserver", "unregisterObserver fail : ${e.message}", e)
        }
    }

    fun <T : Any> sendMessage(event: Class<T>, onSuccessAction: (T) -> Unit, onFailAction: (() -> Unit)? = null) {
        try {
            observerMap[event]?.forEach { observer ->
                (observer as? T)?.let(onSuccessAction)
            } ?: onFailAction?.invoke()
        } catch (e: Exception) {
            Log.e("CommonObserver", "sendMessage fail : ${e.message}", e)
        }
    }

    fun unregisterAllObservers() {
        try {
            observerMap.clear()
        } catch (e: Exception) {
            Log.e("CommonObserver", "unregisterAllObservers fail : ${e.message}", e)
        }
    }

    private fun getEventKeyList(observer: Any): List<Class<*>> {
        return observer::class.superclasses
            .filter { it.hasAnnotation<EventKey>() }
            .map { it.java }
    }

    private fun add(event: Class<*>, observer: Any) {
        Log.d("CommonObserver", "add event: ${event.simpleName}, observer: ${observer.javaClass.simpleName}")
        observerMap.getOrPut(event) { mutableSetOf() }.add(observer)
    }

    private fun remove(event: Class<*>, observer: Any) {
        observerMap[event]?.apply {
            Log.d("CommonObserver", "remove event: ${event.simpleName}, observer: ${observer.javaClass.simpleName}")
            remove(observer)
            if (isEmpty()) observerMap.remove(event)
        }
    }

    private fun <T : Any> processObserver(event: Class<*>?, observer: T, action: (Class<*>) -> Unit) {
        if (event?.isAnnotationPresent(EventKey::class.java) == false) return

        event?.let(action) ?: getEventKeyList(observer).takeIf { it.isNotEmpty() }?.forEach(action)
    }
}