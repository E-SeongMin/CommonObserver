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
            val eventKeyList = event?.let { listOf(it) } ?: getEventKeyList(observer)
            if (eventKeyList.isEmpty()) { return }
                eventKeyList.forEach { add(observer, it) }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun <T : Any> unregisterObserver(observer: T, event: Class<*>? = null) {
        try {
            val eventKeys = event?.let { listOf(it) } ?: getEventKeyList(observer)
            eventKeys.forEach { remove(observer, it) }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun <T : Any> sendMessage(event: Class<T>, onSuccessAction: (T) -> Unit, onFailAction: (() -> Unit)? = null) {
        try {
            observerMap[event]?.run {
                forEach { observer -> (observer as? T)?.let(onSuccessAction) }
            } ?: onFailAction?.invoke()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun unregisterAllObservers() {
        try {
            observerMap.clear()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getEventKeyList(observer: Any): List<Class<*>> {
        return observer::class.superclasses
            .filter { it.hasAnnotation<EventKey>() }
            .map { it.java }
    }

    private fun add(observer: Any, event: Class<*>) {
        Log.d("CommonObserver", "add event : ${event.simpleName}, observer: ${observer.javaClass.simpleName}")
        observerMap.getOrPut(event) { mutableSetOf() }.add(observer)
    }

    private fun remove(observer: Any, event: Class<*>) {
        observerMap[event]?.apply {
            Log.d("CommonObserver", "remove event : ${event.simpleName}, observer: ${observer.javaClass.simpleName}")
            remove(observer)
            if (isEmpty()) observerMap.remove(event)
        }
    }
}