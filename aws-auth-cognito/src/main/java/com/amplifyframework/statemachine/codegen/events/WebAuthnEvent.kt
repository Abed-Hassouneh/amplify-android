/*
 * Copyright 2024 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *  http://aws.amazon.com/apache2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package com.amplifyframework.statemachine.codegen.events

import com.amplifyframework.statemachine.StateMachineEvent
import com.amplifyframework.statemachine.codegen.data.WebAuthnSignInContext
import com.amplifyframework.statemachine.codegen.events.SignInEvent.EventType
import java.util.Date

internal class WebAuthnEvent(val eventType: EventType, override val time: Date? = null) : StateMachineEvent {

    sealed class EventType {
        data class FetchCredentialOptions(val signInContext: WebAuthnSignInContext) : EventType()
        data class AssertCredentialOptions(val signInContext: WebAuthnSignInContext) : EventType()
        data class VerifyCredentialsAndSignIn(val signInContext: WebAuthnSignInContext) : EventType()
        data class ThrowError(val exception: Exception, val signInContext: WebAuthnSignInContext) : EventType()
    }

    override val type: String = eventType.javaClass.simpleName
}