package com.justthetip.activity;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.method.DigitsKeyListener;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

import com.justthetip.R;
import com.justthetip.data.Receipt;
import com.justthetip.data.ReceiptManager;
import com.justthetip.data.TipCalculation;
import com.justthetip.exception.ValidationException;
import com.justthetip.util.Currency;

public class TipActivity extends ActionBarActivity {

	private List<Integer> tipIds = new ArrayList<Integer>();
	private List<Integer> tippedTotalIds = new ArrayList<Integer>();
	private int grandTotalId;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_tip);

		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction().add(R.id.container, new TipFragment()).commit();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.tip, menu);
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

	private void initTotal() {
//		int total = ReceiptManager.getInstance().getReceipt().getTotal();
//		TextView totalField = (TextView) this.findViewById(R.id.total);
//		totalField.setText(Currency.formatCurrency(total));
	}
	
	private void initPayers() {
		// get the layout to add things to
		final RelativeLayout layout = (RelativeLayout) this.findViewById(R.id.tip_layout);
		
		// get the density screen density for setting margins 
		final float density = this.getResources().getDisplayMetrics().density;
		
		// get the receipt for tip calculations
		final Receipt receipt = ReceiptManager.getInstance().getReceipt();
		
		// get the default tip
		final EditText defaultTipPercentField = (EditText) this.findViewById(R.id.default_tip_percent);
		final String defaultTipText = getTip(defaultTipPercentField);
		double defaultTip;
		try {
			defaultTip = Double.parseDouble(defaultTipText) / 100.0;
		} catch (NumberFormatException ex) {
			// tip is not in the right format... assume it's 0.
			defaultTip = 0.0;
		}
		
		defaultTipPercentField.addTextChangedListener(new TextWatcher() {
			@Override
			public void afterTextChanged(Editable s) {
				// do nothing
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int before, int count) {
				// do nothing
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				String tipPercentString;
				try {
					tipPercentString = s.toString();
					Double.parseDouble(tipPercentString);
				} catch (NumberFormatException ex) {
					tipPercentString = defaultTipPercentField.getHint().toString();
				}
				
				for (final Integer tipId : tipIds) {
					final TextView tipField = (TextView) TipActivity.this.findViewById(tipId);
					tipField.setHint(tipPercentString);
				}
				
				for (final Integer tipId : tipIds) {
					final TextView tipField = (TextView) TipActivity.this.findViewById(tipId);
					// fire the change event
					tipField.setText(tipField.getText());
				}
				
				updateGrandTotal();
			}
			
		});
		
		// get the default tip text field
		final TextView defaultTipTextField = (TextView) this.findViewById(R.id.default_tip_text);
		
		// keep track of the last field to put things underneath
		View lastField = defaultTipPercentField;
		
		final List<String> payerNames = ReceiptManager.getInstance().getReceipt().getPayerNames();
		
		int id = 1;
		
		for (final String payerName : payerNames) {
			
			// payer name
			final TextView payerNameField = new TextView(this);
			payerNameField.setText(payerName);
			payerNameField.setTextAppearance(payerNameField.getContext(), android.R.style.TextAppearance_Large);
			payerNameField.setId(id++);
			final LayoutParams payerNameParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			payerNameParams.addRule(RelativeLayout.ALIGN_RIGHT, defaultTipPercentField.getId());
			payerNameParams.addRule(RelativeLayout.BELOW, lastField.getId());
			payerNameParams.setMargins(0, Math.round(50 * density), 0, 0);
			payerNameField.setLayoutParams(payerNameParams);
			layout.addView(payerNameField);
			
			final TipCalculation calc = receipt.getTipCalculation(payerName, defaultTip);
			int taxedTotal = calc.getTaxedTotal();
			//int pretipTotal = calc.getPretipTotal();
			int actualTip = calc.getTip();
			int tippedTotal = calc.getTippedTotal();
			
			// total
			final TextView totalField = new TextView(this);
			totalField.setTextAppearance(totalField.getContext(), android.R.style.TextAppearance_Medium);
			totalField.setText(Currency.formatCurrency(taxedTotal));
			totalField.setId(id++);
			final LayoutParams totalParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			totalParams.addRule(RelativeLayout.ALIGN_RIGHT, defaultTipPercentField.getId());
			totalParams.addRule(RelativeLayout.BELOW, payerNameField.getId());
			totalField.setLayoutParams(totalParams);
			layout.addView(totalField);
			
			// total text
			final TextView totalTextField = new TextView(this);
			totalTextField.setTextAppearance(totalTextField.getContext(), android.R.style.TextAppearance_Medium);
			totalTextField.setText("Total w/ tax");
			totalTextField.setId(id++);
			final LayoutParams totalTextParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			totalTextParams.addRule(RelativeLayout.ALIGN_LEFT, defaultTipTextField.getId());
			totalTextParams.addRule(RelativeLayout.ALIGN_BASELINE, totalField.getId());
			totalTextField.setLayoutParams(totalTextParams);
			layout.addView(totalTextField);
			
			// tip field
			final EditText tipField = new EditText(this);
			tipField.setHint(defaultTipText);
			tipField.setSingleLine();
			tipField.setEms(4);
			tipField.setGravity(Gravity.RIGHT);
			tipField.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);
			tipField.setKeyListener(DigitsKeyListener.getInstance(false, true));
			tipField.setId(id++);
			final LayoutParams tipParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			tipParams.addRule(RelativeLayout.ALIGN_RIGHT, defaultTipPercentField.getId());
			tipParams.addRule(RelativeLayout.BELOW, totalField.getId());
			tipField.setLayoutParams(tipParams);
			layout.addView(tipField);
			tipIds.add(tipField.getId());
			
			// "%"
			final TextView percentField = new TextView(this);
			percentField.setText("%");
			percentField.setTextAppearance(percentField.getContext(), android.R.style.TextAppearance_Medium);
			percentField.setId(id++);
			final LayoutParams percentParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			percentParams.addRule(RelativeLayout.ALIGN_BASELINE, tipField.getId());
			percentParams.addRule(RelativeLayout.RIGHT_OF, tipField.getId());
			percentField.setLayoutParams(percentParams);
			layout.addView(percentField);
			
			// "x"
			final TextView timesField = new TextView(this);
			timesField.setText("x");
			timesField.setTextAppearance(timesField.getContext(), android.R.style.TextAppearance_Medium);
			timesField.setId(id++);
			final LayoutParams timesParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			timesParams.addRule(RelativeLayout.ALIGN_BASELINE, tipField.getId());
			timesParams.addRule(RelativeLayout.LEFT_OF, tipField.getId());
			timesField.setLayoutParams(timesParams);
			layout.addView(timesField);
			
			// tip text
			final TextView tipTextField = new TextView(this);
			tipTextField.setTextAppearance(tipTextField.getContext(), android.R.style.TextAppearance_Medium);
			tipTextField.setText("Tip %");
			tipTextField.setId(id++);
			final LayoutParams tipTextParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			tipTextParams.addRule(RelativeLayout.ALIGN_LEFT, defaultTipTextField.getId());
			tipTextParams.addRule(RelativeLayout.ALIGN_BASELINE, tipField.getId());
			tipTextField.setLayoutParams(tipTextParams);
			layout.addView(tipTextField);
			
			// actual tip
			final TextView actualTipField = new TextView(this);
			actualTipField.setTextAppearance(actualTipField.getContext(), android.R.style.TextAppearance_Medium);
			actualTipField.setText(Currency.formatCurrency(actualTip));
			actualTipField.setId(id++);
			final LayoutParams actualTipParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			actualTipParams.addRule(RelativeLayout.ALIGN_RIGHT, defaultTipPercentField.getId());
			actualTipParams.addRule(RelativeLayout.BELOW, tipField.getId());
			actualTipField.setLayoutParams(actualTipParams);
			layout.addView(actualTipField);
			
			// tip text
			final TextView actualTipTextField = new TextView(this);
			actualTipTextField.setTextAppearance(actualTipTextField.getContext(), android.R.style.TextAppearance_Medium);
			actualTipTextField.setText("Actual Tip");
			actualTipTextField.setId(id++);
			final LayoutParams actualTipTextParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			actualTipTextParams.addRule(RelativeLayout.ALIGN_LEFT, defaultTipTextField.getId());
			actualTipTextParams.addRule(RelativeLayout.ALIGN_BASELINE, actualTipField.getId());
			actualTipTextField.setLayoutParams(actualTipTextParams);
			layout.addView(actualTipTextField);
			
			// total with tip
			final TextView totalWithTipField = new TextView(this);
			totalWithTipField.setTextAppearance(totalWithTipField.getContext(), android.R.style.TextAppearance_Medium);
			totalWithTipField.setText(Currency.formatCurrency(tippedTotal));
			totalWithTipField.setId(id++);
			final LayoutParams totalWithTipParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			totalWithTipParams.addRule(RelativeLayout.ALIGN_RIGHT, defaultTipPercentField.getId());
			totalWithTipParams.addRule(RelativeLayout.BELOW, actualTipField.getId());
			totalWithTipField.setLayoutParams(totalWithTipParams);
			layout.addView(totalWithTipField);
			tippedTotalIds.add(totalWithTipField.getId());
			
			// "= "
			final TextView equalField = new TextView(this);
			equalField.setTextAppearance(equalField.getContext(), android.R.style.TextAppearance_Medium);
			equalField.setText("= ");
			equalField.setId(id++);
			final LayoutParams equalParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			equalParams.addRule(RelativeLayout.ALIGN_BASELINE, totalWithTipField.getId());
			equalParams.addRule(RelativeLayout.LEFT_OF, totalWithTipField.getId());
			equalField.setLayoutParams(equalParams);
			layout.addView(equalField);
			tippedTotalIds.add(equalField.getId());
			
			// tip text
			final TextView totalWithTipTextField = new TextView(this);
			totalWithTipTextField.setTextAppearance(totalWithTipTextField.getContext(), android.R.style.TextAppearance_Medium);
			totalWithTipTextField.setText("Final Total");
			totalWithTipTextField.setId(id++);
			final LayoutParams totalWithTipTextParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			totalWithTipTextParams.addRule(RelativeLayout.ALIGN_LEFT, defaultTipTextField.getId());
			totalWithTipTextParams.addRule(RelativeLayout.ALIGN_BASELINE, totalWithTipField.getId());
			totalWithTipTextField.setLayoutParams(totalWithTipTextParams);
			layout.addView(totalWithTipTextField);
			
			
			// attach events:
			tipField.addTextChangedListener(new TextWatcher() {
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
					// determine the tip, or default to 0 if not parseable
					double tipPercent;
					try {
						tipPercent = Double.parseDouble(s.toString()) / 100.0;
					} catch (NumberFormatException ex) {
						try {
							tipPercent = Double.parseDouble(tipField.getHint().toString()) / 100.0;
						} catch (NumberFormatException ex2) {
							tipPercent = 0.0;
						}
					}
					
					TipCalculation calc = receipt.getTipCalculation(payerName, tipPercent);
					int actualTip = calc.getTip();
					int tippedTotal = calc.getTippedTotal();
					
					actualTipField.setText(Currency.formatCurrency(actualTip));
					totalWithTipField.setText(Currency.formatCurrency(tippedTotal));
					
					updateGrandTotal();
				}
			});
			
			lastField = totalWithTipField;
		}
		
		// grand total
		TextView grandTotalField = new TextView(this);
		grandTotalField.setTextAppearance(grandTotalField.getContext(), android.R.style.TextAppearance_Medium);
		grandTotalField.setId(id++);
		LayoutParams grandTotalParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		grandTotalParams.addRule(RelativeLayout.ALIGN_RIGHT, defaultTipPercentField.getId());
		grandTotalParams.addRule(RelativeLayout.BELOW, lastField.getId());
		grandTotalParams.setMargins(0, Math.round(50 * density), 0, 0);
		grandTotalField.setLayoutParams(grandTotalParams);
		layout.addView(grandTotalField);
		grandTotalId = grandTotalField.getId();
		
		// grand total text
		TextView grandTotalTextField = new TextView(this);
		grandTotalTextField.setTextAppearance(grandTotalTextField.getContext(), android.R.style.TextAppearance_Medium);
		grandTotalTextField.setText("Grand Total");
		grandTotalTextField.setId(id++);
		LayoutParams grandTotalTextParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		grandTotalTextParams.addRule(RelativeLayout.ALIGN_LEFT, defaultTipTextField.getId());
		grandTotalTextParams.addRule(RelativeLayout.ALIGN_BASELINE, grandTotalField.getId());
		grandTotalTextField.setLayoutParams(grandTotalTextParams);
		layout.addView(grandTotalTextField);
		
		updateGrandTotal();
	}
	
	private void updateGrandTotal() {
		// make sure the grand total field exists first
		if (grandTotalId == 0) return;
		
		int grandTotal = 0;
		for (final Integer tippedTotalId : tippedTotalIds) {
			final TextView tippedTotalField = (TextView) this.findViewById(tippedTotalId);
			try {
				final int total = Currency.parseCurrency(tippedTotalField.getText());
				grandTotal += total;
			} catch (ValidationException e) {
				// on an exception, skip this total
			}
		}
		
		final TextView grandTotalField = (TextView) this.findViewById(grandTotalId);
		grandTotalField.setText(Currency.formatCurrency(grandTotal));
	}

	private void updateTotals() {
//		// get the relevant fields
//		EditText defaultTipPercentField = (EditText) this.findViewById(R.id.default_tip_percent);
//		TextView totalWithTipField = (TextView) this.findViewById(R.id.total_with_tip);
//
//		updateTotalWithTip(defaultTipPercentField, totalWithTipField, null);
//		
//		Receipt receipt = ReceiptManager.getInstance().getReceipt();
//		List<String> payerNames = receipt.getPayerNames();
//		for (String payerName : payerNames) {
//			// TODO: update the tip hint 
//			// TODO: updateTotalWithTip(tipField, totalWithTipField, payerName); 
//		}
	}

