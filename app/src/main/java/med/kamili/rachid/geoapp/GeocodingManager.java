package med.kamili.rachid.geoapp;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.text.TextUtils;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class GeocodingManager {

    private final String BASE_LINK = "https://maps.googleapis.com/maps/api/geocode/json?";
    private final String ADRESS_PARAM = "address=";
    private final String LATLNG_PARAM = "latlng=";
    private final String KEY_PARAM = "&key=";
    private final String API_KEY = "AIzaSyAtrCWM07Z9QEQB4xP82Q_OXS7f_pwAVp4";

    private static GeocodingManager mInstance;
    private Context mContext;
    private IOnLoadFromAPI listener;

    private GeocodingManager(Context mContext) {
        this.mContext = mContext;
        this.listener = (IOnLoadFromAPI) mContext;
    }

    public static GeocodingManager getInstance(Context context) {
        if (mInstance == null)
            mInstance = new GeocodingManager(context);
        return mInstance;
    }

    public LatLng getLatLngByAddressGeocoder(String addressText) {
        Geocoder geocoder = new Geocoder(mContext, Locale.getDefault());
        if (Geocoder.isPresent()) {
            try {
                List<Address> list = geocoder.getFromLocationName(addressText, 1);
                if (list.isEmpty()) {
                    Toast.makeText(mContext, "No adress found", Toast.LENGTH_LONG).show();
                    return null;
                }
                Address address = list.get(0);
                return new LatLng(address.getLatitude(), address.getLongitude());

            } catch (IOException ioException) {
                Toast.makeText(mContext, "Service not available", Toast.LENGTH_LONG).show();
            }
        }
        return null;
    }

    public String getAddressByLatLngGeocoder(LatLng addressLatLng) {
        Geocoder geocoder = new Geocoder(mContext, Locale.getDefault());
        if (Geocoder.isPresent()) {
            try {
                List<Address> list = geocoder.getFromLocation(addressLatLng.latitude, addressLatLng.longitude, 1);
                if (list.isEmpty()) {
                    Toast.makeText(mContext, "No adress found", Toast.LENGTH_LONG).show();
                    return null;
                }
                Address address = list.get(0);

                List<String> addressFragments = new ArrayList<>();
                for (int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
                    addressFragments.add(address.getAddressLine(i));
                }
                return TextUtils.join(System.getProperty("line.separator"), addressFragments);
            } catch (IOException ioException) {
                Toast.makeText(mContext, "Service not available", Toast.LENGTH_LONG).show();
            } catch (IllegalArgumentException illegalArgumentException) {
                Toast.makeText(mContext, "Invalid latitude or longitude values", Toast.LENGTH_LONG).show();
            }
        }
        return null;
    }

    public void getLatLngByAddressAPI(String addressText) {

        RequestQueue queue = Volley.newRequestQueue(mContext);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET,
                getAdressUrl(addressText) , null,
                new ResponseFromAPI(),new ResponseFromAPI());

        queue.add(jsonObjectRequest);
    }

    public void getAddressByLatLngAPI(LatLng addressLatLng){
        RequestQueue queue = Volley.newRequestQueue(mContext);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET,
                getLatlngUrl(addressLatLng) , null,
                new ResponseFromAPI(),new ResponseFromAPI());

        queue.add(jsonObjectRequest);
    }

    private String getAdressUrl(String addressText) {
        return BASE_LINK + ADRESS_PARAM + addressText + KEY_PARAM + API_KEY;
    }

    private String getLatlngUrl(LatLng latLng) {
        return BASE_LINK + LATLNG_PARAM + latLng.latitude + "," + latLng.longitude + KEY_PARAM + API_KEY;
    }

    public class ResponseFromAPI implements Response.Listener<JSONObject>, Response.ErrorListener {

        @Override
        public void onResponse(JSONObject response) {
            try {
                JSONObject adressObject = ( (JSONObject) ((JSONArray) response.get("results")).get(0));
                JSONObject locationObject = ((JSONObject)( (JSONObject) adressObject.get("geometry")).get("location"));
                String address = adressObject.get("formatted_address").toString();
                LatLng latLng = new LatLng(
                        Double.parseDouble(locationObject.get("lat").toString()),
                        Double.parseDouble(locationObject.get("lng").toString())
                );

                listener.onLoadAddress(address, latLng);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onErrorResponse(VolleyError error) {
            Toast.makeText(mContext, error.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    public interface IOnLoadFromAPI{
        void onLoadAddress(String address, LatLng latLng);
    }
}
