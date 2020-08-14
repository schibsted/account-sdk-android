/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */

package com.schibsted.account.network.response;

import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;

import com.google.gson.annotations.SerializedName;
import com.schibsted.account.util.Preconditions;

/**
 * Represents a passwordless token response as received when requesting an auth code to be (re-)
 * sent.
 */
public class PasswordlessToken implements Parcelable {

    /**
     * Required by {@link Parcelable}. <b>You probably should not be accessing this manually</b>.
     */
    public static final Parcelable.Creator<PasswordlessToken> CREATOR = new Parcelable.Creator<PasswordlessToken>() {
        @Override
        public PasswordlessToken createFromParcel(@NonNull final Parcel in) {
            Preconditions.checkNotNull(in);
            return new PasswordlessToken(in);
        }

        @Override
        public PasswordlessToken[] newArray(int size) {
            return new PasswordlessToken[size];
        }
    };
    @SerializedName("passwordless_token")
    private final String value;

    /**
     * Constructor.
     *
     * @param value The passwordless token.
     */
    public PasswordlessToken(final String value) {
        this.value = value;
    }

    /**
     * Constructor for {@link #CREATOR}.
     *
     * @param in The {@link Parcel} to recreate from.
     */
    @SuppressWarnings("WeakerAccess")
    PasswordlessToken(@NonNull final Parcel in) {
        Preconditions.checkNotNull(in);

        this.value = in.readString();
    }

    @Override
    public String toString() {
        return "RequestId{" +
                "id='" + this.value + '\'' +
                '}';
    }

    /**
     * Provides the passwordless token.
     *
     * @return The passwordless token.
     */
    public String getValue() {
        return this.value;
    }

    /**
     * Returns 0.
     *
     * @return 0.
     */
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull final Parcel parcel, final int flags) {
        Preconditions.checkNotNull(parcel);

        parcel.writeString(this.value);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }

        PasswordlessToken that = (PasswordlessToken) object;

        return value.equals(that.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }
}
