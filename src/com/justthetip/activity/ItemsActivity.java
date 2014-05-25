package com.justthetip.activity;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.text.InputType;
import android.text.method.DigitsKeyListener;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

import com.justthetip.R;
import com.justthetip.data.Item;
import com.justthetip.data.Receipt;
import com.justthetip.data.ReceiptManager;
import com.justthetip.exception.ValidationException;
import com.justthetip.util.Currency;
import com.justthetip.widget.MultiSelectSpinner;

public class ItemsActivity extends ActionBarActivity {

	private static final String TAG = ItemsActivity.class.getSimpleName();

	private static Stack<RowIds> rowIdsStack = new Stack<RowIds>();

	private static class RowIds {
		private int startId;

		public RowIds(int startId) {
			this.startId = startId;
		}

		public int getDollarId() {
			return startId;
		}

		public int getCostId() {
			return startId + 1;
		}

		public int getDescriptionId() {
			return startId + 2;
		}

		public int getPayersId() {
			return startId + 3;
		}

		public int getMaxId() {
			return getPayersId();
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_items);

		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction().add(R.id.container, new ItemsFragment()).commit();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.items, menu);
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

	public void addItem(View view) {
		RelativeLayout layout = (RelativeLayout) this.findViewById(R.id.items_layout);

		// get the list of payers
		List<String> payerNames = ReceiptManager.getInstance().getReceipt().getPayerNames();

		RowIds lastRowIds = !rowIdsStack.isEmpty() ? rowIdsStack.peek() : null;
		int startId = lastRowIds != null ? lastRowIds.getMaxId() + 1 : 1;
		RowIds rowIds = new RowIds(startId);
		rowIdsStack.add(rowIds);

		// log the payers (for debugging)
		int i = 0;
		for (String payerName : payerNames) {
			Log.i(TAG, ++i + ". " + payerName);
		}

		// create a text item for "$"
		TextView dollarSign = new TextView(ItemsActivity.this);
		dollarSign.setId(rowIds.getDollarId());
		dollarSign.setText("$");
		dollarSign.setTextAppearance(view.getContext(), android.R.style.TextAppearance_Large);

		// create an EditText for cost
		EditText cost = new EditText(ItemsActivity.this);
		cost.setId(rowIds.getCostId());
		cost.setHint("XX.YY");
		cost.setSingleLine();
		cost.setGravity(Gravity.RIGHT);
		cost.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);
		cost.setKeyListener(DigitsKeyListener.getInstance(true, true));

		// create an EditText for description
		EditText description = new EditText(ItemsActivity.this);
		description.setId(rowIds.getDescriptionId());
		description.setHint("(Description)");
		description.setSingleLine();

		// create a multi-spinner for payers
		MultiSelectSpinner payers = new MultiSelectSpinner(ItemsActivity.this, "Payers");
		payers.setId(rowIds.getPayersId());
		payers.setItems(payerNames);

		// Set the layout params for the new row
		LayoutParams dollarParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		LayoutParams costParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		LayoutParams descriptionParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		LayoutParams payersParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		if (lastRowIds != null) {
			descriptionParams.addRule(RelativeLayout.BELOW, lastRowIds.getDescriptionId());
			descriptionParams.addRule(RelativeLayout.ALIGN_LEFT, lastRowIds.getDescriptionId());
		}

		descriptionParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
		descriptionParams.addRule(RelativeLayout.RIGHT_OF, rowIds.getCostId());
		descriptionParams.addRule(RelativeLayout.LEFT_OF, rowIds.getPayersId());

		dollarParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
		dollarParams.addRule(RelativeLayout.ALIGN_BASELINE, rowIds.getCostId());
		costParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
		costParams.addRule(RelativeLayout.ALIGN_BASELINE, rowIds.getDescriptionId());
		costParams.addRule(RelativeLayout.RIGHT_OF, rowIds.getDollarId());
		// costParams.addRule(RelativeLayout.RIGHT_OF, rowIds.getDollarId());
		// payersParams.addRule(RelativeLayout.ALIGN_BOTTOM, rowIds.getDescriptionId());
		payersParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		payersParams.addRule(RelativeLayout.ALIGN_BASELINE, rowIds.getDescriptionId());

		dollarSign.setLayoutParams(dollarParams);
		cost.setLayoutParams(costParams);
		description.setLayoutParams(descriptionParams);
		payers.setLayoutParams(payersParams);

		// add the new row
		layout.addView(dollarSign);
		layout.addView(cost);
		layout.addView(description);
		layout.addView(payers);

