package com.amplifyframework.statemachine.codegen.data

import android.content.Context
import com.amplifyframework.core.store.EncryptedKeyValueRepository
import com.amplifyframework.statemachine.codegen.states.AuthState
import com.amplifyframework.statemachine.util.LifoMap
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Repository for managing authentication states.
 * This class uses an in-memory LIFO map and an encrypted key-value store to persist authentication states.
 *
 * @constructor Creates an instance of AuthStateRepo with the provided context.
 * @param context The context used to initialize the encrypted key-value store.
 */
internal class AuthStateRepo private constructor(context: Context) {

    // In-memory LIFO map to store authentication states.
    private val authStateMap = LifoMap.empty<String, AuthState>()

    // Encrypted key-value store for persisting authentication states.
    private val encryptedStore = EncryptedKeyValueRepository(
        context,
        PREF_KEY
    )

    /**
     * Stores the given authentication state associated with the specified key.
     *
     * @param key The key to associate with the authentication state.
     * @param value The authentication state to store.
     */
    fun put(key: String, value: AuthState) {
        encryptedStore.put(key, serializeState(value))
        authStateMap.push(key, value)
    }

    /**
     * Retrieves the authentication state associated with the specified key.
     *
     * @param key The key associated with the authentication state.
     * @return The authentication state if found, or null otherwise.
     */
    fun get(key: String): AuthState? {
        return if (authStateMap.containsKey(key)) {
            authStateMap.get(key)
        } else encryptedStore.get(key)?.let { deserializeState(it) }.also {
            // If the state is found in the encrypted store, push it to the in-memory map.
            it?.let { authStateMap.push(key, it) }
        }
    }

    /**
     * Removes the authentication state associated with the specified key.
     *
     * @param key The key associated with the authentication state to remove.
     */
    fun remove(key: String) {
        authStateMap.pop(key)
        encryptedStore.remove(key)
    }

    /**
     * Retrieves the most recently added authentication state.
     *
     * @return The most recently added authentication state, or null if none exists.
     */
    fun activeState(): AuthState? {
        return authStateMap.peek()
    }

    /**
     * Retrieves the key associated with the most recently added authentication state.
     *
     * @return The key associated with the most recently added authentication state, or null if none exists.
     */
    fun activeStateKey(): String? {
        return authStateMap.peekKey()
    }

    // Serializes the given authentication state to a JSON string.
    private fun serializeState(authState: AuthState): String {
        return Json.encodeToString(authState)
    }

    // Deserializes the given JSON string to an authentication state.
    private fun deserializeState(encodedState: String?): AuthState? {
        return runCatching {
            encodedState?.let { Json.decodeFromString(it) as AuthState }
        }.getOrNull()
    }

    companion object {

        // Preference key for the encrypted key-value store.
        private val PREF_KEY = Companion::class.java.name

        private var instance: AuthStateRepo? = null

        /**
         * Retrieves the singleton instance of AuthStateRepo.
         *
         * @param context The context used to initialize the encrypted key-value store.
         * @return The singleton instance of AuthStateRepo.
         */
        @Synchronized
        fun getInstance(context: Context): AuthStateRepo {
            if (instance == null) {
                instance = AuthStateRepo(context)
            }
            return instance!!
        }
    }
}