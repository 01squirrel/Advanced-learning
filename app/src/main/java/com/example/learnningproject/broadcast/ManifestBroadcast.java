package com.example.learnningproject.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import java.security.PrivilegedAction;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

//清单声明的接收器
public class ManifestBroadcast extends BroadcastReceiver {
    private static final String TAG = "MyBroadcastReceive";
    ExecutorService service = Executors.newSingleThreadExecutor();
    Handler handler = new Handler(Looper.myLooper());
    @Override
    public void onReceive(Context context, Intent intent) {
        //使用 goAsync() 来标记它在 onReceive() 完成后需要更多时间才能完成。
        // 如果您希望在 onReceive() 中完成的工作很长，足以导致界面线程丢帧 (>16ms)，
        // 则这种做法非常有用，这使它尤其适用于后台线程。
        final PendingResult pendingResult = goAsync();
        Task task = new Task(pendingResult,intent);
        task.execute();
        String log = "Action:" + intent.getAction() + "\n" +
                "URI:" + intent.toUri(Intent.URI_INTENT_SCHEME) + "\n";
        Log.d(TAG, "onReceive: "+log);
        switch (intent.getAction()){
            case Intent.ACTION_AIRPLANE_MODE_CHANGED:
                Toast.makeText(context,log,Toast.LENGTH_SHORT).show();
            case Intent.ACTION_BATTERY_LOW:
                Toast.makeText(context,"电量低，请连接充电器",Toast.LENGTH_SHORT).show();
        }
        //Toast.makeText(context, log, Toast.LENGTH_LONG).show();
        //new form to replace task
        service.execute(()->{
            String logs = "Action: " + intent.getAction() + "\n" +
                    "URI: " + intent.toUri(Intent.URI_INTENT_SCHEME) + "\n";
            Log.d(TAG, logs);
            handler.post(pendingResult::finish);
        });
    }

    private static class Task extends AsyncTask<String,Integer,String>{

        private final PendingResult pendingIntent;
        private final Intent intent;

        private Task(PendingResult pendingResult, Intent intent) {
            this.pendingIntent = pendingResult;
            this.intent = intent;
        }


        @Override
        protected String doInBackground(String... strings) {
            String log = "Action: " + intent.getAction() + "\n" +
                    "URI: " + intent.toUri(Intent.URI_INTENT_SCHEME) + "\n";
            Log.d(TAG, log);
            return log;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            // Must call finish() so the BroadcastReceiver can be recycled.
            pendingIntent.finish();
        }
    }
}
