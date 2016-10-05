package com.movistar.tvsindesco.cordova.plugin;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.SystemClock;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.text.TextUtils;



import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.EventLogger;
import com.google.android.exoplayer2.drm.DrmSessionManager;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.source.ConcatenatingMediaSource;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroup;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.source.dash.DashMediaSource;
import com.google.android.exoplayer2.source.dash.DefaultDashChunkSource;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.source.smoothstreaming.DefaultSsChunkSource;
import com.google.android.exoplayer2.source.smoothstreaming.SsMediaSource;
import com.google.android.exoplayer2.text.Cue;
import com.google.android.exoplayer2.text.TextRenderer;
import com.google.android.exoplayer2.trackselection.AdaptiveVideoTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.FixedTrackSelection;
import com.google.android.exoplayer2.trackselection.MappingTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.ui.SubtitleView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.upstream.HttpDataSource;
import com.google.android.exoplayer2.util.Util;

import com.movistar.tvsindesco.MainActivity;
import com.videoplayer.VideoPlayer;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.engine.SystemWebView;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.File;
import java.util.List;


/**
 * Created by juan.fernandezfraile on 12/01/2016.
 */
public class VideoPlugin extends CordovaPlugin implements ExoPlayer.EventListener{
    private static final String LOGTAG = "LogsAndroid";
    static Context context = MainActivity.getContext();
    static String userAgent = Util.getUserAgent(context, "mvtv_phone");
    private static VideoPlayer player;
    private static SimpleExoPlayer playerAudio;
    static String urlPlaying;
    public static String LA_URL = "";
    public static String token = "";
    private static EventLogger eventLogger;
    static boolean errorDRM = false;
    static boolean errorNotFileDRM = false;
//    static Surface surface_live = MainActivity.getSurfaceView_live();
    static SurfaceView surfaceView_live = MainActivity.getSurfaceView_live();
    static SystemWebView webView = MainActivity.getWebView();
    static boolean codecMpeg123 = false;
//    static SimpleExoPlayerView simple_view = MainActivity.getSimpleExoplayerView();
    static SubtitleView subtitleLayout;


    private static final TrackSelection.Factory FIXED_FACTORY = new FixedTrackSelection.Factory();
	
    static int PLAYING_LIVE = 0;
    static int PLAYING_RTSP = 1;
    static int PLAYING_VOD = 2;
    static int PLAYING_AUDIO = 3;
    static int PLAYING_DRM = 4;

    static int stateVideo = 0;

    static int currentPlay;


    public static void forcePause() {
        if (player != null) {
            player.release();
        }
        if (playerAudio != null) {
            playerAudio.stop();
        }
    }

    public static void forceResume() {
//        if (currentPlay == PLAYING_LIVE) {
//            player = new DemoPlayer(new ExtractorRendererBuilder(context, userAgent, Uri.parse(urlPlaying)));
//
//            player.prepare();
//            player.setSurface(surface_live);
//            player.setPlayWhenReady(true);
//        }else if(currentPlay == PLAYING_DRM){
//            player = new DemoPlayer(new SmoothStreamingRendererBuilder(context, userAgent, urlPlaying.toString(),
//                    new SmoothStreamingTestMediaDrmCallback()));
//            player.setSurface(surface_live);
//
//            eventLogger = new EventLogger();
//            eventLogger.startSession();
//            player.addListener(eventLogger);
//            player.setInfoListener(eventLogger);
//
//            player.prepare();
//            player.setPlayWhenReady(true);
//        }

    }

    public static void setStateVideo(int state) {
        stateVideo = state;
    }

    public static int getStateVideo() {
        return stateVideo;
    }

    public static String getLicense() {
        return LA_URL;
    }

    public static void setDRMError() {
        errorDRM = true;
//        MainActivity.fireEvent("errorDrm");
    }

    public static void setDRMerrorNotFile() {
        if (!errorNotFileDRM) {
            errorNotFileDRM = true;
//            MainActivity.fireEvent("errorNotFileDRM");
        }

    }

    public static String getToken() {
        return token;
    }
    public static boolean getCodecMpeg123() {
        return codecMpeg123;
    }

    @Override
    public boolean execute(final String action, final JSONArray args, final CallbackContext callbackContext) throws JSONException {
        if (action.equals("playIP")) {
            cordova.getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    currentPlay = PLAYING_LIVE;
                    codecMpeg123 = true;
                    String url = null;
                    try {
                        url = args.getString(0);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    urlPlaying = url;
                    Log.i(LOGTAG, "url: " + url);
                    if (playerAudio != null) {
                        playerAudio.release();
                        playerAudio = null;
                    }
                    if (player != null) {
                        player.release();
                        player = null;
                    }
                    player.init(url, surfaceView_live);
                }
            });

