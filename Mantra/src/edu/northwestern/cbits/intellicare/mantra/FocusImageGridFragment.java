package edu.northwestern.cbits.intellicare.mantra;

import java.io.File;

import edu.northwestern.cbits.intellicare.mantra.DatabaseHelper.FocusImageCursor;
import edu.northwestern.cbits.intellicare.mantra.activities.NewFocusBoardActivity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.AdapterView.OnItemClickListener;

public class FocusImageGridFragment extends Fragment {

	private static final String CN = "FocusImageGridFragment";
	private FocusImageCursor mCursor;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.focus_images_grid, container,
				false);
		Intent intent = getActivity().getIntent();
		long focusBoardId = intent.getLongExtra(
				NewFocusBoardActivity.FOCUS_BOARD_ID, -1);
		mCursor = FocusBoardManager.get(getActivity()).queryFocusImages(focusBoardId);
		FocusImageCursorAdapter adapter = new FocusImageCursorAdapter(
				getActivity(), mCursor);
		GridView gv = (GridView) view.findViewById(R.id.gridview);
		gv.setAdapter(adapter);
		
		// DBG: let's inspect the contents of the image set for the selected Focus board
		mCursor.moveToPosition(-1);
		for(int i = 0; mCursor.moveToNext(); i++) {
			for(int j=0; j < mCursor.getColumnCount(); j++) {
				Log.d(CN+".onCreateView", "row[" + i + "][\"" + mCursor.getColumnName(j) + "\"] = \"" + mCursor.getString(j) + "\"");
			}
		}
		
		// when the user taps an image, display it
		gv.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int positionInView, long rowId) {
				Intent intent = new Intent();
				intent.setAction(android.content.Intent.ACTION_VIEW);
				mCursor.moveToPosition(positionInView);
				String filePath = mCursor.getString(FocusBoardManager.COL_INDEX_FILE_PATH);
				Log.d(CN+".onItemClick", "filePath = " + filePath);
				intent.setDataAndType(Uri.fromFile(new File(filePath)), "image/*");
				startActivity(intent);		
			}
		});
		
		return view;
	}

	@Override
	public void onDestroy() {
		mCursor.close();
		super.onDestroy();
	}
}
