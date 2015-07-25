package com.example.fukuharakazuya.myapplication;

import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.app.Activity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;


public class MainActivity extends Activity implements SensorEventListener {

    // テキスト(歩数、消費カロリー）
    private TextView steps;
    private TextView calo;

    //   画像
    private ImageView meal;

    //    センサー系
    private SensorManager mSensorManager;
    private Sensor mStepDetectorSensor;
    private Sensor mStepConterSensor;

    //    各種数値
    private int stepsCount = 0;
    private int caloCount = 0;

    //   画像ボタン
    private ImageButton reset;

    //    初期化フラグ
    private int resetCount = 0;

    //   初期化をした際に値を保管する変数
    private int everSteps = 0;

    //    デバッグフラグ
    private int debug = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//       デバッグ時以外はコメントアウトする
//        debug = 1;

        findViews();

        initial();

        clickButton();

        //センサーマネージャを取得
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        //センサマネージャから TYPE_STEP_DETECTOR についての情報を取得する
        mStepDetectorSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);

        //センサマネージャから TYPE_STEP_COUNTER についての情報を取得する
        mStepConterSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);

    }


    // リソースの取得をするメソッド
    private void findViews() {

        // テキスト
        steps = (TextView) findViewById(R.id.stepsCount);
        calo = (TextView) findViewById(R.id.caloCount);

//        画像
        meal = (ImageView) findViewById(R.id.meal);

//        画像ボタン
        reset = (ImageButton) findViewById(R.id.reset);
    }

    //    初期処理
    protected void initial() {

        everSteps = call("everSteps");

        resetCount = call("resetCount");
    }


    @Override
    public void onSensorChanged(SensorEvent event) {

        Sensor sensor = event.sensor;

        float[] values = event.values;

        //TYPE_STEP_COUNTER
        if (sensor.getType() == Sensor.TYPE_STEP_COUNTER) {

////            デバッグ処理
//            if (debug == 1) {
//
////                下記に試したいカロリーを入力する
//                caloCount = 61;
//
//                calo.setText(String.valueOf(caloCount));
//
//                changeImage(caloCount);
//
//                return;
//
//            }


            // リセットボタンが押下されたの処理(「everSteps = 現在の総歩数」にするのが鍵)
            if (resetCount == 1) {

                everSteps = (int) values[0];

                save("everSteps", everSteps);

                caloCount = 0;

//             リセットボタンの状態を元に戻す
                resetCount = 0;

                save("resetCount", resetCount);
            }

            stepsCount = (int) values[0] - everSteps;

            save("stepsCount", stepsCount);

//            画面に歩数を表示する
            steps.setText(String.valueOf(stepsCount));

//            30歩以上の場合カロリーを計算し表示する(30歩 = 1kcal)
            if (stepsCount > 30) {

                caloCount = stepsCount / 30;

                save("caloCount", caloCount);

                calo.setText(String.valueOf(caloCount));
                Log.i("calo", String.valueOf(caloCount));

                changeImage(caloCount);

//                30歩以下の場合水の画像をセットする
            } else {

                meal.setImageResource(R.mipmap.water);

            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    //    リスナーを設定する
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this,
                mStepConterSensor,
                SensorManager.SENSOR_DELAY_NORMAL);

        mSensorManager.registerListener(this,
                mStepDetectorSensor,
                SensorManager.SENSOR_DELAY_NORMAL);
    }

//    リスナーを解除する（バックグラウンドで実行させたいためコメントアウト）
//    protected void onPausese() {
//        super.onPause();
//        mSensorManager.unregisterListener(this, mStepConterSensor);
//        mSensorManager.unregisterListener(this, mStepDetectorSensor);
//    }

    //    画像を変更すメソッド
    protected void changeImage(int calo) {

        if (calo > 500) {

            meal.setImageResource(R.mipmap.noodle);

        } else if (calo > 400) {

            meal.setImageResource(R.mipmap.potato);

        } else if (calo > 300) {

            meal.setImageResource(R.mipmap.applepie);

        } else if (calo > 200) {

            meal.setImageResource(R.mipmap.dango);

        } else if (calo > 100) {

            meal.setImageResource(R.mipmap.corn);

        } else if (calo > 60) {

            meal.setImageResource(R.mipmap.tounyu);

        } else if (calo > 30) {

            meal.setImageResource(R.mipmap.lemon);

        } else {
            meal.setImageResource(R.mipmap.water);
        }

    }

    // ボタン押下時の処理
    protected void clickButton() {

//       初期ボタン（鳥）押下時
        reset.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                stepsCount = 0;

                caloCount = 0;

                steps.setText(String.valueOf(stepsCount));

                calo.setText(String.valueOf(caloCount));

//                リセットボタン押下のフラグを立てる
                resetCount = 1;

//                直後にアプリを終了された場合を考慮してフラグを保存しておく
                save("resetCount", resetCount);

                meal.setImageResource(R.mipmap.water);


            }
        });
    }

    // 値を保存するメソッド
    public void save(String key, int i) {
        // getDefaultSharedPreferencesメソッドを使ってSharedPreferencesクラスのインスタンスを取得する
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(this);

        // SharedPreferencesクラスのeditメソッドを使ってEditorクラスのインスタンスを取得する
        SharedPreferences.Editor editor = sharedPreferences.edit();

        // EditorクラスのputIntメソッドなどでキーと値を指定して保存する
        editor.putInt(key, i);

        // Editorクラスのcommitメソッドで保存を完了する
        editor.commit();
    }

    // 保存した値を取り出すメソッド
    public int call(String key) {

        // getDefaultSharedPreferencesメソッドを使ってSharedPreferencesクラスのインスタンスを取得する
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(this);

        // SharedPreferencesクラスのget○○メソッドなどでキーを指定して値を読み出す
        int i = sharedPreferences.getInt(key, 0);

        return i;
    }


}