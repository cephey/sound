package ru.ptitsyn.afinal.services;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v7.app.NotificationCompat;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import javax.net.ssl.HttpsURLConnection;

import ru.ptitsyn.afinal.BookPageActivity;
import ru.ptitsyn.afinal.R;
import ru.ptitsyn.afinal.utils.Audio;
import ru.ptitsyn.afinal.utils.AuthUtil;
import ru.ptitsyn.afinal.utils.DBHelper;
import ru.ptitsyn.afinal.utils.PlaybackStatus;
import ru.ptitsyn.afinal.utils.PlayerInfo;
import ru.ptitsyn.afinal.utils.StorageUtil;

public class MediaPlayerService extends Service implements MediaPlayer.OnCompletionListener,
        MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnSeekCompleteListener,
        MediaPlayer.OnInfoListener, MediaPlayer.OnBufferingUpdateListener, AudioManager.OnAudioFocusChangeListener {

    private MediaPlayer mediaPlayer;
    // Используется для pause/resume MediaPlayer
    private int resumePosition;

    private int lastBookmarkPosition = 0;

    private Handler myHandler = new Handler();

    private AudioManager audioManager;

    //Handle incoming phone calls
    private boolean ongoingCall = false;
    private PhoneStateListener phoneStateListener;
    private TelephonyManager telephonyManager;

    //List of available Audio files
    private ArrayList<Audio> audioList;
    private int audioIndex = -1;
    private int audioBookmark = 0;
    private Audio activeAudio; //an object of the currently playing audio

    public static final String ACTION_PLAY = "ru.ptitsyn.afinal.ACTION_PLAY";
    public static final String ACTION_PAUSE = "ru.ptitsyn.afinal.ACTION_PAUSE";
    public static final String ACTION_PREVIOUS = "ru.ptitsyn.afinal.ACTION_PREVIOUS";
    public static final String ACTION_NEXT = "ru.ptitsyn.afinal.ACTION_NEXT";
    public static final String ACTION_STOP = "ru.ptitsyn.afinal.ACTION_STOP";

    // MediaSession
    private MediaSessionCompat mediaSession;
    private MediaControllerCompat.TransportControls transportControls;

    // AudioPlayer notification ID
    private static final int NOTIFICATION_ID = 101;


    @Override
    public void onCreate() {
        Log.e("EA_DEMO", "call --onCreate--");
        super.onCreate();
        // Управление вспроизведением при входящих звонках.
        // Ставит проигрывание на паузу.
        // После разговора запускает воспроизведение вновь
        callStateListener();

        // Рессивер ставит воспроизведение на паузу при изменение аудио выхода (выдернули наушник)
        registerBecomingNoisyReceiver();

        // Рессиверы котрые принимают от активити команды на начало воспроизведение, паузу, продолжение...
        register_playNewAudio();
        register_pauseAudio();
        register_resumeAudio();
    }

    //The system calls this method when an activity, requests the service be started
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (mediaSession == null) {
            try {
                initMediaSession();
            } catch (RemoteException e) {
                e.printStackTrace();
                stopSelf();
            }
        }

        // Handle Intent action from MediaSession.TransportControls
        handleIncomingActions(intent);
        return super.onStartCommand(intent, flags, startId);
    }

    private void initMediaPlayer() {
        mediaPlayer = new MediaPlayer();

        // Set up MediaPlayer event listeners
        mediaPlayer.setOnCompletionListener(this);
        mediaPlayer.setOnErrorListener(this);
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setOnBufferingUpdateListener(this);
        mediaPlayer.setOnSeekCompleteListener(this);
        mediaPlayer.setOnInfoListener(this);

        // Reset so that the MediaPlayer is not pointing to another data source
        mediaPlayer.reset();

        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {
            // Set the data source to the mediaFile location
            mediaPlayer.setDataSource(getApplicationContext(), Uri.parse(AuthUtil.domain + activeAudio.getUrl()), AuthUtil.headers());
        } catch (IOException e) {
            e.printStackTrace();
            stopSelf();
        }
        mediaPlayer.prepareAsync();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            stopMedia();
            mediaPlayer.release();
        }
        removeAudioFocus();

        // Disable the PhoneStateListener
        if (phoneStateListener != null) {
            telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);
        }

        removeNotification();

        // unregister BroadcastReceivers
        unregisterReceiver(becomingNoisyReceiver);
        unregisterReceiver(playNewAudio);
        unregisterReceiver(pauseAudio);
        unregisterReceiver(resumeAudio);

        // clear cached playlist
        new StorageUtil(getApplicationContext()).clearCachedAudioPlaylist();
    }

    public PlayerInfo info() {
        if (mediaPlayer == null) return null;

        if (mediaPlayer.isPlaying()) {
            return new PlayerInfo(mediaPlayer.getCurrentPosition(), mediaPlayer.getDuration());
        }
        return null;
    }

    private void playMedia() {
        if (!mediaPlayer.isPlaying()) {
            mediaPlayer.seekTo(resumePosition);
            mediaPlayer.start();
            myHandler.postDelayed(UpdateBookmark, 200);
        }
    }

    private void stopMedia() {
        if (mediaPlayer == null) return;
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            myHandler.removeCallbacks(UpdateBookmark);
        }
    }

    private void pauseMedia() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            resumePosition = mediaPlayer.getCurrentPosition();
            myHandler.removeCallbacks(UpdateBookmark);
        }
    }

    private Runnable UpdateBookmark = new Runnable() {
        public void run() {

            if (activeAudio.getFileId() > 0 && mediaPlayer.isPlaying()) {

                // сохраняю автозакладку
                int currentPosition = mediaPlayer.getCurrentPosition();
                int position = (int) (currentPosition / 1000);
                if (position % 5 == 0 && lastBookmarkPosition != position) {
                    lastBookmarkPosition = position;

                    // сохраняю закладку
                    DBHelper dbHelper = new DBHelper(getApplicationContext());
                    SQLiteDatabase db = dbHelper.getWritableDatabase();
                    Log.e("EA_DEMO", "--- UPDATE autobookmark");
                    db.execSQL(
                            "UPDATE autobookmark SET file_id = " + activeAudio.getFileId() + ", position = " + currentPosition + " WHERE book_id = " + activeAudio.getBookId(),
                            new String[] {}
                    );
                }
            }
            myHandler.postDelayed(this, 200);
        }
    };

    // Binder given to clients
    private final IBinder iBinder = new LocalBinder();

    @Override
    public IBinder onBind(Intent intent) {
        return iBinder;
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {
        //Invoked indicating buffering status of
        //a media resource being streamed over the network.
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        audioIndex += 1;
        resumePosition = 0;

        if (audioIndex < audioList.size()) {
            // index is in a valid range
            activeAudio = audioList.get(audioIndex);

            mp.reset();
            mp.setAudioStreamType(AudioManager.STREAM_MUSIC);

            try {
                // Set the data source to the mediaFile location
//            mediaPlayer.setDataSource(mediaFile);
                mp.setDataSource(getApplicationContext(), Uri.parse(AuthUtil.domain + activeAudio.getUrl()), AuthUtil.headers());
            } catch (IOException e) {
                e.printStackTrace();
                stopSelf();
            }
            mp.prepareAsync();

        } else {
            // останавливаю сервис, если плей лист закончился
            stopMedia();
            stopSelf();
        }
    }

    // Handle errors
    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        // Invoked when there has been an error during an asynchronous operation
        switch (what) {
            case MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK:
                Log.d("MediaPlayer Error", "MEDIA ERROR NOT VALID FOR PROGRESSIVE PLAYBACK " + extra);
                break;
            case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
                Log.d("MediaPlayer Error", "MEDIA ERROR SERVER DIED " + extra);
                break;
            case MediaPlayer.MEDIA_ERROR_UNKNOWN:
                Log.d("MediaPlayer Error", "MEDIA ERROR UNKNOWN " + extra);
                break;
        }
        return false;
    }

    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        // Invoked to communicate some info.
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        // Invoked when the media source is ready for playback.
        playMedia();
    }

    @Override
    public void onSeekComplete(MediaPlayer mp) {
        // Invoked indicating the completion of a seek operation.
    }

    @Override
    public void onAudioFocusChange(int focusState) {
        // Invoked when the audio focus of the system is updated.
        switch (focusState) {
            case AudioManager.AUDIOFOCUS_GAIN:
                // resume playback
                if (mediaPlayer == null) initMediaPlayer();
                else if (!mediaPlayer.isPlaying()) mediaPlayer.start();
                mediaPlayer.setVolume(1.0f, 1.0f);
                break;
            case AudioManager.AUDIOFOCUS_LOSS:
                // Lost focus for an unbounded amount of time: stop playback and release media player
                if (mediaPlayer.isPlaying()) mediaPlayer.stop();
                mediaPlayer.release();
                mediaPlayer = null;
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                // Lost focus for a short time, but we have to stop
                // playback. We don't release the media player because playback
                // is likely to resume
                if (mediaPlayer.isPlaying()) mediaPlayer.pause();
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                // Lost focus for a short time, but it's ok to keep playing
                // at an attenuated level
                if (mediaPlayer.isPlaying()) mediaPlayer.setVolume(0.1f, 0.1f);
                break;
        }
    }

    private boolean requestAudioFocus() {
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        int result = audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            // Focus gained
            return true;
        }
        // Could not gain focus
        return false;
    }

    private boolean removeAudioFocus() {
        return AudioManager.AUDIOFOCUS_REQUEST_GRANTED ==
                audioManager.abandonAudioFocus(this);
    }

    public class LocalBinder extends Binder {
        public MediaPlayerService getService() {
            return MediaPlayerService.this;
        }
    }

    // ---------------------------------------------------------------------------------------------
    // Ставим воспроизведение на паузу, когда пользователь вдруг выдернул наушники из телефона
    // или отключилось bluetooth устройство с которого шло воспроизведение звука
    private BroadcastReceiver becomingNoisyReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //pause audio on ACTION_AUDIO_BECOMING_NOISY
            pauseMedia();
            buildNotification(PlaybackStatus.PAUSED);
        }
    };

    private void registerBecomingNoisyReceiver() {
        //register after getting audio focus
        IntentFilter intentFilter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        registerReceiver(becomingNoisyReceiver, intentFilter);
    }
    // ---------------------------------------------------------------------------------------------

    // Ставим на паузу воспроизведение если принимаем звонок на телефон.
    // И продолжаем после того, как разговор закончен
    private void callStateListener() {
        // Get the telephony manager
        telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        //Starting listening for PhoneState changes
        phoneStateListener = new PhoneStateListener() {
            @Override
            public void onCallStateChanged(int state, String incomingNumber) {
                switch (state) {
                    //if at least one call exists or the phone is ringing
                    //pause the MediaPlayer
                    case TelephonyManager.CALL_STATE_OFFHOOK:
                    case TelephonyManager.CALL_STATE_RINGING:
                        if (mediaPlayer != null) {
                            pauseMedia();
                            ongoingCall = true;
                        }
                        break;
                    case TelephonyManager.CALL_STATE_IDLE:
                        // Phone idle. Start playing.
                        if (mediaPlayer != null) {
                            if (ongoingCall) {
                                ongoingCall = false;
                                playMedia();
                            }
                        }
                        break;
                }
            }
        };
        // Register the listener with the telephony manager
        // Listen for changes to the device call state.
        telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
    }

    // ---------------------------------------------------------------------------------------------
    private BroadcastReceiver playNewAudio = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            try {
                // Загружаю список файлов для воспроизведения и закладку
                StorageUtil storage = new StorageUtil(getApplicationContext());
                audioList = storage.loadAudio();
                audioIndex = storage.loadAudioIndex();
                resumePosition = storage.loadAudioBookmarkPosition();

                if (audioIndex != -1 && audioIndex < audioList.size()) {
                    //index is in a valid range
                    activeAudio = audioList.get(audioIndex);
                } else {
                    stopSelf();
                }
            } catch (NullPointerException e) {
                stopSelf();
            }

            // запрашиваю audio focus
            if (!requestAudioFocus()) {
                // если OS фокус не дала, останавливаю сервис
                stopSelf();
            }

            // Останавливаю проигрывание, если необходимо
            // делаю reset Аудиоплееру
            stopMedia();
            if (mediaPlayer != null) {
                mediaPlayer.reset();
            }
            initMediaPlayer();
            updateMetaData();
            buildNotification(PlaybackStatus.PLAYING);
        }
    };

    private void register_playNewAudio() {
        // Регистрирую ресивер playNewAudio
        IntentFilter filter = new IntentFilter(BookPageActivity.Broadcast_PLAY_NEW_AUDIO);
        registerReceiver(playNewAudio, filter);
    }
    // ---------------------------------------------------------------------------------------------
    private BroadcastReceiver pauseAudio = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            // Ставлю воспроизведение на паузу
            pauseMedia();
            updateMetaData();
            buildNotification(PlaybackStatus.PAUSED);
        }
    };

    private void register_pauseAudio() {
        // Регистрирую ресивер pauseAudio
        IntentFilter filter = new IntentFilter(BookPageActivity.Broadcast_PAUSE_AUDIO);
        registerReceiver(pauseAudio, filter);
    }
    // ---------------------------------------------------------------------------------------------
    private BroadcastReceiver resumeAudio = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            playMedia();
            updateMetaData();
            buildNotification(PlaybackStatus.PLAYING);
        }
    };

    private void register_resumeAudio() {
        // Регистрирую ресивер resumeAudio
        IntentFilter filter = new IntentFilter(BookPageActivity.Broadcast_RESUME_AUDIO);
        registerReceiver(resumeAudio, filter);
    }
    // ---------------------------------------------------------------------------------------------

    private void initMediaSession() throws RemoteException {
        if (mediaSession != null) return; // mediaSessionManager exists

        // Create a new MediaSession
        mediaSession = new MediaSessionCompat(getApplicationContext(), "EA_DEMO"); // второй параметр - это LOG тег
        //Get MediaSessions transport controls
        transportControls = mediaSession.getController().getTransportControls();
        //set MediaSession -> ready to receive media commands
        mediaSession.setActive(true);
        //indicate that the MediaSession handles transport control commands
        // through its MediaSessionCompat.Callback.
        mediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);

        //Set mediaSession's MetaData
        updateMetaData();

        // Attach Callback to receive MediaSession updates
        mediaSession.setCallback(new MediaSessionCompat.Callback() {
            // Implement callbacks
            @Override
            public void onPlay() {
                super.onPlay();
                playMedia();
                buildNotification(PlaybackStatus.PLAYING);
            }

            @Override
            public void onPause() {
                super.onPause();
                pauseMedia();
                buildNotification(PlaybackStatus.PAUSED);
            }

            @Override
            public void onSeekTo(long position) {
                super.onSeekTo(position);
            }
        });
    }

    private void updateMetaData() {
        if (activeAudio != null) {

            Bitmap albumArt = BitmapFactory.decodeResource(getResources(), R.drawable.media_image); // обложка
            // Update the current metadata
            mediaSession.setMetadata(new MediaMetadataCompat.Builder()
                    .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, albumArt) // по идее сюда надо обложку книги
                    .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, activeAudio.getBookAuthorName()) // автор
                    .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, activeAudio.getBookName()) // название книги
