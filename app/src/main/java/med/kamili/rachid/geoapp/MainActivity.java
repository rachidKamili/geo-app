package med.kamili.rachid.geoapp;

import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback , GeocodingManager.IOnLoadFromAPI {

    @BindView(R.id.etAdress)
    EditText etAdress;
    @BindView(R.id.etLatitude)
    EditText etLatitude;
    @BindView(R.id.etLongitude)
    EditText etLongitude;
    @BindView(R.id.btnCoder)
    Button btnCoder;
    @BindView(R.id.btnGeoApi)
    Button btnGeoApi;
    @BindView(R.id.sReverse)
    Switch sReverse;
    private GeocodingManager mGeocodingManager;
    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        mGeocodingManager = GeocodingManager.getInstance(this);
        initMap();
    }

    private void initMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
    }

    @OnClick({R.id.btnCoder, R.id.btnGeoApi})
    public void onBtnClicked(View view) {
        switch (view.getId()) {
            case R.id.btnCoder:
                if (sReverse.isChecked()) {
                    NumberFormat nf = NumberFormat.getInstance();

                    LatLng latLng;
                    try {
                        latLng = new LatLng(
                                nf.parse(etLatitude.getText().toString()).doubleValue(),
                                nf.parse(etLongitude.getText().toString()).doubleValue()
                        );
                    } catch (ParseException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Invalid latitude or longitude values", Toast.LENGTH_LONG).show();
                        return;
                    }
                    String address = mGeocodingManager.getAddressByLatLngGeocoder(latLng);
                    if (address!=null){
                        etAdress.setText(address);
                        mMap.addMarker(new MarkerOptions().position(latLng).title(address));
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                    }
                } else {
                    LatLng latLng = mGeocodingManager.getLatLngByAddressGeocoder(etAdress.getText().toString());
                    if (latLng!=null){
                        etLatitude.setText(String.valueOf(latLng.latitude));
                        etLongitude.setText(String.valueOf(latLng.longitude));
                        mMap.addMarker(new MarkerOptions().position(latLng).title(etAdress.getText().toString()));
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                    }
                }
                break;
            case R.id.btnGeoApi:
                if (sReverse.isChecked()) {
                    NumberFormat nf = NumberFormat.getInstance();
                    LatLng latLng;
                    try {
                        latLng = new LatLng(
                                nf.parse(etLatitude.getText().toString()).doubleValue(),
                                nf.parse(etLongitude.getText().toString()).doubleValue()
                        );
                    } catch (ParseException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Invalid latitude or longitude values", Toast.LENGTH_LONG).show();
                        return;
                    }
                    mGeocodingManager.getAddressByLatLngAPI(latLng);
                }else {
                    mGeocodingManager.getLatLngByAddressAPI(etAdress.getText().toString());
                }
                break;
        }
    }

    @Override
    public void onLoadAddress(String address, LatLng latLng) {
        etLatitude.setText(String.valueOf(latLng.latitude));
        etLongitude.setText(String.valueOf(latLng.longitude));
        etAdress.setText(address);
        mMap.addMarker(new MarkerOptions().position(latLng).title(address));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
    }
}
