/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */

package com.schibsted.account.engine.controller

import android.os.Parcel
import android.os.Parcelable
import com.schibsted.account.common.util.Logger
import com.schibsted.account.common.util.readStack
import com.schibsted.account.engine.integration.contract.Contract
import com.schibsted.account.engine.step.Step
import java.util.Stack

abstract class Controller<in T : Contract<*>>() : Parcelable {
    internal val navigation: Stack<Step> = Stack()

    protected constructor(parcel: Parcel) : this() {
        val inStack = parcel.readStack<Step>(Controller::class.java.classLoader)
        navigation.addAll(inStack)
    }

    /**
     * Perform the login sequence. Additional calls to this function will re-trigger the currently
     * active task.
     * @param contract The required contract to call to provide any additionally required information
     */
    abstract fun evaluate(contract: T)

    /**
     * Goes back one step in the controller.
     * @return True of an element was popped off the stack, false if we're already at the beginning
     */
    @JvmOverloads
    fun back(step: Int = 1) {
        for (i in 0 until step) {
            if (navigation.size > 0) {
                navigation.pop()
            } else {
                Logger.warn( "Attempted to go back when the navigation stack was empty" )
            }
        }
    }

    fun start(contract: T) = evaluate(contract)

    internal inline fun <reified E : Step> findOnStack(): E? = navigation.find { it is E } as E?

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeList(navigation.toList())
    }

    override fun describeContents(): Int = 0
}
