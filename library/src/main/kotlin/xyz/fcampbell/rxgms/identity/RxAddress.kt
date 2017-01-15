package xyz.fcampbell.rxgms.identity

import android.content.Context
import com.google.android.gms.identity.intents.Address
import com.google.android.gms.identity.intents.UserAddressRequest
import rx.Completable
import xyz.fcampbell.rxgms.common.ApiClientDescriptor
import xyz.fcampbell.rxgms.common.ApiDescriptor
import xyz.fcampbell.rxgms.common.RxGmsApi
import xyz.fcampbell.rxgms.common.util.toCompletable

/**
 * Created by francois on 2017-01-13.
 */
class RxAddress(
        apiClientDescriptor: ApiClientDescriptor
) : RxGmsApi<Address.AddressOptions>(
        apiClientDescriptor,
        ApiDescriptor(Address.API)
) {
    constructor(
            context: Context
    ) : this(ApiClientDescriptor(context))

    fun requestUserAddress(userAddressRequest: UserAddressRequest): Completable {
        return apiClient.toCompletable { Address.requestUserAddress(it.first, userAddressRequest, 25) }
    }
}