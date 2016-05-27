package web;

import android.content.Context;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonRequest;
import com.brainbeats.R;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;
import utils.Constants;

/**
 * Created by douglas on 5/24/2016.
 */
public class WebApiManager {

    public static final int MY_SOCKET_TIMEOUT_MS = 30000;

    //Sound cloud API links
    public static final String API_ROOT_URL     = "https://api.soundcloud.com";
    public static final String API_TRACKS_URL   = "/tracks/";
    public static final String API_PLAYLIST_URL = "/playlists/";

    //Sound cloud API keys
    public static final String KEY_ClIENT_ID    = "client_id";

    public interface OnResponseListener {
        void onResponse(JSONObject object);
    }

    public interface OnErrorListener {
        void onErrorResponse(VolleyError error);
    }

    public static void getTrack(Context context, String trackId, final OnResponseListener responseListener, final OnErrorListener errorListener) {
        HashMap<String, String> mParams = new HashMap<>();
        mParams.put(KEY_ClIENT_ID, Constants.SOUND_CLOUD_CLIENT_ID);
        String url = API_ROOT_URL + API_TRACKS_URL + trackId;
        try {
            JSONObject jsonRequest = new JSONObject();
            sendRequest(context, Request.Method.GET, url, mParams, jsonRequest, responseListener, errorListener);
        } catch (Exception ex) {
            errorListener.onErrorResponse(new VolleyError(context.getString(R.string.unknown_volley_error)));
        }
    }

    public static void getPlayList(Context context, String playlistId, final OnResponseListener responseListener, final OnErrorListener errorListener){
        HashMap<String, String> mParams = new HashMap<>();
        mParams.put(KEY_ClIENT_ID, Constants.SOUND_CLOUD_CLIENT_ID);
        String url = API_ROOT_URL + API_PLAYLIST_URL + playlistId;
        try {
            JSONObject jsonRequest = new JSONObject();
            sendRequest(context, Request.Method.GET, url, mParams, jsonRequest, responseListener, errorListener);
        } catch (Exception ex) {
            errorListener.onErrorResponse(new VolleyError(context.getString(R.string.unknown_volley_error)));
        }
    }

    public static void sendRequest(Context context, int method, final String url, final HashMap<String, String> urlParams, final JSONObject requestParam, final OnResponseListener onResponseListener, final OnErrorListener onErrorListener) {
        JsonRequest request = new JsonObjectRequest(method, url, requestParam, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                onResponseListener.onResponse(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                onErrorListener.onErrorResponse(error);
            }
        }) {
            //Note: Need to override this method to put params in the URL, because JsonObjectRequest does not call getParams().
            @Override
            public String getUrl() {
                //NOTE: Code in here need to be replicated in the other case below (in case of JsonObjectRequest).
                StringBuilder stringBuilder = new StringBuilder(url);
                if (urlParams != null && urlParams.size() > 0) {
                    int i = 1;
                    for (Map.Entry<String, String> entry : urlParams.entrySet()) {
                        if (i == 1) {
                            stringBuilder.append("?" + entry.getKey() + "=" + entry.getValue());
                        } else {
                            stringBuilder.append("&" + entry.getKey() + "=" + entry.getValue());
                        }
                        i++;
                    }
                }

                String url = stringBuilder.toString();
                return url;
            }

            @Override
            public Map<String, String> getHeaders() {
                //NOTE: Code in here need to be replicated in the other case below (in case of JsonObjectRequest).
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Content-Type", "application/json; charset=utf-8");
                headers.put(KEY_ClIENT_ID, Constants.SOUND_CLOUD_CLIENT_ID);
                //TODO: Add more headers here if needed.

                return headers;
            }
        };

        request.setRetryPolicy(new DefaultRetryPolicy(
                WebApiManager.MY_SOCKET_TIMEOUT_MS,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        SingletonVolley.getInstance(context).addToRequestQueue(request);
    }

}