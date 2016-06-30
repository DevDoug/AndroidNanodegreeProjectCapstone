package fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import com.android.volley.VolleyError;
import com.brainbeats.R;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.json.JSONArray;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import adapters.LibraryAdapter;
import architecture.AccountManager;
import entity.Track;
import entity.UserPlaylistsResponse;
import entity.UserTrackResponse;
import utils.Constants;
import web.WebApiManager;

public class LibraryTabFragment extends Fragment {

    ArrayList<Track> trackList;
    private RecyclerView mBeatListView;
    private LibraryAdapter mLibraryAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private int mDataType;
    public String mFilter = "";

    public LibraryTabFragment() {
        // Required empty public constructor
    }

    public static LibraryTabFragment newInstance(Constants.LibraryDataType dataType) {
        LibraryTabFragment tabFragment = new LibraryTabFragment();
        Bundle args = new Bundle();
        args.putInt(Constants.KEY_LIBRARY_DATA_TYPE,dataType.getCode());
        tabFragment.setArguments(args);
        return tabFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            mDataType = args.getInt(Constants.KEY_LIBRARY_DATA_TYPE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_library_tab, container, false);
        mBeatListView = (RecyclerView) v.findViewById(R.id.library_content_list);
        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        trackList = new ArrayList<>();

        if (Constants.LibraryDataType.Songs.getCode() == mDataType)
            loadUserSongs();
        else if (Constants.LibraryDataType.Playlists.getCode() == mDataType)
            loadUserPlaylist();
        else
            loadUserFavorites();
    }

    public ArrayList<Track> loadUserSongs() {
        WebApiManager.getUserTracks(getContext(), AccountManager.getInstance(getContext()).getUserId(), new WebApiManager.OnArrayResponseListener() {
            @Override
            public void onArrayResponse(JSONArray array) {
                Log.i(getClass().getSimpleName(), "Response = " + array.toString());
                Gson gson = new Gson();
                Type token = new TypeToken<ArrayList<UserTrackResponse>>(){}.getType();
                try {
                    ArrayList<UserTrackResponse> userTracks = gson.fromJson(array.toString(), token);
                    for (int i = 0; i < userTracks.size(); i++) {
                        Track tempTrack = new Track();
                        tempTrack.setTitle(userTracks.get(i).getTitle());
                        trackList.add(tempTrack);
                    }

                    if(!mFilter.equalsIgnoreCase(""))
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                            Stream<Track> trackStream = trackList.stream().filter(t -> t.getTitle().equalsIgnoreCase(mFilter));
                            trackList = (ArrayList<Track>) trackStream;
                        } else {
                            Log.i("test",mFilter);
                        }
                    //trackList = trackList.stream.filter(p -> p.getAge() > 16).collect(Collectors.toList());

                    setTrackList();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

        }, new WebApiManager.OnErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.toString();
            }
        }) ;
        return null;
    }

    public void loadUserPlaylist() {
        WebApiManager.getUserPlaylists(getContext(), AccountManager.getInstance(getContext()).getUserId(), new WebApiManager.OnArrayResponseListener() {
            @Override
            public void onArrayResponse(JSONArray array) {
                Log.i(getClass().getSimpleName(), "Response = " + array.toString());
                Gson gson = new Gson();
                Type token = new TypeToken<ArrayList<UserPlaylistsResponse>>() {
                }.getType();
                try {
                    ArrayList<UserPlaylistsResponse> userPlaylists = gson.fromJson(array.toString(), token);
                    trackList = (ArrayList<Track>) userPlaylists.get(0).getTracks();
                    setTrackList();

                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }, new WebApiManager.OnErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.i(getClass().getSimpleName(), "Response = " + error.toString());
            }
        });
    }

    public void loadUserFavorites() {
        WebApiManager.getUserFavorites(getContext(), AccountManager.getInstance(getContext()).getUserId(), new WebApiManager.OnArrayResponseListener() {
            @Override
            public void onArrayResponse(JSONArray array) {
                Log.i(getClass().getSimpleName(), "Response = " + array.toString());
                Gson gson = new Gson();
                Type token = new TypeToken<ArrayList<UserTrackResponse>>() {
                }.getType();
                try {
                    ArrayList<UserTrackResponse> userTracks = gson.fromJson(array.toString(), token);
                    for(int i = 0; i < userTracks.size();i++){
                        Track tempTrack = new Track();
                        tempTrack.setTitle(userTracks.get(i).getTitle());
                        trackList.add(tempTrack);
                    }
                    setTrackList();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }, new WebApiManager.OnErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.i(getClass().getSimpleName(), "Response = " + error.toString());
            }
        });
    }

    public void setTrackList(){
        mLibraryAdapter = new LibraryAdapter(getContext(), trackList);
        mLayoutManager = new LinearLayoutManager(getContext(),LinearLayoutManager.VERTICAL, false);
        mBeatListView.setLayoutManager(mLayoutManager);
        mBeatListView.setAdapter(mLibraryAdapter);
    }

    public void updateFilterParams(String params){
        mFilter = params;
    }
}