//	private void updateTotalWithTip(EditText tipField, TextView totalWithTipField, String payerName) {
//		// get the tip value
//		double tip;
//		try {
//			String tipText = getTip(tipField);
//			tip = Double.parseDouble(tipText) / 100.0;
//		} catch (NumberFormatException ex) {
//			// tip is not in the right format... assume it's 0.
//			tip = 0.0;
//		}
//		
//		// determine the total
//		Receipt receipt = ReceiptManager.getInstance().getReceipt();
//		int totalWithTip;
//		if (payerName != null) {
//			totalWithTip = receipt.getTotalWithTip(payerName, tip);
//		} else {
//			totalWithTip = receipt.getTotalWithTip(tip);
//		}
//		
//		// fill in the total with tip
//		totalWithTipField.setText(Currency.formatCurrency(totalWithTip));
//	}
	
	private String getTip(EditText tipField) {
		String tipText = tipField.getText().toString();
		if (tipText.length() == 0) {
			tipText = tipField.getHint().toString();
		}
		return tipText;
	}
	
	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class TipFragment extends Fragment {

		public TipFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_tip, container, false);
			return rootView;
		}

		public void onActivityCreated(Bundle savedInstanceState) {
			super.onActivityCreated(savedInstanceState);

			TipActivity activity = (TipActivity) getActivity();
			activity.initTotal();
			activity.initPayers();
			activity.updateTotals();

			TextView defaultTipField = (TextView) activity.findViewById(R.id.default_tip_percent);
			defaultTipField.addTextChangedListener(new TextWatcher() {
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
					TipActivity activity = (TipActivity) getActivity();
					activity.updateTotals();
				}
			});
		}
	}

}