//                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, activeAudio.getTitle()) // возможно название главы ???
                    .build());
        }
    }

    private void buildNotification(PlaybackStatus playbackStatus) {

        int notificationAction = android.R.drawable.ic_media_pause;//needs to be initialized
        PendingIntent play_pauseAction = null;

        //Build a new notification according to the current state of the MediaPlayer
        if (playbackStatus == PlaybackStatus.PLAYING) {
            notificationAction = android.R.drawable.ic_media_pause;
            //create the pause action
            play_pauseAction = playbackAction(1);
        } else if (playbackStatus == PlaybackStatus.PAUSED) {
            notificationAction = android.R.drawable.ic_media_play;
            //create the play action
            play_pauseAction = playbackAction(0);
        }

        Bitmap largeIcon = BitmapFactory.decodeResource(getResources(), R.drawable.media_image); // обложка
        // Нужно вынести в отдельный таск, тогда заработает
        // Bitmap largeIcon = getBitmapFromURL(activeAudio.getBookCover());

        // Create a new Notification
        NotificationCompat.Builder notificationBuilder = (NotificationCompat.Builder) new NotificationCompat.Builder(this)
                .setShowWhen(false)
                // Set the Notification style
                .setStyle(new NotificationCompat.MediaStyle()
                        // Attach our MediaSession token
                        .setMediaSession(mediaSession.getSessionToken())
                        // Show our playback controls in the compact notification view.
                        .setShowActionsInCompactView(0, 1, 2))
                // Set the Notification color
                .setColor(getResources().getColor(R.color.colorPrimary))
                // Set the large and small icons
                .setLargeIcon(largeIcon)
                .setSmallIcon(android.R.drawable.stat_sys_headset)
                // Set Notification content information
                .setContentText(activeAudio.getBookAuthorName())
                .setContentTitle(activeAudio.getBookName())
//                .setContentInfo(activeAudio.getTitle())
                .setContentInfo("")
                // Add playback actions
                .addAction(android.R.drawable.ic_media_previous, "previous", playbackAction(3))
                .addAction(notificationAction, "pause", play_pauseAction)
                .addAction(android.R.drawable.ic_media_next, "next", playbackAction(2));

        ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).notify(NOTIFICATION_ID, notificationBuilder.build());
    }

    private void removeNotification() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(NOTIFICATION_ID);
    }

    public static Bitmap getBitmapFromURL(String src) {
        Log.e("EA_DEMO", src);
        try {
            URL url = new URL(src);
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Authorization", AuthUtil.token);
            connection.connect();
            InputStream input = connection.getInputStream();
            return BitmapFactory.decodeStream(input);
        } catch (IOException e) {
            return null;
        }
    }

    private PendingIntent playbackAction(int actionNumber) {
        Intent playbackAction = new Intent(this, MediaPlayerService.class);
        switch (actionNumber) {
            case 0:
                // Play
                playbackAction.setAction(ACTION_PLAY);
                return PendingIntent.getService(this, actionNumber, playbackAction, 0);
            case 1:
                // Pause
                playbackAction.setAction(ACTION_PAUSE);
                return PendingIntent.getService(this, actionNumber, playbackAction, 0);
            case 2:
                // Next track
                playbackAction.setAction(ACTION_NEXT);
                return PendingIntent.getService(this, actionNumber, playbackAction, 0);
            case 3:
                // Previous track
                playbackAction.setAction(ACTION_PREVIOUS);
                return PendingIntent.getService(this, actionNumber, playbackAction, 0);
            default:
                break;
        }
        return null;
    }

    private void handleIncomingActions(Intent playbackAction) {
        Log.e("EA_DEMO", "call handleIncomingActions");
        if (playbackAction == null || playbackAction.getAction() == null) return;

        String actionString = playbackAction.getAction();
        Log.e("EA_DEMO", "handleIncomingActions playbackAction is " + actionString);
        if (actionString.equalsIgnoreCase(ACTION_PLAY)) {
            transportControls.play();
        } else if (actionString.equalsIgnoreCase(ACTION_PAUSE)) {
            transportControls.pause();
        } else if (actionString.equalsIgnoreCase(ACTION_NEXT)) {
            transportControls.skipToNext();
        } else if (actionString.equalsIgnoreCase(ACTION_PREVIOUS)) {
            transportControls.skipToPrevious();
        } else if (actionString.equalsIgnoreCase(ACTION_STOP)) {
            transportControls.stop();
        }
    }


}

