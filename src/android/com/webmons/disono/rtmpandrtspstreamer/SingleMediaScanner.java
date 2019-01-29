package com.webmons.disono.rtmpandrtspstreamer;

import android.content.Context;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.media.MediaScannerConnection.MediaScannerConnectionClient;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import java.io.File;

/**
 * Author: Archie, Disono (webmonsph@gmail.com)
 * Website: https://webmons.com
 * 
 * Created at: 1/09/2018
 */

public class SingleMediaScanner implements MediaScannerConnectionClient {

  private MediaScannerConnection mMs;
  private File mFile;
    private Context mContext;

  public SingleMediaScanner(Context context, File f) {
      mContext = context;
      mFile = f;
      mMs = new MediaScannerConnection(context, this);
      mMs.connect();
  }

  @Override
  public void onMediaScannerConnected() {
      mMs.scanFile(mFile.getAbsolutePath(), "video/*");
  }

  @Override
  public void onScanCompleted(String path, Uri uri) {
      Log.i("SingleMediaScanner", path);
      Log.i("SingleMediaScanner", uri.getPath());

      mMs.disconnect();

      MediaScannerConnection.scanFile(mContext, new String[] { mFile.getAbsolutePath() }, new String[] { "video/*" }, null);

      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
      {
          Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
          mediaScanIntent.setData(Uri.fromFile(mFile));
          mContext.sendBroadcast(mediaScanIntent);
      }
      else
      {
          mContext.sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse(mFile.getAbsolutePath())));
      }
  }

}
