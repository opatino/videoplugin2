package com.videoplayer;

import android.content.Context;
import android.os.Handler;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceView;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.drm.DrmSessionManager;
import com.google.android.exoplayer2.source.TrackGroup;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.text.Cue;
import com.google.android.exoplayer2.text.TextRenderer;
import com.google.android.exoplayer2.trackselection.AdaptiveVideoTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.FixedTrackSelection;
import com.google.android.exoplayer2.trackselection.MappingTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.source.ConcatenatingMediaSource;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.dash.DashMediaSource;
import com.google.android.exoplayer2.source.dash.DefaultDashChunkSource;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.source.smoothstreaming.DefaultSsChunkSource;
import com.google.android.exoplayer2.source.smoothstreaming.SsMediaSource;
import com.google.android.exoplayer2.ui.SubtitleView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.upstream.HttpDataSource;
import com.google.android.exoplayer2.util.Util;

import java.util.List;

public class VideoPlayer {

    private static final String TAG = "VideoPlayer";
    private static final DefaultBandwidthMeter BANDWIDTH_METER = new DefaultBandwidthMeter();
    private SimpleExoPlayer player;
    private MappingTrackSelector trackSelector;
    private Handler mainHandler;
    private boolean playerNeedsSource;
    private DataSource.Factory mediaDataSourceFactory;
    private boolean shouldRestorePosition;
    private MappingTrackSelector.SelectionOverride override;
    private ComponentListener componentListener;
    private EventLogger eventLogger;
    private SubtitleView subtitleLayout;
    String userAgent;
    Context context;
    private static final TrackSelection.Factory FIXED_FACTORY = new FixedTrackSelection.Factory();

    public VideoPlayer(Context context, SubtitleView subtitleView) {
        this.context = context;
        mainHandler = new Handler();
        userAgent = Util.getUserAgent(context, "mvtv_phone");
        mediaDataSourceFactory = buildDataSourceFactory(true);
        shouldRestorePosition = true;
        componentListener = new ComponentListener();
        subtitleLayout = subtitleView;
    }

    public void init(String uriString, SurfaceView surfaceView) {
        if (player == null) {
            DrmSessionManager drmSessionManager = null;
            eventLogger = new EventLogger();
            TrackSelection.Factory videoTrackSelectionFactory =
                    new AdaptiveVideoTrackSelection.Factory(BANDWIDTH_METER);
            trackSelector = new DefaultTrackSelector(mainHandler, videoTrackSelectionFactory);
//        trackSelector.addListener(this);
            trackSelector.addListener(eventLogger);
            player = ExoPlayerFactory.newSimpleInstance(context, trackSelector, new DefaultLoadControl(),
                    drmSessionManager, false); //prefer extension decoders false
            player.addListener(eventLogger);
            player.setAudioDebugListener(eventLogger);
            player.setVideoDebugListener(eventLogger);
            player.setId3Output(eventLogger);
            playerNeedsSource = true;
        }

        subtitleLayout.setUserDefaultStyle();
        subtitleLayout.setUserDefaultTextSize();

        if (playerNeedsSource) {
            Uri[] uris;
            String[] extensions;
//            if (ACTION_VIEW.equals(action)) {
//                uris = new Uri[] {intent.getData()};
//                extensions = new String[] {intent.getStringExtra(EXTENSION_EXTRA)};
//            } else if (ACTION_VIEW_LIST.equals(action)) {
//                String[] uriStrings = intent.getStringArrayExtra(URI_LIST_EXTRA);
//                uris = new Uri[uriStrings.length];
//                for (int i = 0; i < uriStrings.length; i++) {
//                    uris[i] = Uri.parse(uriStrings[i]);
//                }
//                extensions = intent.getStringArrayExtra(EXTENSION_LIST_EXTRA);
//                if (extensions == null) {
//                    extensions = new String[uriStrings.length];
//                }
//            } else {
//                showToast(getString(R.string.unexpected_intent_action, action));
//                return;
//            }
//            if (Util.maybeRequestReadExternalStoragePermission(this, uris)) {
//                // The player will be reinitialized if the permission is granted.
//                return;
//            }
            uris = new Uri[]{Uri.parse(uriString)};
            MediaSource[] mediaSources = new MediaSource[uris.length];
            for (int i = 0; i < uris.length; i++) {
                mediaSources[i] = buildMediaSource(uris[i], null); // extensions[i] = null);
            }
            MediaSource mediaSource = mediaSources.length == 1 ? mediaSources[0]
                    : new ConcatenatingMediaSource(mediaSources);
            player.prepare(mediaSource, !shouldRestorePosition);
            playerNeedsSource = false;
        }

        player.setVideoSurfaceView(surfaceView);
        player.setTextOutput(componentListener);
        player.setPlayWhenReady(true);
    }

    public void release() {
        player.release();
        player = null;
    }

    public void stop() {
        player.stop();
    }

    public void setVolume(float vol) {
        player.setVolume(vol);
    }

    public boolean changeAudio(int audioTrack) {
        MappingTrackSelector.TrackInfo  trackInfo = trackSelector.getTrackInfo();
        int rendererCount = trackInfo.rendererCount;
        int i;
        for (i = 0;  i < rendererCount; i++) {
            if (player.getRendererType(i) == C.TRACK_TYPE_AUDIO) {
                break;
            }
        }
        TrackGroupArray trackGroups = trackInfo.getTrackGroups(i);
        Log.d(TAG, "audio elegido " + audioTrack);
        if (audioTrack == -1) {
            trackSelector.clearSelectionOverrides(i);
            return true;
        }
        override = new MappingTrackSelector.SelectionOverride(FIXED_FACTORY, audioTrack, 0);
        trackSelector.setSelectionOverride(i, trackGroups, override);
        return true;
    }

