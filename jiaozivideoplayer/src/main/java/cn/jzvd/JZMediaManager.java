package cn.jzvd;

import android.graphics.Point;
import android.graphics.SurfaceTexture;
import android.media.AudioManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;
import android.view.ViewGroup;

import java.lang.reflect.Method;
import java.util.Map;

import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;

/**
 * <p>统一管理MediaPlayer的地方,只有一个mediaPlayer实例，那么不会有多个视频同时播放，也节省资源。</p>
 * <p>Unified management MediaPlayer place, there is only one MediaPlayer instance, then there will be no more video broadcast at the same time, also save resources.</p>
 * Created by Nathen
 * On 2015/11/30 15:39
 */
public class JZMediaManager implements TextureView.SurfaceTextureListener, IjkMediaPlayer.OnPreparedListener, IjkMediaPlayer.OnCompletionListener, IjkMediaPlayer.OnBufferingUpdateListener, IjkMediaPlayer.OnSeekCompleteListener, IjkMediaPlayer.OnErrorListener, IjkMediaPlayer.OnInfoListener, IjkMediaPlayer.OnVideoSizeChangedListener {
    public static final int HANDLER_PREPARE = 0;
    public static final int HANDLER_RELEASE = 2;
    public static final String TAG = "JiaoZiVideoPlayer";
    public static JZResizeTextureView textureView;
    public static SurfaceTexture savedSurfaceTexture;
    public static String CURRENT_PLAYING_URL;
    public static boolean CURRENT_PLING_LOOP;
    public static Map<String, String> MAP_HEADER_DATA;
    private static cn.jzvd.JZMediaManager JZMediaManager;
    public IjkMediaPlayer mediaPlayer = new IjkMediaPlayer();
    public int currentVideoWidth = 0;
    public int currentVideoHeight = 0;
    HandlerThread mMediaHandlerThread;
    MediaHandler mMediaHandler;
    Handler mainThreadHandler;

    public JZMediaManager() {
        mMediaHandlerThread = new HandlerThread(TAG);
        mMediaHandlerThread.start();
        mMediaHandler = new MediaHandler((mMediaHandlerThread.getLooper()));
        mainThreadHandler = new Handler();
    }

    public static cn.jzvd.JZMediaManager instance() {
        if (JZMediaManager == null) {
            JZMediaManager = new JZMediaManager();
        }
        return JZMediaManager;
    }

    public Point getVideoSize() {
        if (currentVideoWidth != 0 && currentVideoHeight != 0) {
            return new Point(currentVideoWidth, currentVideoHeight);
        } else {
            return null;
        }
    }

    public void prepare() {
        releaseMediaPlayer();
        Message msg = new Message();
        msg.what = HANDLER_PREPARE;
        mMediaHandler.sendMessage(msg);
    }