            return true;
//        } else if (action.equals("playRtsp")) {
//            cordova.getActivity().runOnUiThread(new Runnable() {
//                public void run() {
//                    Log.i(LOGTAG, "playingrtsp: ");
//                    currentPlay = PLAYING_RTSP;
//                    codecMpeg123 = true;
//                    String url = null;
//                    try {
//                        url = args.getString(0);
//                    } catch (JSONException e) {
//                        e.printStackTrace();
//                    }
//                    urlPlaying = url;
//                    if (player != null) {
//                        if (player.getDvbSubsShown()) {
//                            player.switchDvbSubs(false);
//                            player.setSelectedTrack(DemoPlayer.TYPE_DVBSUBS, -1);
//                        }
//                        player.release();
//                    }
//                    player = new DemoPlayer(new RtspRendererBuilder(context, userAgent, url));
//                    player.setSurface(surface_live);
//
//                    player.prepare();
//                    player.setPlayWhenReady(true);
//
//                }
//            });
//
//            return true;
        } else if (action.equals("state")) {
            callbackContext.success(stateVideo);
            return true;
        } else if (action.equals("playVOD")) {
            cordova.getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    currentPlay = PLAYING_VOD;
                    codecMpeg123 = false;
                    String url = null;
                    try {
                        url = args.getString(0);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    urlPlaying = url;
                    Log.i(LOGTAG, "url: " + url);
                    if (player != null) {
                        player.release();
                    }
                    player.init(url, surfaceView_live);
                }
            });

            return true;
