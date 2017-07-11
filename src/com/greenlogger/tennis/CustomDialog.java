package com.greenlogger.tennis;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.greenlogger.tenis.R;

public class CustomDialog extends Dialog {
	public DialogInterface.OnClickListener mBackClickListener = null;

	public CustomDialog(final Context context) {
		super(context);
	}

	public CustomDialog(final Context context, final int theme) {
		super(context, theme);
	}

	@Override
	public boolean onKeyUp(final int keyCode, final KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && mBackClickListener != null) {
			mBackClickListener.onClick(this, DialogInterface.BUTTON_NEUTRAL);
			return true;
		}
		return false;
	}

	public static class Builder {
		private final Context mContext;
		private String mTitle;
		private String mMessage;
		private String mPositiveButtonText = null;;
		private String mNeutralButtonText = null;;
		private String mNegativeButtonText = null;;
		private View mContentView;
		private boolean mImageSet = false;
		private int mImage;

		private DialogInterface.OnClickListener mPositiveButtonClickListener = null;
		private DialogInterface.OnClickListener mNegativeButtonClickListener = null;
		private DialogInterface.OnClickListener mNeutralButtonClickListener = null;

		public Builder(final Context context) {
			mContext = context;
		}

		/** Set the Dialog message from String
		 * 
		 * @param title
		 * @return */
		public Builder setMessage(final String message) {
			mMessage = message;
			return this;
		}

		/** Set the Dialog message from resource
		 * 
		 * @param title
		 * @return */
		public Builder setMessage(final int message) {
			mMessage = (String) mContext.getText(message);
			return this;
		}

		/** Set the Dialog title from resource
		 * 
		 * @param title
		 * @return */
		public Builder setTitle(final int title) {
			mTitle = (String) mContext.getText(title);
			return this;
		}

		/** Set the Dialog title from String
		 * 
		 * @param title
		 * @return */
		public Builder setTitle(final String title) {
			mTitle = title;
			return this;
		}

		/** Set a custom content view for the Dialog. If a message is set, the
		 * contentView is not added to the Dialog...
		 * 
		 * @param v
		 * @return */
		public Builder setContentView(final View v) {
			mContentView = v;
			return this;
		}

		/** Set the positive button resource and it's listener
		 * 
		 * @param positiveButtonText
		 * @param listener
		 * @return */
		public Builder setPositiveButton(final int positiveButtonText,
				final DialogInterface.OnClickListener listener) {
			mPositiveButtonText = (String) mContext.getText(positiveButtonText);
			mPositiveButtonClickListener = listener;
			return this;
		}

		/** Set the positive button text and it's listener
		 * 
		 * @param positiveButtonText
		 * @param listener
		 * @return */
		public Builder setPositiveButton(final String positiveButtonText,
				final DialogInterface.OnClickListener listener) {
			mPositiveButtonText = positiveButtonText;
			mPositiveButtonClickListener = listener;
			return this;
		}

		/** Set the neutral button resource and it's listener
		 * 
		 * @param neutralButtonText
		 * @param listener
		 * @return */
		public Builder setNeutralButton(final int neutralButtonText,
				final DialogInterface.OnClickListener listener) {
			mNeutralButtonText = (String) mContext.getText(neutralButtonText);
			mNeutralButtonClickListener = listener;
			return this;
		}

		/** Set the neutral button text and it's listener
		 * 
		 * @param neutralButtonText
		 * @param listener
		 * @return */
		public Builder setNeutralButton(final String neutralButtonText,
				final DialogInterface.OnClickListener listener) {
			mNeutralButtonText = neutralButtonText;
			mNeutralButtonClickListener = listener;
			return this;
		}

		/** Set the negative button resource and it's listener
		 * 
		 * @param negativeButtonText
		 * @param listener
		 * @return */
		public Builder setNegativeButton(final int negativeButtonText,
				final DialogInterface.OnClickListener listener) {
			mNegativeButtonText = (String) mContext.getText(negativeButtonText);
			mNegativeButtonClickListener = listener;
			return this;
		}

		/** Set the negative button text and it's listener
		 * 
		 * @param negativeButtonText
		 * @param listener
		 * @return */
		public Builder setNegativeButton(final String negativeButtonText,
				final DialogInterface.OnClickListener listener) {
			mNegativeButtonText = negativeButtonText;
			mNegativeButtonClickListener = listener;
			return this;
		}

		public Builder setImage(final int resourceId) {
			mImageSet = true;
			mImage = resourceId;
			return this;
		}
		
		/** Create the custom dialog */
		public CustomDialog create() {
			final LayoutInflater inflater = (LayoutInflater) mContext
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			// instantiate the dialog with the custom Theme
			final CustomDialog dialog = new CustomDialog(mContext,
					R.style.Dialog);
			final View layout = inflater.inflate(R.layout.custom_dialog, null);
			dialog.addContentView(layout, new LayoutParams(
					LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));

			// set image
			if (mImageSet) {
				((ImageView) dialog.findViewById(R.id.image))
						.setImageResource(mImage);
			} else {
				layout.findViewById(R.id.image).setVisibility(View.GONE);
			}
			// set the dialog title
			if (mTitle != null) {
				((TextView) layout.findViewById(R.id.title)).setText(mTitle);
			} else {
				layout.findViewById(R.id.title).setVisibility(View.GONE);
			}
			// set the confirm button
			if (mPositiveButtonText != null) {
				((Button) layout.findViewById(R.id.positiveButton))
						.setText(mPositiveButtonText);
				if (mPositiveButtonClickListener != null) {
					((Button) layout.findViewById(R.id.positiveButton))
							.setOnClickListener(new View.OnClickListener() {
								public void onClick(final View v) {
									mPositiveButtonClickListener.onClick(
											dialog,
											DialogInterface.BUTTON_POSITIVE);
								}
							});
				}
			} else {
				// if no confirm button just set the visibility to GONE
				layout.findViewById(R.id.positiveButton).setVisibility(
						View.GONE);
			}
			// set the neutral button
			if (mNeutralButtonText != null) {
				((Button) layout.findViewById(R.id.neutralButton))
						.setText(mNeutralButtonText);
				if (mNeutralButtonClickListener != null) {
					((Button) layout.findViewById(R.id.neutralButton))
							.setOnClickListener(new View.OnClickListener() {
								public void onClick(final View v) {
									mNeutralButtonClickListener.onClick(dialog,
											DialogInterface.BUTTON_NEUTRAL);
								}
							});
					dialog.mBackClickListener = mNeutralButtonClickListener;
				}
			} else {
				// if no confirm button just set the visibility to GONE
				layout.findViewById(R.id.neutralButton)
						.setVisibility(View.GONE);
			}
			// set the cancel button
			if (mNegativeButtonText != null) {
				((Button) layout.findViewById(R.id.negativeButton))
						.setText(mNegativeButtonText);
				if (mNegativeButtonClickListener != null) {
					((Button) layout.findViewById(R.id.negativeButton))
							.setOnClickListener(new View.OnClickListener() {
								public void onClick(final View v) {
									mNegativeButtonClickListener.onClick(
											dialog,
											DialogInterface.BUTTON_NEGATIVE);
								}
							});
				}
			} else {
				// if no confirm button just set the visibility to GONE
				layout.findViewById(R.id.negativeButton).setVisibility(
						View.GONE);
			}
			// set the content message
			if (mMessage != null) {
				((TextView) layout.findViewById(R.id.text)).setText(mMessage);
			} else if (mContentView != null) {
				// if no message set
				// add the contentView to the dialog body
				((LinearLayout) layout.findViewById(R.id.content_view))
						.removeAllViews();
				((LinearLayout) layout.findViewById(R.id.content_view))
						.addView(mContentView, new LayoutParams(
								LayoutParams.WRAP_CONTENT,
								LayoutParams.WRAP_CONTENT));
			} else {
				layout.findViewById(R.id.content_view).setVisibility(View.GONE);
			}

			dialog.setContentView(layout);
			CustomFont.overrideFonts(mContext, layout);
			return dialog;
		}

	}
}
