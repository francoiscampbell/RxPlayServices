package xyz.fcampbell.rxgms.games

import android.content.Context
import com.google.android.gms.common.api.Scope
import com.google.android.gms.games.Games
import com.google.android.gms.games.event.Events
import io.reactivex.Completable
import io.reactivex.Observable
import xyz.fcampbell.rxgms.common.ApiClientDescriptor
import xyz.fcampbell.rxgms.common.ApiDescriptor
import xyz.fcampbell.rxgms.common.RxGmsApi

/**
 * Created by francois on 2017-01-13.
 */
@Suppress("unused")
class RxEvents(
        apiClientDescriptor: ApiClientDescriptor,
        gamesOptions: Games.GamesOptions,
        vararg scopes: Scope
) : RxGmsApi<Events, Games.GamesOptions>(
        apiClientDescriptor,
        ApiDescriptor(Games.API, Games.Events, gamesOptions, *scopes)
) {
    constructor(
            context: Context,
            gamesOptions: Games.GamesOptions,
            vararg scopes: Scope
    ) : this(ApiClientDescriptor(context), gamesOptions, *scopes)

    fun load(forceReload: Boolean): Observable<Events.LoadEventsResult> {
        return fromPendingResult { load(it, forceReload) }
    }

    fun loadByIds(forceReload: Boolean, vararg eventIds: String): Observable<Events.LoadEventsResult> {
        return fromPendingResult { loadByIds(it, forceReload, *eventIds) }
    }

    fun increment(eventId: String, incrementAmount: Int): Completable {
        return toCompletable { increment(it, eventId, incrementAmount) }
    }
}