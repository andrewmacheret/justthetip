package com.justthetip.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;

public class Dialog {

	/**
	 * Show an error message.
	 * 
	 * @param activity
	 *            The current activity.
	 * @param message
	 *            The error message.
	 */
	public static void showError(Activity activity, String message) {
		// Show a dialog with the error
		AlertDialog.Builder builder = new AlertDialog.Builder(activity);
		builder.setMessage(message);
		builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				dialog.dismiss();
			}
		});
		builder.create();
		return;
	}

}
