package com.example.Less3;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends Activity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
	}

	public void onClickButtonTranslate(View v) {
		String text = ((EditText) findViewById(R.id.editText)).getText().toString();
		AsyncTaskTranslator taskTranslator = new AsyncTaskTranslator();
		taskTranslator.execute(text);
		AsyncTaskImage taskImage = new AsyncTaskImage();
		taskImage.execute(text);
	}

	private void setImages(ArrayList<Bitmap> bitmaps){
		GridView gridView = (GridView) findViewById(R.id.gridview);
		if(bitmaps == null){
			gridView.setNumColumns(1);
			bitmaps = new ArrayList<Bitmap>();
			Toast.makeText(this, "Error occurred while receiving images", Toast.LENGTH_LONG).show();
		}
		if(bitmaps.size() == 0){
			Toast.makeText(this, "Images weren't found", Toast.LENGTH_LONG).show();
		}
		int px = (int)(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 200, getResources().getDisplayMetrics()));
		gridView.setAdapter(new ImageAdapter(this, bitmaps, px, px, 0));
	}


	public class AsyncTaskTranslator extends AsyncTask<String, Void, String> {
		private static final String KEY = "trnsl.1.1.20140108T215534Z.181880cb79f09bf9.645a3128b6a4193f650069cdf93cd6c2043db227";

		@Override
		protected String doInBackground(String... strings) {
			String query, result = "";
			int from , to;
			URL url;
			URLConnection connection;
			Scanner scanner;
			try {
				query = "https://translate.yandex.net/api/v1.5/tr/translate?key="+
						KEY +
						"&text=" +
						URLEncoder.encode(strings[0], "ISO-8859-1") +
						"&lang=en-ru";

				url = new URL(query);
				connection = url.openConnection();
				connection.setConnectTimeout(10000);
				connection.setReadTimeout(10000);
				scanner = new Scanner(connection.getInputStream());
				while (scanner.hasNext()) {
					result += scanner.nextLine();
				}

				from = result.indexOf("<text>");
				to = result.indexOf("</text>");
				if (from == -1) {
					throw new UnsupportedEncodingException();
				}

				return result.substring(from + 6, to);
			}

			catch (UnsupportedEncodingException e) {
				return "Wrong data";
			}
			catch (IOException e) {
				return "Disconnect server";
			}
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			TextView textView = (TextView) findViewById(R.id.textView);
			textView.setText(result);
		}
	}


	public class AsyncTaskImage extends AsyncTask<String, Void, ArrayList<Bitmap>>{
		private Bitmap getBitmap(String url) throws IOException {
			InputStream iStream = new URL(url).openStream();
			return BitmapFactory.decodeStream(iStream);
		}

		@Override
		protected ArrayList<Bitmap> doInBackground(String... strings) {
			String query, result = "";
			ArrayList<Bitmap> bitmaps;
			Matcher matcher;

			try {
				bitmaps = new ArrayList<Bitmap>();
				query = "http://images.yandex.ru/yandsearch?text=" +
						URLEncoder.encode(strings[0], "ISO-8859-1");
				URL url = new URL(query);
				URLConnection connection = url.openConnection();
				connection.setConnectTimeout(10000);
				connection.setReadTimeout(10000);
				Scanner scanner = new Scanner(connection.getInputStream());
				while (scanner.hasNext()) {
					result += scanner.nextLine();
				}
				matcher = Pattern
						.compile("<img class=\"[^\"]*preview[^\"]*\" (?:alt=\".*?\" )*src=\"(.*?)\"")
						.matcher(result);
				for(int i = 0; i < 9; i++){
					if(!matcher.find()){
						break;
					}
					try{
						bitmaps.add(getBitmap(matcher.group(1)));
					} catch (IOException e){
						--i;
					}
				}
				return bitmaps;
			} catch (IOException e) {
				return null;
			}
		}

		@Override
		protected void onPostExecute(ArrayList<Bitmap> result) {
			super.onPostExecute(result);
			setImages(result);
		}
	}
}