    public boolean changeSubtitle(int dvbSubTrack) {
        MappingTrackSelector.TrackInfo  trackInfo = trackSelector.getTrackInfo();
        int rendererCount = trackInfo.rendererCount;
        int i;
        for (i = 0;  i < rendererCount; i++) {
            if (player.getRendererType(i) == C.TRACK_TYPE_TEXT) {
                break;
            }
        }
        TrackGroupArray trackGroups = trackInfo.getTrackGroups(i);;
        Log.d(TAG, "subtitulo elegido " + dvbSubTrack);
        if (dvbSubTrack == -1) {
            trackSelector.clearSelectionOverrides(i);
            return true;
        }
        override = new MappingTrackSelector.SelectionOverride(FIXED_FACTORY, dvbSubTrack, 0);
        trackSelector.setSelectionOverride(i, trackGroups, override);
        return true;
    }

    public String getAudios() {
        String audioinfo = "";
        MappingTrackSelector.TrackInfo  trackInfo = trackSelector.getTrackInfo();
        int rendererCount = trackInfo.rendererCount;
        int i;
        for (i = 0;  i < rendererCount; i++) {
            if (player.getRendererType(i) == C.TRACK_TYPE_AUDIO) {
                break;
            }
        }
        TrackGroupArray trackGroup = null;
        if (i < rendererCount) {
            trackGroup = trackInfo.getTrackGroups(i);
            Log.d(TAG, "num audios " + trackGroup.length);
            if (trackGroup.length > 0) {
                for (int index = 0;  index < trackGroup.length; index++) {
                    TrackGroup group = trackGroup.get(index);
                    int l = group.length;
                    for (int j = 0 ; j < l; j++) {
                        String code = "unknown";
                        Format format = group.getFormat(j);
                        Log.d(TAG, "audio language: " + format.language);
                        if (format.sampleMimeType.equals("audio/mpeg-L2")) {
                            code = "mpeg1";
                        } else if (format.sampleMimeType.equals("audio/ac3")) {
                            code = "ac3";
                        }
                        audioinfo += "*" + index + "-" + format.language + "-" + code;
                    }
                }
            }
        }
        return audioinfo;
    }

    public String getSubtitles() {
        String subtitlesInfo = "";
        MappingTrackSelector.TrackInfo  trackInfo = trackSelector.getTrackInfo();
        int rendererCount = trackInfo.rendererCount;
        int i;
        for (i = 0;  i < rendererCount; i++) {
            if (player.getRendererType(i) == C.TRACK_TYPE_TEXT) {
                break;
            }
        }
        TrackGroupArray trackGroup = null;
        if (i < rendererCount) {
            trackGroup = trackInfo.getTrackGroups(i);
            Log.d(TAG, "num subtitles " + trackGroup.length);
            if (trackGroup.length > 0) {
                for (int index = 0;  index < trackGroup.length; index++) {
                    TrackGroup group = trackGroup.get(index);
                    int l = group.length;
                    for (int j = 0 ; j < l; j++) {
                        Format format = group.getFormat(j);
                        Log.d(TAG, "subtitle language: " + format.language);
                        if (format.sampleMimeType.equals("application/dvbsubs")) {
                            subtitlesInfo += "*" + index + "-" + format.language;
                        }
                    }
                }
            }
        }
        return subtitlesInfo;
    }

    private MediaSource buildMediaSource(Uri uri, String overrideExtension) {
        int type = Util.inferContentType(!TextUtils.isEmpty(overrideExtension) ? "." + overrideExtension
                : uri.getLastPathSegment());
        switch (type) {
            case Util.TYPE_SS:
                return new SsMediaSource(uri, buildDataSourceFactory(false),
                        new DefaultSsChunkSource.Factory(mediaDataSourceFactory), mainHandler, eventLogger);
            case Util.TYPE_DASH:
                return new DashMediaSource(uri, buildDataSourceFactory(false),
                        new DefaultDashChunkSource.Factory(mediaDataSourceFactory), mainHandler, eventLogger);
            case Util.TYPE_HLS:
                return new HlsMediaSource(uri, mediaDataSourceFactory, mainHandler, eventLogger);
            case Util.TYPE_OTHER:
                return new ExtractorMediaSource(uri, mediaDataSourceFactory, new DefaultExtractorsFactory(),
                        mainHandler, eventLogger);
            default: {
                throw new IllegalStateException("Unsupported type: " + type);
            }
        }
    }

    /**
     * Returns a new DataSource factory.
     *
     * @param useBandwidthMeter Whether to set {@link #BANDWIDTH_METER} as a listener to the new
     *                          DataSource factory.
     * @return A new DataSource factory.
     */
    private DataSource.Factory buildDataSourceFactory(boolean useBandwidthMeter) {
        return new DefaultDataSourceFactory(context, useBandwidthMeter ? BANDWIDTH_METER : null,
                buildHttpDataSourceFactory(useBandwidthMeter));
    }

    private final class ComponentListener implements TextRenderer.Output {
        @Override
        public void onCues(List<Cue> cue) {
            subtitleLayout.onCues(cue);
        }
    }

    /**
     * Returns a new HttpDataSource factory.
     *
     * @param useBandwidthMeter Whether to set {@link #BANDWIDTH_METER} as a listener to the new
     *                          DataSource factory.
     * @return A new HttpDataSource factory.
     */
    private HttpDataSource.Factory buildHttpDataSourceFactory(boolean useBandwidthMeter) {
        return new DefaultHttpDataSourceFactory(userAgent, useBandwidthMeter ? BANDWIDTH_METER : null);
    }

}
