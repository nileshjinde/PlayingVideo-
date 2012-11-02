package com.lightcone.playingvideo;

import java.io.File;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.VideoView;

public class PlayingVideo extends Activity implements OnCompletionListener, OnPreparedListener {
	
	static private final String pathToFile = "http://www.youtube.com/watch?v=_AP90lJhLCg&feature=g-logo-xit";  // Video source file
	private VideoView videoPlayer;
	public ProgressDialog mProgressDialog;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        // Find the root of the external storage file system.  We assume the file system is
        // mounted and writable (see the project WriteSDCard for ways to check this).
        
        File root = Environment.getExternalStorageDirectory(); 
        
        // Assign a VideoView object to the video player and set its properties.  It
        // will be started by the onPrepared(MediaPlayer vp) callback below when the
        // file is ready to play.
        
        videoPlayer = (VideoView) findViewById(R.id.videoPlayer);   
        videoPlayer.setOnPreparedListener(this);
        videoPlayer.setOnCompletionListener(this);
        videoPlayer.setKeepScreenOn(true);    
        //videoPlayer.setVideoPath(root + "/" + pathToFile);
      //  videoPlayer.setVideoPath(pathToFile);
        videoPlayer.setVideoURI(Uri.parse(getUrlVideoRTSP(pathToFile)));
        showDialog(0);
    }


public String getUrlVideoRTSP(String urlYoutube)
    {
        try
        {
            String gdy = "http://gdata.youtube.com/feeds/api/videos/";
            DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            String id = extractYoutubeId(urlYoutube);
            URL url = new URL(gdy + id);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            Document doc = documentBuilder.parse(connection.getInputStream());
            Element el = doc.getDocumentElement();
            NodeList list = el.getElementsByTagName("media:content");///media:content
            String cursor = urlYoutube;
            for (int i = 0; i < list.getLength(); i++)
            {
                Node node = list.item(i);
                if (node != null)
                {
                    NamedNodeMap nodeMap = node.getAttributes();
                    HashMap<String, String> maps = new HashMap<String, String>();
                    for (int j = 0; j < nodeMap.getLength(); j++)
                    {
                        Attr att = (Attr) nodeMap.item(j);
                        maps.put(att.getName(), att.getValue());
                    }
                    if (maps.containsKey("yt:format"))
                    {
                        String f = maps.get("yt:format");
                        if (maps.containsKey("url"))
                        {
                            cursor = maps.get("url");
                        }
                        if (f.equals("1"))
                            return cursor;
                    }
                }
            }
            return cursor;
        }
        catch (Exception ex)
        {
            Log.e("Get Url Video RTSP Exception======>>", ex.toString());
        }
        return urlYoutube;

    }
    private String extractYoutubeId(String url) throws MalformedURLException
    {
        String id = null;
        try
        {
            String query = new URL(url).getQuery();
            if (query != null)
            {
                String[] param = query.split("&");
                for (String row : param)
                {
                    String[] param1 = row.split("=");
                    if (param1[0].equals("v"))
                    {
                        id = param1[1];
                    }
                }
            }
            else
            {
                if (url.contains("embed"))
                {
                    id = url.substring(url.lastIndexOf("/") + 1);
                }
            }
        }
        catch (Exception ex)
        {
            Log.e("Exception", ex.toString());
        }
        return id;
    }
    
    /** This callback will be invoked when the file is ready to play */
	@Override
	public void onPrepared(MediaPlayer vp) {
		
		// Don't start until ready to play.  The arg of seekTo(arg) is the start point in
		// milliseconds from the beginning. In this example we start playing 1/5 of
		// the way through the video if the player can do forward seeks on the video.
		
		int iTotalTime=videoPlayer.getDuration();
	//	if(videoPlayer.canSeekForward()) videoPlayer.seekTo(videoPlayer.getDuration()/5);
		if(videoPlayer.canSeekForward()) {
			videoPlayer.seekTo(0);
			videoPlayer.start();
			removeDialog(0);
		}
	}
	
	/** This callback will be invoked when the file is finished playing */
	@Override
	public void onCompletion(MediaPlayer  mp) {
		// Statements to be executed when the video finishes.
		this.finish();	
	}
	
	/**  Use screen touches to toggle the video between playing and paused. */
	@Override
	public boolean onTouchEvent (MotionEvent ev){	
		if(ev.getAction() == MotionEvent.ACTION_DOWN){
			if(videoPlayer.isPlaying()){
				videoPlayer.pause();
			} else {
				videoPlayer.start();
			}
			return true;		
		} else {
			return false;
		}
	}
	
	@Override
	protected Dialog onCreateDialog(int id, Bundle args) {
		ProgressDialog dialog = new ProgressDialog(this);
		dialog = ProgressDialog.show(this,null,null);
		dialog.setContentView(R.layout.loader);
		dialog.setIndeterminate(true);
		dialog.setCancelable(false);
		dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				Log.i("CHECKINFORGOOD", "user cancelling authentication");

			}
		});
		mProgressDialog = dialog;
		return dialog;
	}

}