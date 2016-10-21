package cn.look;

import cn.look.parallaxlistview.ParallaxListView2;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		ParallaxListView2 lv = (ParallaxListView2) findViewById(R.id.lv);
		String[] data = new String[40];
		for (int i = 0; i < data.length; i++) {
			data[i] = "DATA:: " + i;
		}
		lv.setAdapter(new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, data));
		lv.setParallaxView(View.inflate(getApplicationContext(), R.layout.header, null));
	}
}