//        } else if (action.equals("pauseVOD")) {
//            player.getPlayerControl().pause();
//
//            return true;
//        } else if (action.equals("ffRtsp")) {
//            player.rtspFastForward(Integer.parseInt(args.getString(0)));
//
//            return true;
//        } else if (action.equals("rewindRtsp")) {
//            player.rtspRewind(Integer.parseInt(args.getString(0)));
//
//            return true;
//        } else if (action.equals("resumeVOD")) {
//            player.getPlayerControl().start();
//
//            return true;
//        } else if (action.equals("playPositionVOD")) {
//            if (player.getPlaybackState() == player.STATE_READY || player.getPlaybackState() == player.STATE_BUFFERING) {
//                callbackContext.success(player.getPlayerControl().getCurrentPosition());
//            } else {
//                callbackContext.success(0);
//            }
//
//            return true;
//        } else if (action.equals("playTimeVOD")) {
//            if (player.getPlaybackState() == player.STATE_READY || player.getPlaybackState() == player.STATE_BUFFERING) {
//                callbackContext.success(player.getPlayerControl().getDuration());
//            } else {
//                callbackContext.success(0);
//            }
//
//            return true;
//        } else if (action.equals("playPositionAudio")) {
//            if (playerAudio != null) {
//                if (playerAudio.getPlaybackState() == playerAudio.STATE_READY) {
//                    callbackContext.success(playerAudio.getPlayerControl().getCurrentPosition());
//                } else {
//                    callbackContext.success(0);
//                }
//            } else {
//                callbackContext.success(0);
//            }
//            return true;
//        } else if (action.equals("playTimeAudio")) {
//
//            if (playerAudio != null) {
//                if (playerAudio.getPlaybackState() == playerAudio.STATE_READY) {
//                    callbackContext.success(playerAudio.getPlayerControl().getDuration());
//                } else {
//                    callbackContext.success(0);
//                }
//            } else {
//
//                callbackContext.success(0);
//            }
//
//            return true;
//        } else if (action.equals("seekVOD")) {
//            player.seekTo(Integer.parseInt(args.getString(0)));
//
//            return true;
//        } else if (action.equals("seekRtsp")) {
//            player.rtspPlay(Integer.parseInt(args.getString(0)));
//
//            return true;
        } else if (action.equals("playAudio")) {
            currentPlay = PLAYING_AUDIO;
            codecMpeg123 = false;
            final String url = args.getString(0);
            urlPlaying = url;
            if (player != null) {
                player.release();
            }
            player.init(url, surfaceView_live);
            return true;
//        } else if (action.equals("pauseAudio")) {
//            playerAudio.getPlayerControl().pause();
//
//            return true;
//        } else if (action.equals("resumeAudio")) {
//            playerAudio.getPlayerControl().start();
//
//            return true;
        } else if (action.equals("stopAudio")) {
            if (playerAudio != null) {
                playerAudio.stop();
            }

            return true;
//        } else if (action.equals("seekAudio")) {
//            if (playerAudio != null) {
//                playerAudio.seekTo(Integer.parseInt(args.getString(0)));
//            }
//
//            return true;
        } else if (action.equals("release")) {
            if (player != null) {
                player.stop();
            }

            return true;
        } else if (action.equals("setToken")) {
            Log.i(LOGTAG, "setToken");
            token = args.getString(0);
            Log.i(LOGTAG, "token: " + token);
            return true;
        } else if (action.equals("setLicense")) {
            Log.i(LOGTAG, "setLicense");
            LA_URL = args.getString(0);
            Log.i(LOGTAG, "LA_URL: " + LA_URL);

            return true;
//        } else if (action.equals("playDrm")) {
//            cordova.getActivity().runOnUiThread(new Runnable() {
//                public void run() {
//                    currentPlay = PLAYING_DRM;
//                    codecMpeg123 = false;
//
//                    errorDRM = false;
//                    String contentUri = null;
//                    try {
//                        contentUri = args.getString(0);
//                        Log.i(LOGTAG, "url: " + contentUri);
//                        urlPlaying = contentUri;
//                    } catch (JSONException e) {
//                        e.printStackTrace();
//                    }
//
//                    if (player != null) {
//                        player.release();
//                    }
//
//                    player = new DemoPlayer(new SmoothStreamingRendererBuilder(context, userAgent, contentUri.toString(),
//                            new SmoothStreamingTestMediaDrmCallback()));
//                    player.setSurface(surface_live);
//
//                    eventLogger = new EventLogger();
//                    eventLogger.startSession();
//                    player.addListener(eventLogger);
//                    player.setInfoListener(eventLogger);
//
//                    player.prepare();
//                    player.setPlayWhenReady(true);
//                }
//            });
//
//            return true;
        } else if (action.equals("changeAudio")) {
            if (player.changeAudio(Integer.parseInt(args.getString(0)))) {
                callbackContext.success("1");
            } else {
                callbackContext.success("0");
            }
            return true;
        } else if (action.equals("getAudios")) {
            String audioinfo = player.getAudios();
            callbackContext.success(audioinfo.substring(1));
            return true;
        } else if (action.equals("getSubtitles")) {
            callbackContext.success(player.getSubtitles());
            return true;
        } else if (action.equals("changeSubtitle")) {
            if (player.changeSubtitle(Integer.parseInt(args.getString(0)))) {
                callbackContext.success("1");
            } else {
                callbackContext.success("0");
            }
            return true;
        } else if (action.equals("supportMulticast")) {
            File file = new File("/proc/net/igmp");
            if (file.exists()) {
                callbackContext.success("1");
            } else {
                callbackContext.success("0");
            }

            return true;
        } else if (action.equals("setVideoSize")) {
            float APP_WIDTH = 1280;
            float appWidth = webView.getWidth();
            float appHeight = webView.getHeight();
            final android.view.ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) surfaceView_live.getLayoutParams();
            if(args.getString(0).equals("1")) {
                float scale = APP_WIDTH / appWidth;
                Log.i(LOGTAG, "screenWidth: "+appWidth);
                Log.i(LOGTAG, "scale: "+scale);
                float width = Integer.parseInt(args.getString(1))/scale;
                float height = Integer.parseInt(args.getString(2))/scale;
                float bottomMargin = Integer.parseInt(args.getString(3))/scale;
                float rightMargin = Integer.parseInt(args.getString(3))/scale;
                lp.width = (int) width;
                lp.height =(int) height;
                lp.topMargin = (int) (appHeight - height - bottomMargin);
                lp.leftMargin = (int) (appWidth - width -rightMargin);
                cordova.getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        surfaceView_live.setLayoutParams(lp);
                        MainActivity.getVideoFrame().bringToFront();
                    }
                });

            }else {
                lp.width = (int) appWidth;
                lp.height = (int) appHeight;
                lp.topMargin = 0;
                lp.leftMargin = 0;
                cordova.getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                    surfaceView_live.setLayoutParams(lp);
                        webView.bringToFront();
                    }
                });
            }
            return true;
        } else if (action.equals("muteVideo")) {
            if(args.getString(0).equals("1")) {
                player.setVolume(0);
            }else{
                player.setVolume(0);
            }
            return true;
        } else {
            return false;
        }
    }

    //Exoplayer.EventListener implementation

    @Override
    public void onLoadingChanged(boolean isLoading) {
        // Do nothing.
    }

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
        // Do nothing.
    }

    @Override
    public void onPositionDiscontinuity() {
        // Do nothing.
    }

    @Override
    public void onTimelineChanged(Timeline timeline, Object manifest) {
        // Do nothing.
    }

    @Override
    public void onPlayerError(ExoPlaybackException e) {
//        String errorString = null;
//        if (e.type == ExoPlaybackException.TYPE_RENDERER) {
//            Exception cause = e.getRendererException();
//            if (cause instanceof MediaCodecRenderer.DecoderInitializationException) {
//                // Special case for decoder initialization failures.
//                MediaCodecRenderer.DecoderInitializationException decoderInitializationException =
//                        (MediaCodecRenderer.DecoderInitializationException) cause;
//                if (decoderInitializationException.decoderName == null) {
//                    if (decoderInitializationException.getCause() instanceof MediaCodecUtil.DecoderQueryException) {
//                        errorString = getString(R.string.error_querying_decoders);
//                    } else if (decoderInitializationException.secureDecoderRequired) {
//                        errorString = getString(R.string.error_no_secure_decoder,
//                                decoderInitializationException.mimeType);
//                    } else {
//                        errorString = getString(R.string.error_no_decoder,
//                                decoderInitializationException.mimeType);
//                    }
//                } else {
//                    errorString = getString(R.string.error_instantiating_decoder,
//                            decoderInitializationException.decoderName);
//                }
//            }
//        }
//        if (errorString != null) {
//            showToast(errorString);
//        }
//        playerNeedsSource = true;
//        updateButtonVisibilities();
//        showControls();
    }
}
