package fragments;


import android.app.Dialog;
import android.graphics.drawable.Drawable;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import com.brainbeats.MixerActivity;
import com.brainbeats.R;

import java.util.ArrayList;
import java.util.List;

import adapters.ImageAdapter;
import adapters.MixItemAdapter;
import data.MixContract;
import data.MixDbHelper;
import model.Mix;
import model.MixItem;
import utils.Constants;
import utils.MixManager;

public class MixerDetailFragment extends Fragment implements ImageAdapter.DialogImageSelectedListener {

    private List<MixItem> mixItemList = new ArrayList<>();
    private RecyclerView mMixerItemList;
    private MixItemAdapter mMixerItemAdapter;
    public Bundle mUserSelections;
    public EditText mMixTitle;
    public Mix mSelectedMix;
    private ImageView mPlayMixButton;
    private CoordinatorLayout mCoordinatorLayout;
    private Handler handler = new Handler();
    private Dialog mDialog;

    public MixerDetailFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_mixer_detail, container, false);
        mMixerItemList = (RecyclerView) v.findViewById(R.id.beat_mix_item_list);
        mMixTitle = (EditText) v.findViewById(R.id.track_title);
        mCoordinatorLayout = (CoordinatorLayout) getActivity().findViewById(R.id.main_content_coordinator_layout);
        mPlayMixButton = (ImageView) v.findViewById(R.id.play_song_button);
        ((TextView)v.findViewById(R.id.separator_title)).setText(R.string.beat_levels);
        return v;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
        Drawable up = DrawableCompat.wrap(ContextCompat.getDrawable(getContext(), R.drawable.ic_up));
        DrawableCompat.setTint(up, getResources().getColor(R.color.theme_primary_text_color));
        toolbar.setNavigationIcon(up);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager fm = getActivity().getSupportFragmentManager();
                mSelectedMix.setMixTitle(mMixTitle.getText().toString());
                ((MixerActivity) getActivity()).getContentResolver().update(MixContract.MixEntry.CONTENT_URI,Constants.buildMixRecord(mSelectedMix), MixDbHelper.DB_ID_FIELD + mSelectedMix.getMixId(), null);
                for (int i = 0; i < mSelectedMix.getMixItems().size(); i++) {
                    MixItem item = mSelectedMix.getMixItems().get(i);
                    ((MixerActivity) getActivity()).getContentResolver().update(
                            MixContract.MixItemsEntry.CONTENT_URI,
                            Constants.buildMixItemsRecord(mSelectedMix.getMixId(), item),
                            MixDbHelper.DB_ID_FIELD + item.getMixItemId(),
                            null);
                }
                InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(getActivity().INPUT_METHOD_SERVICE);
                if(getActivity().getCurrentFocus() != null) //
                    inputMethodManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);
                    ((MixerActivity) getActivity()).navigateUpOrBack(getActivity(), fm);
            }
        });
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mUserSelections = getArguments();
        if (mUserSelections != null) {
            mSelectedMix = (Mix) mUserSelections.get(Constants.KEY_EXTRA_SELECTED_MIX);
            mMixTitle.setText(mSelectedMix.getBeatTitle());
            mixItemList = mSelectedMix.getMixItems();

            MixItem addNewMix = new MixItem();
            addNewMix.setMixItemTitle("Add New");
            mixItemList.add(addNewMix);
        }

        mMixerItemAdapter = new MixItemAdapter(getContext(), mixItemList, this);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        mMixerItemList.setLayoutManager(mLayoutManager);
        mMixerItemList.setAdapter(mMixerItemAdapter);
        mMixerItemAdapter.notifyDataSetChanged();

        mPlayMixButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playMix();
            }
        });
    }

    public void showAddBeatItemDialog(){
        mDialog = Constants.buildImageListDialogue(getContext(), getContext().getResources().getString(R.string.add_sound_item_to_current_beat), this);
    }

    @Override
    public void dialogImageSelected(int position) {
        MixItem item = new MixItem();
        switch (position) {
            case 0:
                item.setMixItemTitle(getContext().getResources().getStringArray(R.array.default_mix_items)[0]);
                mDialog.dismiss();
                break;
            case 1:
                item.setMixItemTitle(getContext().getResources().getStringArray(R.array.default_mix_items)[1]);
                mDialog.dismiss();
                break;
            case 2:
                item.setMixItemTitle(getContext().getResources().getStringArray(R.array.default_mix_items)[2]);
                mDialog.dismiss();
                break;
            case 3:
                item.setMixItemTitle(getContext().getResources().getStringArray(R.array.default_mix_items)[3]);
                mDialog.dismiss();
                break;
        }
        item.setMixItemLevel(Constants.MIX_ITEM_DEFAULT_LEVEL);
        boolean mixItemExists = false;
        for(MixItem mix : mixItemList) {
            if(mix.getMixItemTitle().equalsIgnoreCase(item.getMixItemTitle())) {
                mixItemExists = true;
            }
        }

        if (!mixItemExists) {
            Uri returnRow = getContext().getContentResolver().insert(MixContract.MixItemsEntry.CONTENT_URI, Constants.buildMixItemsRecord(mSelectedMix.getMixId(), item));
            mixItemList.add(0, item);
            mMixerItemList.setAdapter(mMixerItemAdapter);
            mMixerItemAdapter.notifyDataSetChanged();
        } else {
            Snackbar errorSnack = Snackbar.make(mCoordinatorLayout, String.format(getContext().getString(R.string.mix_item_exists_snack_message), item.getMixItemTitle()), Snackbar.LENGTH_LONG);
            errorSnack.show();
        }
    }

    public void playMix(){
        final Thread thread = new Thread(new Runnable() {
            public void run() {
                handler.post(new Runnable() {
                    public void run() {
                        MixManager.getInstance(getContext()).playMix((ArrayList<MixItem>) mixItemList);
                    }
                });
            }
        });
        thread.start();
    }
}
