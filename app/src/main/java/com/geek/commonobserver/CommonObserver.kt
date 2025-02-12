package com.geek.commonobserver

import android.util.Log
import com.geek.commonobserver.event.EventKey
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.full.superclasses

object CommonObserver {

    private val observerMap = mutableMapOf<Class<*>, MutableSet<Any>>()

    fun <T : Any> registerObserver(observer: T, event: Class<T>? = null) {
        try {
            val eventKeys = event?.let { listOf(it) } ?: getEventKeys(observer)
            eventKeys.forEach { addObserver(observer, it) }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun <T : Any> unregisterObserver(observer: T, event: Class<T>? = null) {
        try {
            val eventKeys = event?.let { listOf(it) } ?: getEventKeys(observer)
            eventKeys.forEach { removeObserver(observer, it) }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun <T : Any> sendMessage(event: Class<T>, onSuccessAction: (T) -> Unit, onFailAction: (() -> Unit)? = null) {
        if (observerMap.contains(event)) {
            observerMap[event]?.let { observerSet ->
                observerSet.forEach { observer ->
                    val currentObserver = observer as? T
                    currentObserver?.let(onSuccessAction)
                }
            } ?: {
                onFailAction?.invoke()
            }
        } else {
            onFailAction?.invoke()
        }
    }

    fun unregisterAllObservers() {
        try {
            observerMap.clear()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getEventKeys(observer: Any): List<Class<*>> {
        return observer::class.superclasses
            .filter { it.hasAnnotation<EventKey>() }
            .map { it.java }
    }

    private fun addObserver(observer: Any, event: Class<*>) {
        Log.d("CommonObserver", "add event : ${event.simpleName}, observer: ${observer.javaClass.simpleName}")
        observerMap.getOrPut(event) { mutableSetOf() }.add(observer)
    }

    private fun removeObserver(observer: Any, event: Class<*>) {
        observerMap[event]?.apply {
            Log.d("CommonObserver", "remove event : ${event.simpleName}, observer: ${observer.javaClass.simpleName}")
            remove(observer)
            if (isEmpty()) observerMap.remove(event)
        }
    }
}