    public void releaseMediaPlayer() {
        Message msg = new Message();
        msg.what = HANDLER_RELEASE;
        mMediaHandler.sendMessage(msg);
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i1) {
//        Log.i(TAG, "onSurfaceTextureAvailable [" + JZVideoPlayerManager.getCurrentJzvd().hashCode() + "] ");
        if (savedSurfaceTexture == null) {
            savedSurfaceTexture = surfaceTexture;
            prepare();
        } else {
            textureView.setSurfaceTexture(savedSurfaceTexture);
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i1) {
        // 如果SurfaceTexture还没有更新Image，则记录SizeChanged事件，否则忽略
        Log.i(TAG, "onSurfaceTextureSizeChanged [" + JZVideoPlayerManager.getCurrentJzvd().hashCode() + "] ");
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
        return savedSurfaceTexture == null;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
    }

    @Override
    public void onPrepared(IMediaPlayer mp) {
        mediaPlayer.start();
        mainThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                if (JZVideoPlayerManager.getCurrentJzvd() != null) {
                    JZVideoPlayerManager.getCurrentJzvd().onPrepared();
                }
            }
        });
    }

    @Override
    public void onCompletion(IMediaPlayer mp) {
        mainThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                if (JZVideoPlayerManager.getCurrentJzvd() != null) {
                    JZVideoPlayerManager.getCurrentJzvd().onAutoCompletion();
                }
            }
        });
    }

    @Override
    public void onBufferingUpdate(IMediaPlayer mp, final int percent) {
        mainThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                if (JZVideoPlayerManager.getCurrentJzvd() != null) {
                    JZVideoPlayerManager.getCurrentJzvd().setBufferProgress(percent);
                }
            }
        });
    }

    @Override
    public void onSeekComplete(IMediaPlayer mp) {
        mainThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                if (JZVideoPlayerManager.getCurrentJzvd() != null) {
                    JZVideoPlayerManager.getCurrentJzvd().onSeekComplete();
                }
            }
        });
    }

    @Override
    public boolean onError(IMediaPlayer mp, final int what, final int extra) {
        mainThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                if (JZVideoPlayerManager.getCurrentJzvd() != null) {
                    JZVideoPlayerManager.getCurrentJzvd().onError(what, extra);
                }
            }
        });
        return true;
    }

    @Override
    public boolean onInfo(IMediaPlayer mp, final int what, final int extra) {
        mainThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                if (JZVideoPlayerManager.getCurrentJzvd() != null) {
                    JZVideoPlayerManager.getCurrentJzvd().onInfo(what, extra);
                }
            }
        });
        return false;
    }

//    @Override
//    public void onVideoSizeChanged(IMediaPlayer mp, int width, int height) {
//        currentVideoWidth = width;
//        currentVideoHeight = height;
//        mainThreadHandler.post(new Runnable() {
//            @Override
//            public void run() {
//                if (JZVideoPlayerManager.getCurrentJzvd() != null) {
//                    JZVideoPlayerManager.getCurrentJzvd().onVideoSizeChanged();
//                }
//            }
//        });
//    }

    @Override
    public void onVideoSizeChanged(IMediaPlayer iMediaPlayer, int i, int i1, int i2, int i3) {
        currentVideoWidth = i;
        currentVideoHeight = i1;
        mainThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                if (JZVideoPlayerManager.getCurrentJzvd() != null) {
                    JZVideoPlayerManager.getCurrentJzvd().onVideoSizeChanged();
                }
            }
        });
    }

    public class MediaHandler extends Handler {
        public MediaHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case HANDLER_PREPARE:
                    try {
                        currentVideoWidth = 0;
                        currentVideoHeight = 0;
                        mediaPlayer.release();
                        mediaPlayer = new IjkMediaPlayer();
                        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                        mediaPlayer.setLooping(CURRENT_PLING_LOOP);
                        mediaPlayer.setOnPreparedListener(cn.jzvd.JZMediaManager.this);
                        mediaPlayer.setOnCompletionListener(cn.jzvd.JZMediaManager.this);
                        mediaPlayer.setOnBufferingUpdateListener(cn.jzvd.JZMediaManager.this);
                        mediaPlayer.setScreenOnWhilePlaying(true);
                        mediaPlayer.setOnSeekCompleteListener(cn.jzvd.JZMediaManager.this);
                        mediaPlayer.setOnErrorListener(cn.jzvd.JZMediaManager.this);
                        mediaPlayer.setOnInfoListener(cn.jzvd.JZMediaManager.this);
                        mediaPlayer.setOnVideoSizeChangedListener(cn.jzvd.JZMediaManager.this);
                        Class<IjkMediaPlayer> clazz = IjkMediaPlayer.class;
                        Method method = clazz.getDeclaredMethod("setDataSource", String.class, Map.class);
                        method.invoke(mediaPlayer, CURRENT_PLAYING_URL, MAP_HEADER_DATA);
                        mediaPlayer.prepareAsync();
                        mediaPlayer.setSurface(new Surface(savedSurfaceTexture));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                case HANDLER_RELEASE:
                    mediaPlayer.release();
                    if (textureView.getParent() != null) {
                        ViewGroup container = (ViewGroup) textureView.getParent();
                        container.removeView(textureView);
                    }
                    break;
            }
        }
    }

}
