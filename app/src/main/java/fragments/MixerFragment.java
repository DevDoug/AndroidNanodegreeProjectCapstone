package fragments;

import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.brainbeats.LibraryActivity;
import com.brainbeats.MixerActivity;
import com.brainbeats.R;
import adapters.MixerAdapter;
import data.MixContract;
import model.Mix;
import utils.Constants;


public class MixerFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, AdapterView.OnItemClickListener {

    private static final int URL_LOADER = 0;

    private ListView mMixerItems;
    private TextView mEmptyText;
    private MixerAdapter mMixerAdapter;
    private OnFragmentInteractionListener mListener;
    public FloatingActionButton mAddNewBeatButton;
    public AlertDialog mAddOptionsDialog;

    public MixerFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_mixer, container, false);
        mMixerItems = (ListView) v.findViewById(R.id.mixer_list);
        mEmptyText = (TextView) v.findViewById(R.id.empty_text);
        ((TextView)v.findViewById(R.id.separator_title)).setText(R.string.mixes);
        mAddNewBeatButton = (FloatingActionButton) v.findViewById(R.id.mixer_fob);
        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(URL_LOADER, null, this);
        mMixerItems.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Cursor c = mMixerAdapter.getCursor();
                ((MixerActivity) getActivity()).loadMixerDetailFragment(Constants.buildMixFromCursor(getContext(),c,position));
            }
        });
        mAddNewBeatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAddOptionsDialog = Constants.buildListDialogue(getContext(),getString(R.string.new_beat_title),R.array.new_beat_options,MixerFragment.this);
            }
        });
    }

    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        switch (position) {
            case 0:
                Mix defaultMix = Constants.buildNewDefaultMixRecord(getContext());
                Uri returnRow = getActivity().getContentResolver().insert(MixContract.MixEntry.CONTENT_URI,Constants.buildMixRecord(defaultMix));
                long returnRowId = ContentUris.parseId(returnRow);
                getActivity().getContentResolver().bulkInsert(MixContract.MixItemsEntry.CONTENT_URI,Constants.buildMixItemsBulkRecord(returnRowId,defaultMix.getMixItems()));
                mAddOptionsDialog.dismiss();
                break;
            case 1:
                mAddOptionsDialog.dismiss();
                mAddOptionsDialog = Constants.buildListDialogue(getContext(),getString(R.string.create_beat_from_title),R.array.new_from_existing_beat_options, new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        switch (position) {
                            case 0:
                                Toast.makeText(getContext(), "Creating from resource", Toast.LENGTH_LONG).show();
                                break;
                            case 1:
                                Toast.makeText(getContext(), "Creating from resource", Toast.LENGTH_LONG).show();
                                break;
                            case 2:
                                Intent libraryIntent = new Intent(getContext(), LibraryActivity.class);
                                startActivity(libraryIntent);
                                break;
                        }
                    }
                });
                break;
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int loaderID, Bundle args) {
        switch (loaderID) {
            case URL_LOADER:
                // Returns a new CursorLoader
                return new CursorLoader(
                        getActivity(),         // Parent activity context
                        MixContract.MixEntry.CONTENT_URI,  // Table to query
                        null,                          // Projection to return
                        null,                  // No selection clause
                        null,                  // No selection arguments
                        null                   // Default sort order
                );
            default:
                // An invalid id was passed in
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data == null) { //no mix data found
            mMixerItems.setVisibility(View.GONE);
            mEmptyText.setVisibility(View.VISIBLE);
        } else {
            mMixerAdapter = new MixerAdapter(getContext(), data,0);
            mMixerItems.setAdapter(mMixerAdapter);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }
}