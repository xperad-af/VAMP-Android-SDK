package jp.supership.vamp.sample;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import jp.supership.vamp.AdvancedListener;
import jp.supership.vamp.VAMP;
import jp.supership.vamp.VAMPError;
import jp.supership.vamp.VAMPListener;

public class VAMPAdActivity extends BaseActivity {

    /**
     * 広告枠IDを設定してください
     * 59756 : Androidテスト用ID (このIDのままリリースしないでください)
     */
    public static final String VAMP_AD_ID = "59756";

    private VAMP vamp;
    private Button showButton;

    @Override
    protected void onCreateLayout(Bundle savedInstanceState) {
        setContentView(R.layout.activity_vamp_ad);
        setTitle(R.string.vamp_ad1);

        // VAMPインスタンスの取得
        vamp = VAMP.getVampInstance(this, VAMP_AD_ID);
        vamp.setVAMPListener(new AdListener());
        vamp.setAdvancedListener(new AdvListener());

        Button loadButton = (Button) findViewById(R.id.button_load);
        loadButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // 広告の取得を開始
                vamp.load();
                addLog("[LOAD] load()");
            }
        });

        showButton = (Button) findViewById(R.id.button_show);
        showButton.setEnabled(false);
        showButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // 広告の表示準備ができているか確認
                if (vamp.isReady()) {
                    // 動画の準備が完了していた場合
                    // 動画を再生する
                    vamp.show();
                    addLog("[SHOW] show()");
                } else {
                    addLog("[SHOW] isReady:false");
                }
            }
        });

        TextView idTextView = (TextView) findViewById(R.id.vamp_id);
        idTextView.setText("ID:" + VAMP_AD_ID);

        mLogView = (TextView) findViewById(R.id.logs);
    }

    private class AdListener implements VAMPListener {

        @Override
        public void onReceive(String placementId, String adnwName) {
            // 広告表示の準備完了
            // v3.0〜　onLoadResult:successで判定する
        }

        @Override
        public void onFail(String placementId, VAMPError error) {
            // 広告準備or表示失敗
            //
            // deprecated このメソッドは廃止予定です.
            // 代わりにonFailedToLoadおよびonFailedToShowメソッドを使用してください
        }

        @Override
        public void onFailedToLoad(VAMPError error, String placementId) {
            // 広告取得失敗
            // 広告が取得できなかったときに通知されます。
            // 例）在庫が無い、タイムアウトなど
            // @see https://github.com/AdGeneration/VAMP-Android-SDK/wiki/VAMP-Android-API-Errors
            addLog("onFailedToLoad(" + error + ")", Color.RED);
            showButton.setEnabled(false);
        }

        @Override
        public void onFailedToShow(VAMPError error, String placementId) {
            // 広告表示失敗
            // show実行したが、何らかの理由で広告表示が失敗したときに通知されます。
            // 例）ユーザーが広告再生を途中でキャンセルなど
            addLog("onFailedToShow(" + error + ")", Color.RED);
            showButton.setEnabled(false);
        }

        @Override
        public void onComplete(String placementId, String adnwName) {
            // インセンティブ付与OK
            // インセンティブ付与が可能になったタイミング（動画再生完了時、またはエンドカードを閉じたタイミング）で通知
            // アドネットワークによって通知タイミングが異なる
            addLog("onComplete(" + adnwName + ")", Color.BLUE);
        }

        @Override
        public void onClose(String placementId, String adnwName) {
            // 動画プレーヤーやエンドカードが表示終了
            // ＜注意：ユーザキャンセルなども含むので、インセンティブ付与はonCompleteで判定すること＞
            addLog("onClose(" + adnwName + ")", Color.BLACK);
        }

        @Override
        public void onExpired(String placementId) {
            // 有効期限オーバー
            // ＜注意：onReceiveを受けてからの有効期限が切れました。showするには再度loadを行う必要が有ります＞
            addLog("onExpired()", Color.RED);
            showButton.setEnabled(false);
        }
    }

    private class AdvListener implements AdvancedListener {

        @Override
        public void onLoadStart(String placementId, String adnwName) {
            // 優先順にアドネットワークごとの広告取得を開始
            addLog("onLoadStart(" + adnwName + ")");
        }

        @Override
        public void onLoadResult(String placementId, boolean success, String adnwName, String message) {
            // アドネットワークを１つずつ呼び出した結果、広告在庫が取得できたかをsuccessフラグで確認
            if (success) {
                showButton.setEnabled(true);
                addLog("onLoadResult(" + adnwName + ",success:" + success + ")", Color.BLACK);
            } else {
                // 失敗しても、次のアドネットワークがあれば、広告取得を試みます。
                // 最終的に全てのアドネットワークの広告在庫が無ければ
                // onFailedToLoadのNO_ADSTOCKが通知されます。
                addLog("onLoadResult(" + adnwName + ",success:" + success + ") " + message, Color.RED);
            }
        }
    }
}
