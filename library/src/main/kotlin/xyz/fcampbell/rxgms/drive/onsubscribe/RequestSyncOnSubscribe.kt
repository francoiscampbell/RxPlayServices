package xyz.fcampbell.rxgms.drive.onsubscribe

import android.content.Context
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.Status
import com.google.android.gms.drive.Drive
import rx.Observable
import rx.Observer
import xyz.fcampbell.rxgms.common.onsubscribe.PendingResultOnSubscribe

/**
 * Created by francois on 2016-12-24.
 */
internal class RequestSyncOnSubscribe(
        ctx: Context
) : BaseDriveOnSubscribe<Status>(ctx) {
    override fun onGoogleApiClientReady(apiClient: GoogleApiClient, observer: Observer<in Status>) {
        val pendingResult = Drive.DriveApi.requestSync(apiClient)
        Observable.create(PendingResultOnSubscribe(pendingResult)).subscribe(observer)
    }
}