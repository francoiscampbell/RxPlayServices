package pl.charmas.android.reactivelocation.sample;

import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.AutocompletePredictionBuffer;
import com.google.android.gms.location.places.PlaceLikelihood;
import com.google.android.gms.location.places.PlaceLikelihoodBuffer;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import java.util.List;
import java.util.concurrent.TimeUnit;

import pl.charmas.android.reactivelocation.DataBufferObservable;
import pl.charmas.android.reactivelocation.ReactiveLocationProvider;
import pl.charmas.android.reactivelocation.sample.utils.TextObservable;
import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.subscriptions.CompositeSubscription;

import static rx.android.app.AppObservable.bindActivity;

public class PlacesActivity extends ActionBarActivity {

    private TextView currentPlaceView;
    private EditText queryView;
    private ListView placeSuggestionsList;
    private ReactiveLocationProvider reactiveLocationProvider;
    private CompositeSubscription compositeSubscription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_places);
        currentPlaceView = (TextView) findViewById(R.id.current_place_view);
        queryView = (EditText) findViewById(R.id.place_query_view);
        placeSuggestionsList = (ListView) findViewById(R.id.place_suggestions_list);

        reactiveLocationProvider = new ReactiveLocationProvider(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        compositeSubscription = new CompositeSubscription();
        compositeSubscription.add(
                bindActivity(this, reactiveLocationProvider.getCurrentPlace(null))
                        .flatMap(new Func1<PlaceLikelihoodBuffer, Observable<PlaceLikelihood>>() {
                            @Override
                            public Observable<PlaceLikelihood> call(PlaceLikelihoodBuffer placeLikelihoods) {
                                return DataBufferObservable.from(placeLikelihoods);
                            }
                        })
                        .firstOrDefault(null)
                        .subscribe(new Action1<PlaceLikelihood>() {
                            @Override
                            public void call(PlaceLikelihood place) {
                                if (place != null) {
                                    currentPlaceView.setText(place.getPlace().getName());
                                }
                            }
                        })
        );

        Observable<String> queryObservable = TextObservable.create(queryView)
                .debounce(1, TimeUnit.SECONDS)
                .filter(new Func1<String, Boolean>() {
                    @Override
                    public Boolean call(String s) {
                        return !TextUtils.isEmpty(s);
                    }
                });
        Observable<Location> lastKnownLocationObservable = reactiveLocationProvider.getLastKnownLocation();
        Observable<List<String>> suggestionsObservable = Observable
                .combineLatest(queryObservable, lastKnownLocationObservable, new Func2<String, Location, QueryWithCurrentLocation>() {
                    @Override
                    public QueryWithCurrentLocation call(String query, Location currentLocation) {
                        return new QueryWithCurrentLocation(query, currentLocation);
                    }
                }).flatMap(new Func1<QueryWithCurrentLocation, Observable<AutocompletePredictionBuffer>>() {
                    @Override
                    public Observable<AutocompletePredictionBuffer> call(QueryWithCurrentLocation q) {
                        if (q.location == null) return Observable.empty();

                        double latitude = q.location.getLatitude();
                        double longitude = q.location.getLatitude();
                        LatLngBounds bounds = new LatLngBounds(
                                new LatLng(latitude - 0.05, longitude - 0.05),
                                new LatLng(latitude + 0.05, longitude + 0.05)
                        );
                        return reactiveLocationProvider.getPlaceAutocompletePredictions(q.query, bounds, null);
                    }
                }).flatMap(new Func1<AutocompletePredictionBuffer, Observable<List<String>>>() {
                    @Override
                    public Observable<List<String>> call(AutocompletePredictionBuffer autocompletePredictions) {
                        return DataBufferObservable.from(autocompletePredictions).map(new Func1<AutocompletePrediction, String>() {
                            @Override
                            public String call(AutocompletePrediction autocompletePrediction) {
                                return autocompletePrediction.getDescription();
                            }
                        }).toList();
                    }
                });

        compositeSubscription.add(bindActivity(this, suggestionsObservable).subscribe(new Action1<List<String>>() {
            @Override
            public void call(List<String> autocompletePredictions) {
                placeSuggestionsList.setAdapter(new ArrayAdapter<>(PlacesActivity.this, android.R.layout.simple_list_item_1, autocompletePredictions));
            }
        }));
    }

    @Override
    protected void onStop() {
        super.onStop();
        compositeSubscription.unsubscribe();
        compositeSubscription = null;
    }

    private static class QueryWithCurrentLocation {
        public final String query;
        public final Location location;

        private QueryWithCurrentLocation(String query, Location location) {
            this.query = query;
            this.location = location;
        }
    }
}
