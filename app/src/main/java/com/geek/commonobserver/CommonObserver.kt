package com.geek.commonobserver

object CommonObserver {

    private val observers = mutableMapOf<Class<*>, Any>()

    fun <T : Any> registerObserver(event: Class<T>, observer: T) {
        if (!observers.contains(event)) {
            observers[event] = observer
        }
    }

    fun <T : Any> unregisterObserver(event: Class<T>) {
        if (observers.contains(event)) {
            observers.remove(event)
        }
    }

    fun <T : Any> sendMessage(event: Class<T>, action: (T) -> Unit) {
        if (observers.contains(event)) {
            try {
                val currentObserver = observers[event] as? T
                currentObserver?.let(action)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun unregisterAllObserver() {
        observers.clear()
    }
}