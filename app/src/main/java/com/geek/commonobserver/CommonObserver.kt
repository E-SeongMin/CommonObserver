package com.geek.commonobserver

import android.util.Log
import com.geek.commonobserver.event.EventKey
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.full.superclasses

object CommonObserver {
    private val observerMap = ConcurrentHashMap<Class<*>, MutableSet<Any>>()
    /** * Method to register an observer. If you need to add a specific event, just enter the event parameter. */
    fun <T : Any> registerObserver(observer: T, event: Class<*>? = null) {
        try {
            if (event?.isAnnotationPresent(EventKey::class.java) == false) {
                Log.d("CommonObserver", "CommonObserver registerObserver fail : ${event.simpleName} does not have EventKey annotation")
                return
            }

            event?.let {
                add(observer, it)
            } ?: run {
                getEventKeyList(observer).takeIf { it.isNotEmpty() }?.forEach {
                    add(observer, it)
                }
            }
        } catch (e: Exception) {
            Log.e("CommonObserver", "CommonObserver registerObserver fail : ${e.message}")
        }
    }

    /** * Method to unregister an observer. If you need to remove a specific event, just enter the event parameter. */
    fun <T : Any> unregisterObserver(observer: T, event: Class<*>? = null) {
        try {
            if (event?.isAnnotationPresent(EventKey::class.java) == false) {
                Log.d("CommonObserver", "CommonObserver unregisterObserver fail : ${event.simpleName} does not have EventKey annotation")
                return
            }

            event?.let {
                remove(observer, it)
            } ?: run {
                getEventKeyList(observer).takeIf { it.isNotEmpty() }?.forEach {
                    remove(observer, it)
                }
            }
        } catch (e: Exception) {
            Log.e("CommonObserver", "CommonObserver unregisterObserver fail : ${e.message}")
        }
    }

    /** * Method to send callback. */
    fun <T : Any> sendMessage(event: Class<T>, onSuccessAction: (T) -> Unit, onFailAction: (() -> Unit)? = null) {
        try {
            observerMap[event]?.run {
                forEach { observer ->
                    (observer as? T)?.let(onSuccessAction)
                }
            } ?: onFailAction?.invoke()
        } catch (e: Exception) {
            Log.e("CommonObserver", "CommonObserver sendMessage fail : ${e.message}")
        }
    }

    /** * Method to remove all observers. */
    fun unregisterAllObserver() {
        try {
            Log.d("CommonObserver", "CommonObserver unregisterAllObserver")
            observerMap.clear ()
        } catch (e: Exception) {
            Log.e("CommonObserver", "CommonObserver unregisterAllObserver fail : ${e.message}")
        }
    }

    private fun getEventKeyList(observer: Any): List<Class<*>> {
        return observer::class.superclasses.mapNotNull { it.takeIf { it.hasAnnotation<EventKey>() }?.java }
    }

    private fun add(observer: Any, event: Class<*>) {
        Log.d("CommonObserver", "CommonObserver add event : ${event.simpleName}, observer : ${observer.javaClass.simpleName}")
        observerMap.getOrPut (event) { mutableSetOf() }.add(observer)
    }

    private fun remove(observer: Any, event: Class<*>) {
        observerMap[event]?.apply {
            Log.d("CommonObserver", "CommonObserver remove event : ${event.simpleName}, observer : ${observer.javaClass.simpleName}")
            remove(observer)
            if (isEmpty()) {
                observerMap.remove(event)
            }
        }
    }
}