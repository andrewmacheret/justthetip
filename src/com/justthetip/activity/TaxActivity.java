package com.justthetip.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.justthetip.R;
import com.justthetip.data.Receipt;
import com.justthetip.data.ReceiptManager;
import com.justthetip.data.SubtotalCalculation;
import com.justthetip.data.TaxCalculation;
import com.justthetip.util.Currency;

public class TaxActivity extends ActionBarActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_tax);

		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction().add(R.id.container, new TaxFragment()).commit();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.tax, menu);
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

	public void taxChanged(View view) {
		updateTotal();
	}

	private void initSubtotal() {
		SubtotalCalculation calc = ReceiptManager.getInstance().getReceipt().getGlobalSubtotalCalculation();
		int subtotal = calc.getSubtotal();
		
		TextView subtotalField = (TextView) this.findViewById(R.id.subtotal);
		subtotalField.setText(Currency.formatCurrency(subtotal));
	}

	private void updateTotal() {
		// get the relevant fields
		EditText taxField = (EditText) this.findViewById(R.id.tax);
		TextView totalField = (TextView) this.findViewById(R.id.total);

		// get the tax value
		double tax;
		try {
			String taxText = getTax(taxField);
			tax = Double.parseDouble(taxText) / 100.0;
		} catch (NumberFormatException ex) {
			// tax is not in the right format... assume it's 0.
			tax = 0.0;
		}

		// determine the total
		Receipt receipt = ReceiptManager.getInstance().getReceipt();
		receipt.setTaxPercent(tax);
		TaxCalculation calc = receipt.getGlobalTaxCalculation();
		int total = calc.getTaxedTotal();

		// fill in the subtotal and total
		totalField.setText(Currency.formatCurrency(total));
	}
	
	public void next(View view) {
		EditText taxField = (EditText) this.findViewById(R.id.tax);
		taxField.setBackgroundColor(0x00000000);
		
		// get the tax value
		double tax;
		try {
			String taxText = getTax(taxField);
			tax = Double.parseDouble(taxText) / 100.0;
		} catch (NumberFormatException ex) {
			// don't allow an invalid tax
			taxField.setBackgroundColor(0xffffbbbb);
			return;
		}
		
		// set the tax
		Receipt receipt = ReceiptManager.getInstance().getReceipt();
		receipt.setTaxPercent(tax);
		
		// continue to the tip activity
		Intent intent = new Intent(getApplicationContext(), TipActivity.class);
		startActivity(intent);
	}
	
	private String getTax(EditText taxField) {
		String taxText = taxField.getText().toString();
		if (taxText.length() == 0) {
			taxText = taxField.getHint().toString();
		}
		return taxText;
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class TaxFragment extends Fragment {

		public TaxFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_tax, container, false);
			return rootView;
		}

		public void onActivityCreated(Bundle savedInstanceState) {
			super.onActivityCreated(savedInstanceState);

			TaxActivity activity = (TaxActivity) getActivity();
			activity.initSubtotal();
			activity.updateTotal();

			TextView taxField = (TextView) activity.findViewById(R.id.tax);
			taxField.addTextChangedListener(new TextWatcher() {
				@Override
				public void afterTextChanged(Editable s) {
					// do nothing
				}
				@Override
				public void beforeTextChanged(CharSequence s, int start, int count, int after) {
					// do nothing
				}
				@Override
				public void onTextChanged(CharSequence s, int start, int before, int count) {
					TaxActivity activity = (TaxActivity) getActivity();
					activity.updateTotal();
				}
			});

		}
	}

}
