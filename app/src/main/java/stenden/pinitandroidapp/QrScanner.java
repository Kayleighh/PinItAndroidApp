package stenden.pinitandroidapp;
import android.os.Bundle;

import com.google.zxing.integration.android.IntentIntegrator;

/**
 * Created by koend on 17-1-2017.
 */

public class QrScanner extends MainActivity {

    @Override
    protected void onCreate(Bundle savedInstance){
        super.onCreate(savedInstance);
        IntentIntegrator scanIntegrator = new IntentIntegrator(this);
        scanIntegrator.initiateScan();
    }
    @Override
    public void onBackPressed(){

    }

}