		// modify the add items button's relative position to be below the new text field
		Button addItemButton = (Button) findViewById(R.id.add_item);
		LayoutParams buttonParams = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		buttonParams.addRule(RelativeLayout.BELOW, rowIds.getDescriptionId());
		buttonParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
		addItemButton.setLayoutParams(buttonParams);

		// set the remove item button to only be enabled if there's more than one item
		Button removeItemButton = (Button) findViewById(R.id.remove_item);
		removeItemButton.setEnabled(rowIdsStack.size() > 1);
	}

	public void removeItem(View view) {
		// make sure we have a payer to remove
		if (rowIdsStack.isEmpty()) {
			return;
		}

		RelativeLayout layout = (RelativeLayout) this.findViewById(R.id.items_layout);

		// remove the last text field
		RowIds rowIds = rowIdsStack.pop();
		layout.removeView(findViewById(rowIds.getDollarId()));
		layout.removeView(findViewById(rowIds.getCostId()));
		layout.removeView(findViewById(rowIds.getDescriptionId()));
		layout.removeView(findViewById(rowIds.getPayersId()));

		// modify the add item button's relative position to be below the last text field, or no text field if none
		Button addItemButton = (Button) findViewById(R.id.add_item);
		LayoutParams buttonParams = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		if (!rowIdsStack.isEmpty()) {
			buttonParams.addRule(RelativeLayout.BELOW, rowIdsStack.peek().getDescriptionId());
			buttonParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
		}
		addItemButton.setLayoutParams(buttonParams);

		// set the remove item button to only be enabled if there's more than one item
		Button removeItemButton = (Button) findViewById(R.id.remove_item);
		removeItemButton.setEnabled(rowIdsStack.size() > 1);
	}

	public void next(View view) {
		Receipt receipt = ReceiptManager.getInstance().getReceipt();

		List<String> payerNames = receipt.getPayerNames();
		List<Item> items = new ArrayList<Item>(rowIdsStack.size());

		// Reset background colors
		for (RowIds rowIds : rowIdsStack) {
			EditText costField = ((EditText) findViewById(rowIds.getCostId()));
			MultiSelectSpinner payerIdsField = ((MultiSelectSpinner) findViewById(rowIds.getPayersId()));

			costField.setBackgroundColor(0x00000000);
			payerIdsField.setBackgroundColor(0x00000000);
		}

		try {
			// Populate the list of items
			for (RowIds rowIds : rowIdsStack) {
				EditText costField = ((EditText) findViewById(rowIds.getCostId()));
				EditText descriptionField = ((EditText) findViewById(rowIds.getDescriptionId()));
				MultiSelectSpinner payerIdsField = ((MultiSelectSpinner) findViewById(rowIds.getPayersId()));

				String costString = costField.getText().toString();
				String description = descriptionField.getText().toString();
				List<Integer> payerIds = payerIdsField.getSelectedIndicies();

				int cost;
				try {
					cost = Currency.parseCurrency(costString);
				} catch (ValidationException ex) {
					costField.setBackgroundColor(0xffffbbbb);
					throw ex;
				}

				List<String> itemPayerNames = new ArrayList<String>(payerIds.size());
				for (Integer payerId : payerIds) {
					if (payerId < 0 || payerId >= payerNames.size()) {
						payerIdsField.setBackgroundColor(0xffffbbbb);
						throw new ValidationException("Something is very wrong with payer list: " + payerIds + " ... " + payerNames);
					}
					itemPayerNames.add(payerNames.get(payerId));
				}
				if (payerIds.isEmpty()) {
					payerIdsField.setBackgroundColor(0xffffbbbb);
					throw new ValidationException("Someone has to pay for '" + description + "'.");
				}
				
				items.add(new Item(cost, description, itemPayerNames));
			}
		} catch (ValidationException ex) {
			// TODO: figure out how to show a dialog... this isn't working
			// Dialog.showError(this, ex.getMessage());
			return;
		}

		// Add all the items we found to the receipt
		receipt.setItems(items);

		// go on to TipActivity
		Intent intent = new Intent(getApplicationContext(), TaxActivity.class);
		startActivity(intent);
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class ItemsFragment extends Fragment {

		public ItemsFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_items, container, false);

			return rootView;
		}

		public void onActivityCreated(Bundle savedInstanceState) {
			super.onActivityCreated(savedInstanceState);

			// create the first payer
			getActivity().findViewById(R.id.add_item).performClick();
		}
	}

}
