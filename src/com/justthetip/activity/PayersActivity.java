package com.justthetip.activity;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;

import com.justthetip.R;
import com.justthetip.data.Receipt;
import com.justthetip.data.ReceiptManager;

public class PayersActivity extends ActionBarActivity {

	private static final String TAG = PayersActivity.class.getSimpleName();

	private static List<Integer> payerIds = new ArrayList<Integer>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_payers);

		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction().add(R.id.container, new PayersFragment()).commit();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.payers, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	public void addPayer(View view) {
		RelativeLayout layout = (RelativeLayout) this.findViewById(R.id.payer_layout);

		// create a new text field
		int currentSize = payerIds.size();
		int payerId = currentSize + 1;
		EditText editText = new EditText(PayersActivity.this);
		editText.setId(payerId);
		editText.setHint("(Payer " + (currentSize + 1) + ")");
		editText.setSingleLine();

		// if it's not the first one, have it be below the last one
		LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		if (currentSize > 0) {
			int lastPayerId = payerIds.get(currentSize - 1);
			params.addRule(RelativeLayout.ALIGN_LEFT, lastPayerId);
			params.addRule(RelativeLayout.BELOW, lastPayerId);
		}
		editText.setLayoutParams(params);

		// add the next text field
		layout.addView(editText);
		payerIds.add(payerId);

		// modify the add payer button's relative position to be below the new text field
		Button addPayerButton = (Button) findViewById(R.id.add_payer);
		LayoutParams buttonParams = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		buttonParams.addRule(RelativeLayout.ALIGN_LEFT, payerId);
		buttonParams.addRule(RelativeLayout.BELOW, payerId);
		addPayerButton.setLayoutParams(buttonParams);

		// set the remove payer button to only be enabled if there's more than one payer
		Button removePayerButton = (Button) findViewById(R.id.remove_payer);
		removePayerButton.setEnabled(payerIds.size() > 1);
	}

	public void removePayer(View view) {
		// make sure we have a payer to remove
		if (payerIds.isEmpty()) {
			return;
		}

		RelativeLayout layout = (RelativeLayout) this.findViewById(R.id.payer_layout);

		// remove the last text field
		int lastIndex = payerIds.size() - 1;
		int payerId = payerIds.get(lastIndex);
		layout.removeView(findViewById(payerId));
		payerIds.remove(lastIndex);

		// modify the add payer button's relative position to be below the last text field, or no text field if none
		Button addPayerButton = (Button) findViewById(R.id.add_payer);
		LayoutParams buttonParams = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		if (!payerIds.isEmpty()) {
			int lastPayerId = payerIds.get(payerIds.size() - 1);
			buttonParams.addRule(RelativeLayout.ALIGN_LEFT, lastPayerId);
			buttonParams.addRule(RelativeLayout.BELOW, lastPayerId);
		}
		addPayerButton.setLayoutParams(buttonParams);

		// set the remove payer button to only be enabled if there's more than one payer
		Button removePayerButton = (Button) findViewById(R.id.remove_payer);
		removePayerButton.setEnabled(payerIds.size() > 1);
	}

	public void next(View view) {
		// set the background color of all fields to transparent
		for (int payerId : payerIds) {
			EditText payerField = (EditText) findViewById(payerId);
			payerField.setBackgroundColor(0x00000000);
		}

		// get the names of the payers, and make sure they're all specified and unique
		Set<String> payerNames = new LinkedHashSet<String>();
		boolean success = true;
		for (int payerId : payerIds) {
			EditText payerField = (EditText) findViewById(payerId);
			CharSequence payerName = payerField.getText();
			if (payerName.length() == 0) {
				payerName = payerField.getHint();
				payerName = payerName.subSequence(1, payerName.length() - 1);
			}

			// add the name to the result... if it was already there, then:
			if (!payerNames.add(payerName.toString())) {
				Log.i(TAG, "name already present: " + payerName);

				// set the background color of the field to a light red
				payerField.setBackgroundColor(0xffffbbbb);

				// continue, but don't finish
				success = false;
			}
		}
		if (!success) {
			return;
		}

		// log em
		int i = 0;
		for (String payerName : payerNames) {
			Log.i(TAG, ++i + ". " + payerName);
		}

		ReceiptManager.getInstance().setReceipt(new Receipt(new ArrayList<String>(payerNames)));

		Intent intent = new Intent(getApplicationContext(), ItemsActivity.class);
		startActivity(intent);
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PayersFragment extends Fragment {

		public PayersFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_payers, container, false);
			return rootView;
		}

		public void onActivityCreated(Bundle savedInstanceState) {
			super.onActivityCreated(savedInstanceState);

			payerIds = new ArrayList<Integer>();
			
			// create the first payer
			getActivity().findViewById(R.id.add_payer).performClick();
		}
	}

}
