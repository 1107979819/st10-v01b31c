package com.yuneec.flightmode15;

import android.content.Context;
import android.os.AsyncTask;
import com.yuneec.uartcontroller.UARTController;

public class SyncModelDataTask extends AsyncTask<Long, Void, Boolean> {
    private Context mContext;
    private UARTController mController;
    private SyncModelDataCompletedAction onCompletedAction;

    public interface SyncModelDataCompletedAction {
        void SyncModelDataCompleted();
    }

    public SyncModelDataTask(Context mContext, UARTController mController, SyncModelDataCompletedAction onCompletedAction) {
        this.mContext = mContext;
        this.onCompletedAction = onCompletedAction;
        this.mController = mController;
    }

    protected Boolean doInBackground(Long... params) {
        if (this.mContext == null || this.mController == null) {
            return Boolean.valueOf(false);
        }
        long model_id = params[0].longValue();
        if (model_id != -2) {
            Utilities.sendAllDataToFlightControl(this.mContext, model_id, this.mController);
            Utilities.sendRxResInfoFromDatabase(this.mContext, this.mController, model_id);
        }
        return Boolean.valueOf(true);
    }

    protected void onPostExecute(Boolean result) {
        super.onPostExecute(result);
        if (this.onCompletedAction != null) {
            this.onCompletedAction.SyncModelDataCompleted();
        }
    }
}
