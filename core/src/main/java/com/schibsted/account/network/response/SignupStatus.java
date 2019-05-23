/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */

package com.schibsted.account.network.response;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.RestrictTo;

import com.schibsted.account.util.Preconditions;

/**
 * Represents the signUp status for an identifier.
 *
 * @see <a href="http://techdocs.spid.no/endpoints/GET/phone/%7Bphone%7D/status/">
 * GET /phone/{phone}/status | Schibsted account API Documentation</a>
 * @see <a href="http://techdocs.spid.no/endpoints/GET/email/%7Bemail%7D/status/">
 * GET /email/{email}/status | Schibsted account API Documentation</a>
 */
public final class SignupStatus implements Parcelable {
    /**
     * Required by {@link Parcelable}. <b>You probably should not be accessing this manually</b>.
     */
    public static final Parcelable.Creator<SignupStatus> CREATOR = new Parcelable.Creator<SignupStatus>() {
        public SignupStatus createFromParcel(@NonNull final Parcel source) {
            Preconditions.checkNotNull(source);
            // noinspection PrivateMemberAccessBetweenOuterAndInnerClass
            return new SignupStatus(source);
        }

        public SignupStatus[] newArray(int size) {
            return new SignupStatus[size];
        }
    };
    private final String identifier;
    private final boolean exists;
    private final boolean available;
    private final boolean verified;

    /**
     * Constructor.
     *
     * @param identifier The identifier that this status is associated to.
     * @param source     The payload of the result of the original status query.
     */
    @RestrictTo(RestrictTo.Scope.LIBRARY)
    public SignupStatus(final String identifier, final AccountStatusResponse source) {
        this.identifier = identifier;
        this.exists = source.exists();
        this.available = source.isAvailable();
        this.verified = source.isVerified();
    }

    /**
     * Required by {@link Parcelable}. <b>You probably should not be accessing this manually</b>.
     *
     * @param in The source.
     */
    @RestrictTo(RestrictTo.Scope.SUBCLASSES)
    private SignupStatus(@NonNull final Parcel in) {
        this.identifier = in.readString();
        this.exists = in.readInt() != 0;
        this.available = in.readInt() != 0;
        this.verified = in.readInt() != 0;
    }

    @NonNull
    public String getIdentifier() {
        return this.identifier;
    }

    /**
     * States whether the identifier exists or not.
     *
     * @return <code>true</code> if the queried identifier exists, <code>false</code> if it does
     * not.
     */
    public boolean exists() {
        return this.exists;
    }

    /**
     * States whether the identifier is available or not.
     *
     * @return <code>true</code> if the queried identifier is available, <code>false</code> if it is
     * not.
     */
    public boolean isAvailable() {
        return this.available;
    }

    /**
     * States whether the identifier is verified or not.
     *
     * @return <code>true</code> if the queried identifier is verified, <code>false</code> if it is
     * not.
     */
    public boolean isVerified() {
        return this.verified;
    }

    /**
     * Required by {@link Parcelable}. <b>You probably should not be accessing this manually</b>.
     *
     * @return 0 (no special parcel contents).
     */
    @Override
    @IntRange(from = 0, to = 0)
    @RestrictTo(RestrictTo.Scope.SUBCLASSES)
    public int describeContents() {
        return 0;
    }

    /**
     * Required by {@link Parcelable}. <b>You probably should not be accessing this manually</b>.
     *
     * @param out   The {@link Parcel} to write the object to.
     * @param flags Additional flags about how the object should be written.
     */
    @Override
    @RestrictTo(RestrictTo.Scope.SUBCLASSES)
    public void writeToParcel(@NonNull final Parcel out, final int flags) {
        out.writeString(this.identifier);
        out.writeInt(this.exists ? 1 : 0);
        out.writeInt(this.available ? 1 : 0);
        out.writeInt(this.verified ? 1 : 0);
    }
}
