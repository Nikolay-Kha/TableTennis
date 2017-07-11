package com.greenlogger.tennis;

import java.util.Date;
import java.util.Random;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Handler;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.greenlogger.tenis.R;
import com.greenlogger.tennis.GameSettings.DIFFICULTY;

public class TennisView extends SurfaceView implements SurfaceHolder.Callback {
	public float mNetworkDelay = 0.0f;
	public static final int SCORE_TO_WIN = 11;
	public final static int LAN_MULTIPLAYER_SERVER = -1;
	public final static int LAN_MULTIPLAYER_CLIENT = -2;
	public final static int ONE_PLAYER_ARCADE = -3;
	private float mleftTabPosition = 0.5f;
	private float mrightTabPosition = 0.5f;
	private boolean mleftTabLock = false;
	private boolean mrightTabLock = false;
	private final float TAB_HEIGHT = 0.15f;
	private final float TAB_WIDTH = 0.025f;
	private static final float TAB_SPEED = 0.005f;
	public float mxballpos;
	public float myballpos;
	private float mxballspeed;
	private float myballspeed;
	private boolean mGameTimerActive = false;
	private GameTimerEvent mGameTimer = null;
	private boolean misRunning = false;
	private boolean mIsDrawing = false;
	private boolean misdemo = true;
	private static Random random;
	private static final long GAME_CYCLE_PERIOD = 16;
	private static final float COLLAPSE_MULTI = 1.02f;
	private static final float COLLAPSE_MULTI_HI = 1.009f;
	private static final float INIT_BALL_SPEED = 0.003f;
	private static final float MAX_SUITABLE_SPEED = 0.02f;
	private static final float MAX_SPEED = 4 * MAX_SUITABLE_SPEED;
	public static final int TAB_NONE = 0x0;
	public static final int TAB_LEFT = 0x1;
	public static final int TAB_RIGHT = 0x2;
	public static final int TAB_BOTH = 0x3;
	private static final float MARGIN_COEF = 0.08f;
	private static final int MARGIN_DEFAULT = 5;
	public static final float TILT_MIN_SENSIVITY_ANGLE = 10.0f;
	private static final float TILT_MIN_ANGLE = 5.0f;
	private static final int COLOR_BACKGROUND = 0xFF000000;
	private static final int COLOR_TAB = 0xFFFFFFFF;
	private int mrmargin = MARGIN_DEFAULT;
	private int mlmargin = MARGIN_DEFAULT;
	private int mcputab;
	private int mPlayerTabPos;
	private float mlastspeedpost = -1.0f;
	private GameSettings.DIFFICULTY mdifficulty = GameSettings.DIFFICULTY.DIFFICULTY_HARD;
	private int mcpunumber;
	private float mtildAngle;
	private final float mlowPassTiltFilter[];
	private int mlowPassTiltFilterIndex;
	public int leftTabScore;
	public int rightTabScore;
	private final SoundPool m_soundPool;
	private final int SOUND_START;
	public int SOUND_FINISH;
	private final int SOUND_REF = -10;
	private final int SOUND_REF1;
	private final int SOUND_REF2;
	private final int SOUND_REF3;
	private final int SOUND_REF4;
	private Runnable mchecker = null;
	private final Handler m_handler = new Handler();
	private float mFirstMove;
	private long lastTime = 0;
	private float mTabCpuDifficultySpeed = TAB_SPEED;
	private boolean mGameThreadLive = true;

	private long mLastShotBegin = 0;

	private final Rect mDrawableLTabRect = new Rect();
	private final Rect mDrawableRTabRect = new Rect();
	private final Rect mDrawableBallRect = new Rect();
	private final Rect mLTabRect = new Rect();
	private final Rect mRTabRect = new Rect();
	private final Rect mBallRect = new Rect();
	private final SurfaceHolder mSurfaceHolder;
	private long mUserPosNotifyStartTime = 0;
	private static final long USER_NOTIFY_ANIMATION_LENGTH = 500;
	public int mArcadeScore;
	public int mArcadeLifes;

	public static boolean intersects(final Rect a, final Rect b) {
		return a.left < b.right && b.left < a.right && a.top < b.bottom
				&& b.top < a.bottom;
	}

	class GameTimerEvent extends Thread {

		@Override
		public void run() {
			while (mGameThreadLive) {
				final long timeShotBegin = SystemClock.uptimeMillis();
				final long timeDelta = mLastShotBegin == 0 ? GAME_CYCLE_PERIOD
						: (timeShotBegin - mLastShotBegin);
				long timeDeltaForUpdate = timeDelta;
				if (timeDeltaForUpdate > 100) {
					timeDeltaForUpdate = 100;
				}

				if (mGameTimerActive) {
					if (!((!misRunning && mcputab != TAB_BOTH) || mIsDrawing)) {

						mxballpos += mxballspeed * timeDeltaForUpdate
								/ GAME_CYCLE_PERIOD;
						myballpos += myballspeed * timeDeltaForUpdate
								/ GAME_CYCLE_PERIOD;
						calcBallRect(mBallRect);

						// collapse with up and down
						if (mBallRect.top <= 0 && myballspeed < 0.0f) {
							myballpos = 0;
							myballspeed = -myballspeed;
							calcBallRect(mBallRect);
							playSnd(SOUND_REF);
						}
						if (mBallRect.bottom >= getHeight()
								&& myballspeed > 0.0f) {
							myballpos = 1.0f;
							myballspeed = -myballspeed;
							calcBallRect(mBallRect);
							playSnd(SOUND_REF);
						}

						calclTabRect(mLTabRect);
						calcrTabRect(mRTabRect);
						if (mLTabRect.left > mBallRect.left
								&& mxballspeed < 0.0f) { //speed more that tab width and stop ball on network game
							final boolean net = mcputab == TAB_NONE
									&& mPlayerTabPos == TAB_RIGHT;
							if (!net || mLTabRect.right > mBallRect.left) {
								final float newxpos = 0.0f + 0.95f * TAB_WIDTH;
								final double d = (newxpos) / mxballspeed;
								final float xtmp = mxballpos;
								final float ytmp = myballpos;
								myballpos = (float) (myballpos + d
										* myballspeed);
								mxballpos = newxpos;
								calcBallRect(mBallRect);
								if (!intersects(mBallRect, mLTabRect)) {
									myballpos = ytmp;
									mxballpos = xtmp;
									calcBallRect(mBallRect);
								}
							}
							if (net) {
								mxballspeed = 0.0f;
								myballspeed = 0.0f;
							}
						}
						if (mRTabRect.right < mBallRect.right
								&& mxballspeed > 0.0f) { //speed more that tab width and stop ball on network game
							final boolean net = mcputab == TAB_NONE
									&& mPlayerTabPos == TAB_LEFT;
							if (!net || mRTabRect.left < mBallRect.right) {
								final float newxpos = 1.0f - 0.95f * TAB_WIDTH;
								final double d = (newxpos - 1.0f) / mxballspeed;
								final float xtmp = mxballpos;
								final float ytmp = myballpos;
								myballpos = (float) (myballpos - d
										* myballspeed);
								mxballpos = newxpos;
								calcBallRect(mBallRect);
								if (!intersects(mBallRect, mRTabRect)) {
									myballpos = ytmp;
									mxballpos = xtmp;
									calcBallRect(mBallRect);
								}
							}
							if (net) {
								mxballspeed = 0.0f;
								myballspeed = 0.0f;
							}
						}
						// collapse with tabs and goals
						if (mxballspeed < 0.0f
								&& !(mcputab == TAB_NONE && mPlayerTabPos == TAB_RIGHT)) {
							if (intersects(mBallRect, mLTabRect)) {
								int d = mLTabRect.centerY()
										- mBallRect.centerY();
								if (Math.abs(d) <= 1) {
									d = 0;
								}
								myballspeed = myballspeed + mxballspeed / 2 * d
										/ mBallRect.height();
								if (Math.abs(mxballspeed) > MAX_SUITABLE_SPEED) {
									mxballspeed = -mxballspeed
											* ((Math.abs(mxballspeed) < MAX_SPEED) ? COLLAPSE_MULTI_HI
													: 1.0f);
								} else {
									mxballspeed = -mxballspeed
											* COLLAPSE_MULTI
											- (mxballspeed * 2.0f)
											* (mlastspeedpost != -1.0f ? Math
													.abs(mlastspeedpost
															- mleftTabPosition)
													: 0.0f);
								}
								if (mcpunumber == LAN_MULTIPLAYER_SERVER
										|| mcpunumber == LAN_MULTIPLAYER_CLIENT) {
									prepareDataAndSend(new TennisPacket(
											TennisPacket.TYPE_BALL_REFLECTED));
								}
								playSnd(SOUND_REF);
							} else if (mLTabRect.left > mBallRect.left) { // goal check
								lTabGoal();
								if (mcpunumber == LAN_MULTIPLAYER_SERVER
										|| mcpunumber == LAN_MULTIPLAYER_CLIENT) {
									prepareDataAndSend(new TennisPacket(
											TennisPacket.TYPE_BALL_GOAL));
								}
							}

						} else if (mxballspeed > 0.0f
								&& !(mcputab == TAB_NONE && mPlayerTabPos == TAB_LEFT)) {
							if (intersects(mBallRect, mRTabRect)) {
								int d = mRTabRect.centerY()
										- mBallRect.centerY();
								if (Math.abs(d) <= 1) {
									d = 0;
								}
								myballspeed = myballspeed - mxballspeed / 2 * d
										/ mBallRect.height();
								if (Math.abs(mxballspeed) > MAX_SUITABLE_SPEED) {
									mxballspeed = -mxballspeed
											* ((Math.abs(mxballspeed) < MAX_SPEED) ? COLLAPSE_MULTI_HI
													: 1.0f);
								} else {
									mxballspeed = -mxballspeed
											* COLLAPSE_MULTI
											- (mxballspeed * 2.0f)
											* (mlastspeedpost != -1.0f ? Math
													.abs(mlastspeedpost
															- mrightTabPosition)
													: 0.0f);
								}
								if (mcpunumber == LAN_MULTIPLAYER_SERVER
										|| mcpunumber == LAN_MULTIPLAYER_CLIENT) {
									prepareDataAndSend(new TennisPacket(
											TennisPacket.TYPE_BALL_REFLECTED));
								}
								playSnd(SOUND_REF);
							} else if (mRTabRect.right < mBallRect.right) { // goal check
								rTabGoal();
								if (mcpunumber == LAN_MULTIPLAYER_SERVER
										|| mcpunumber == LAN_MULTIPLAYER_CLIENT) {
									prepareDataAndSend(new TennisPacket(
											TennisPacket.TYPE_BALL_GOAL));
								}
							}
						}

						// calc cpu movie
						if ((mcputab & TAB_LEFT) == TAB_LEFT
								&& mxballspeed < 0.0f) {
							cpumove(mLTabRect, mBallRect);
						}
						if ((mcputab & TAB_RIGHT) == TAB_RIGHT
								&& mxballspeed > 0.0f) {
							cpumove(mRTabRect, mBallRect);
						}

						//human tilt move
						if (Math.abs(mtildAngle) > TILT_MIN_ANGLE
								&& GameSettings.get().getInput() == GameSettings.INPUT.INPUT_TILT2) {
							final float GOOD_SENS_COEF = 1.5f;
							float delta = mtildAngle;
							if (Math.abs(delta) > GameSettings.get()
									.getSensivityTilt() * GOOD_SENS_COEF) {
								delta = Math.signum(delta)
										* GameSettings.get().getSensivityTilt()
										* GOOD_SENS_COEF;
							}
							delta = delta
									/ (GOOD_SENS_COEF * GameSettings.get()
											.getSensivityTilt());
							if (mPlayerTabPos == TAB_RIGHT) {
								mrightTabPosition += delta * TAB_SPEED * 8;
								checkTabPos();
								sendLanTabPos();
							} else if (mPlayerTabPos == TAB_LEFT) {
								mleftTabPosition += delta * TAB_SPEED * 8;
								checkTabPos();
								sendLanTabPos();
							}
						}

						if (mxballpos < -mxballspeed * 20 && mxballspeed < 0.0f) { // checking very fast speed to calc goal
							if (mlastspeedpost == -1.0f) {
								mlastspeedpost = mleftTabPosition;
							}
						} else if ((1.0f - mxballpos) < mxballspeed * 20
								&& mxballspeed > 0.0f) {
							if (mlastspeedpost == -1.0f) {
								mlastspeedpost = mrightTabPosition;
							}
						} else {
							mlastspeedpost = -1.0f;
						}

					}
					calclTabRect(mDrawableLTabRect);
					calcrTabRect(mDrawableRTabRect);
					calcBallRect(mDrawableBallRect);
					if (!mGameThreadLive) {
						return;
					}

					Canvas c = null;
					try {
						c = mSurfaceHolder.lockCanvas();
						synchronized (mSurfaceHolder) {
							doDraw(c);
						}
					} finally {
						if (c != null) {
							mSurfaceHolder.unlockCanvasAndPost(c);
						}
					}

				} else {
					if (mGameThreadLive && mLineColorSt != 0 && !mIsDrawing) {
						Canvas c = null;
						try {
							c = mSurfaceHolder.lockCanvas();
							synchronized (mSurfaceHolder) {
								doDraw(c);
							}
						} finally {
							if (c != null) {
								mSurfaceHolder.unlockCanvasAndPost(c);
							}
						}
					}
				}

				mLastShotBegin = timeShotBegin;

				final long timeShortEnd = SystemClock.uptimeMillis();
				final long timeShotDelta = timeShortEnd - timeShotBegin;

				if (timeShotDelta < GAME_CYCLE_PERIOD && timeShotDelta > 0) {
					try {
						Thread.sleep(GAME_CYCLE_PERIOD - timeShotDelta);
					} catch (final InterruptedException e) {
						e.printStackTrace();
					}
				}

			}
		}

	}

	private MediaPlayer mMediaPlayer = null;

	public TennisView(final Context context, final AttributeSet attrs) {
		super(context, attrs);
		mSurfaceHolder = getHolder();
		mSurfaceHolder.addCallback(this);
		setKeepScreenOn(true);
		m_soundPool = new SoundPool(8, android.media.AudioManager.STREAM_MUSIC,
				0);
		SOUND_START = m_soundPool.load(context, R.raw.start, 1);
		SOUND_FINISH = m_soundPool.load(context, R.raw.end, 1);
		SOUND_REF1 = m_soundPool.load(context, R.raw.r1, 1);
		SOUND_REF2 = m_soundPool.load(context, R.raw.r2, 1);
		SOUND_REF3 = m_soundPool.load(context, R.raw.r3, 1);
		SOUND_REF4 = m_soundPool.load(context, R.raw.r4, 1);

		mlowPassTiltFilter = new float[5];
		for (int i = 0; i < mlowPassTiltFilter.length; i++) {
			mlowPassTiltFilter[i] = 0.0f;
		}
		final Date d = new Date();
		random = new Random(d.getTime());
		initGame();
		mPaint.setAntiAlias(true);
	}

	public void running() {
		mGameTimerActive = true;
		if (mMediaPlayer == null) {
			mMediaPlayer = MediaPlayer.create(getContext(), R.raw.slt);
			mMediaPlayer.start();
			mMediaPlayer.setLooping(true);
		}
	}

	public void stopping() {
		mGameTimerActive = false;
		if (mMediaPlayer != null) {
			mMediaPlayer.release();
			mMediaPlayer = null;
		}
	}

	protected void checkTabPos() {
		// check tab positions
		if (mleftTabPosition < 0.0f) {
			mleftTabPosition = 0;
		}
		if (mleftTabPosition > 1.0f) {
			mleftTabPosition = 1.0f;
		}
		if (mrightTabPosition < 0.0f) {
			mrightTabPosition = 0;
		}
		if (mrightTabPosition > 1.0f) {
			mrightTabPosition = 1.0f;
		}

	}

	public int getNumberOfCpu() {
		return mcpunumber;
	}

	private void cpumove(final Rect tabrect, final Rect br) {
		// detect 1st reflections
		int ypoint;
		if (mdifficulty == DIFFICULTY.DIFFICULTY_EASY) {
			ypoint = br.centerY();
		} else {
			ypoint = (int) ((myballpos + (mxballspeed < 0.0f ? mxballpos
					: (1.0f - mxballpos)) * myballspeed / Math.abs(mxballspeed)) * getHeight());
		}
		// detect reflections
		while ((ypoint < 0 || ypoint > getHeight())
				&& mdifficulty != DIFFICULTY.DIFFICULTY_NORMAL
				&& mdifficulty != DIFFICULTY.DIFFICULTY_EASY) {
			if (ypoint < 0) {
				ypoint = -ypoint;
			}
			if (ypoint > getHeight()) {
				ypoint = getHeight() - (ypoint - getHeight());
			}
		}
		if (ypoint <= 0 || ypoint > getHeight()) {
			ypoint = br.centerY();
		}
		if (myballspeed == 0.0f) {
			ypoint = (int) (myballpos * getHeight() + mFirstMove);
		}

		final int delta = Math.abs(ypoint - tabrect.centerY());
		if (delta > tabrect.height() / 20) {
			final float res = mTabCpuDifficultySpeed
					* Math.signum(ypoint - tabrect.centerY());
			if (mxballspeed < 0.0f) {
				mrightTabLock = false;
				if (delta > tabrect.height() / 2
						|| (myballspeed == 0.0f && delta > tabrect.height() / 5)) {
					mleftTabLock = false;
					mleftTabPosition += res;
				} else {
					mleftTabLock = true;
					if (!mleftTabLock) {
						mleftTabPosition += res;
					}
				}
			} else {
				mleftTabLock = false;
				if (delta > tabrect.height() / 2
						|| (myballspeed == 0.0f && delta > tabrect.height() / 5)) {
					mrightTabLock = false;
					mrightTabPosition += res;
				} else {
					mrightTabLock = true;
					if (!mrightTabLock) {
						mrightTabPosition += res;
					}
				}
			}
		}

	}

	public void leftMove(final float delta) {
		if (!misRunning) {
			return;
		}
		if (mcputab == TAB_BOTH) {
			return;
		}
		if (mPlayerTabPos == TAB_BOTH) {
			mleftTabPosition += delta;
			checkTabPos();
		}
		if (mPlayerTabPos == TAB_LEFT) {
			mleftTabPosition += delta;
			checkTabPos();
			sendLanTabPos();
		}
		if (mPlayerTabPos == TAB_RIGHT) {
			mrightTabPosition += delta;
			checkTabPos();
			sendLanTabPos();
		}
	}

	public void rightMove(final float delta) {
		if (!misRunning) {
			return;
		}
		if (mPlayerTabPos != TAB_BOTH) {
			return;
		}
		mrightTabPosition += delta;
		checkTabPos();
	}

	private void initGame() {
		mUserPosNotifyStartTime = SystemClock.uptimeMillis();
		mxballpos = 0.5f;
		myballpos = 0.5f;
		if (mPlayerTabPos != TAB_NONE || mcpunumber != LAN_MULTIPLAYER_CLIENT) {
			mxballspeed = INIT_BALL_SPEED;
		} else {
			mxballspeed = 0;
		}
		mleftTabPosition = 0.5f;
		mrightTabPosition = 0.5f;
		mLastSendedPos = -1.0f;
		leftTabScore = 0;
		rightTabScore = 0;
		if (misdemo) {
			myballspeed = random.nextFloat() * 2 * mxballspeed - mxballspeed;
			if (myballspeed == 0.0f) {
				myballspeed = 0.0005f;
			}
		} else {
			myballspeed = 0.0f;
		}
		if (random.nextBoolean()) {
			mxballspeed = -mxballspeed;
		}
		if (mchecker != null) {
			m_handler.post(mchecker);
		}
		if (!misdemo) {
			prepareDataAndSend(new TennisPacket()); // blank packet for init
		}
		mFirstMove = (0.5f - random.nextFloat()) * TAB_HEIGHT * getHeight()
				* 0.9f;
		playSnd(SOUND_START);
	}

	public class PlaySoundRunnable implements Runnable {
		int mSoundId;

		PlaySoundRunnable(final int soundId) {
			mSoundId = soundId;
		}

		public void run() {
			float rate = 1.0f;
			float vol = 1.0f;
			int snd = mSoundId;
			if (snd == SOUND_REF) {
				switch (random.nextInt(4)) {
				case 0:
					snd = SOUND_REF1;
					break;
				case 1:
					snd = SOUND_REF2;
					break;
				case 2:
					snd = SOUND_REF3;
					break;
				case 3:
				default:
					snd = SOUND_REF4;
					break;
				}
				rate = 0.8f + random.nextFloat() * 0.4f;
				vol = 0.4f + Math.abs(mxballspeed) / INIT_BALL_SPEED * 0.12f;

				if (vol > 1.0f) {
					vol = 1.0f;
				}
			}
			m_soundPool.play(snd, vol, vol, 0, 0, rate);
		}
	}

	public void playSnd(final int soundId) {
		if (GameSettings.get().getSFX() == GameSettings.SFX.SFX_ON
				&& mcputab != TAB_BOTH && !misdemo) {
			if (soundId == SOUND_START
					&& (leftTabScore >= SCORE_TO_WIN || rightTabScore >= SCORE_TO_WIN)) {
				return;
			}
			final PlaySoundRunnable helper = new PlaySoundRunnable(soundId);
			m_handler.post(helper);
		}
	}

	private float newspeed() {
		float newspd = Math.abs(mxballspeed) / 2;
		if (newspd > INIT_BALL_SPEED * 2) {
			newspd = INIT_BALL_SPEED * 2;
		} else if (newspd < INIT_BALL_SPEED) {
			newspd = INIT_BALL_SPEED;
		}
		return newspd;
	}

	private void lTabGoal() {
		if (misdemo) {
			initGame();
			return;
		}
		if (mcpunumber == ONE_PLAYER_ARCADE) {
			if (mPlayerTabPos == TAB_LEFT) {
				mArcadeLifes--;
			} else {
				mArcadeScore += 100;
			}
		} else {
			rightTabScore++;
		}
		mxballpos = 0.5f;
		myballpos = 0.5f;
		if (mPlayerTabPos != TAB_NONE || mcpunumber != LAN_MULTIPLAYER_CLIENT) {
			mxballspeed = -newspeed();
		} else {
			mxballspeed = 0;
		}
		myballspeed = 0.0f;
		if (mchecker != null) {
			m_handler.post(mchecker);
		}
		playSnd(SOUND_START);
	}

	private void rTabGoal() {
		if (misdemo) {
			initGame();
			return;
		}
		if (mcpunumber == ONE_PLAYER_ARCADE) {
			if (mPlayerTabPos == TAB_RIGHT) {
				mArcadeLifes--;
			} else {
				mArcadeScore += 100;
			}
		} else {
			leftTabScore++;
		}
		mxballpos = 0.5f;
		myballpos = 0.5f;
		if (mPlayerTabPos != TAB_NONE || mcpunumber != LAN_MULTIPLAYER_CLIENT) {
			mxballspeed = newspeed();
		} else {
			mxballspeed = 0;
		}
		myballspeed = 0.0f;
		if (mchecker != null) {
			m_handler.post(mchecker);
		}
		playSnd(SOUND_START);
	}

	public void calcMargins() {
		final int margin = (GameSettings.get().getInput() == GameSettings.INPUT.INPUT_HAND || mPlayerTabPos == TAB_BOTH) ? (int) (getWidth() * MARGIN_COEF)
				: MARGIN_DEFAULT;
		switch (mPlayerTabPos) {
		case TAB_BOTH:
			mlmargin = margin;
			mrmargin = margin;
			break;
		case TAB_LEFT:
			mlmargin = margin;
			mrmargin = MARGIN_DEFAULT;
			break;
		case TAB_RIGHT:
			mrmargin = margin;
			mlmargin = MARGIN_DEFAULT;
			break;
		case TAB_NONE:
			mlmargin = MARGIN_DEFAULT;
			mrmargin = MARGIN_DEFAULT;
			break;
		}
	}

	public void setNumberOfCpu(final int number) {
		Runtime.getRuntime().gc();
		switch (number) {
		case LAN_MULTIPLAYER_SERVER:
		case LAN_MULTIPLAYER_CLIENT:
			misdemo = false;
			mcputab = TAB_NONE;
			if (GameSettings.get().getPos() == GameSettings.POS.POS_LEFT) {
				mPlayerTabPos = TAB_LEFT;
			} else if (GameSettings.get().getPos() == GameSettings.POS.POS_RIGHT) {
				mPlayerTabPos = TAB_RIGHT;
			} else if (GameSettings.get().getPos() == GameSettings.POS.POS_SWAP) {
				if (number == LAN_MULTIPLAYER_SERVER) {
					if (GameSettings.get().getLPAndinvertLPAndCommit() == GameSettings.LASTPOS.LP_LEFT) {
						mPlayerTabPos = TAB_LEFT;
					} else {
						mPlayerTabPos = TAB_RIGHT;
					}
				} else {
					mPlayerTabPos = TAB_NONE;
				}
			}
			break;
		case 2:
			mcputab = TAB_BOTH;
			mPlayerTabPos = TAB_NONE;
			mdifficulty = GameSettings.DIFFICULTY.DIFFICULTY_NIGHTMARE;
			mTabCpuDifficultySpeed = TAB_SPEED * 6.0f;
			misdemo = true;
			break;
		case ONE_PLAYER_ARCADE:
			mArcadeLifes = 3;
			mArcadeScore = 0;
		case 1:
			mdifficulty = GameSettings.get().getDifficulty();
			mTabCpuDifficultySpeed = TAB_SPEED;
			if (mdifficulty == GameSettings.DIFFICULTY.DIFFICULTY_HARD) {
				mTabCpuDifficultySpeed = TAB_SPEED * 1.5f;
			} else if (mdifficulty == GameSettings.DIFFICULTY.DIFFICULTY_VERYHARD) {
				mTabCpuDifficultySpeed = TAB_SPEED * 3.0f;
			} else if (mdifficulty == GameSettings.DIFFICULTY.DIFFICULTY_NIGHTMARE) {
				mTabCpuDifficultySpeed = TAB_SPEED * 6.0f;
			}
			misdemo = false;
			switch (GameSettings.get().getPos()) {
			case POS_LEFT:
				mcputab = TAB_RIGHT;
				mPlayerTabPos = TAB_LEFT;
				break;
			case POS_RIGHT:
				mcputab = TAB_LEFT;
				mPlayerTabPos = TAB_RIGHT;
				break;
			case POS_SWAP:
				if (GameSettings.get().getLPAndinvertLPAndCommit() == GameSettings.LASTPOS.LP_LEFT) {
					mPlayerTabPos = TAB_LEFT;
					mcputab = TAB_RIGHT;
				} else {
					mPlayerTabPos = TAB_RIGHT;
					mcputab = TAB_LEFT;
				}
				break;
			}
			break;
		case 0:
			misdemo = false;
			mcputab = TAB_NONE;
			mPlayerTabPos = TAB_BOTH;
			break;
		}
		mcpunumber = number;
		calcMargins();
		initGame();
		if (mcpunumber == LAN_MULTIPLAYER_SERVER) {
			prepareDataAndSend(new TennisPacket());
		}
	}

	public void setRunning(final boolean isRunnning) {
		misRunning = isRunnning;
		mLineColorSt = 60;
	}

	public boolean isRunning() {
		return misRunning;
	}

	private void calcBallRect(final Rect r) {
		r.set((int) (mlmargin + (getWidth() - mlmargin - mrmargin - TAB_WIDTH
				* getWidth())
				* mxballpos),
				(int) ((getHeight() - TAB_WIDTH * getWidth()) * myballpos),
				(int) (mlmargin
						+ (getWidth() - mlmargin - mrmargin - TAB_WIDTH
								* getWidth()) * mxballpos + TAB_WIDTH
						* getWidth()), (int) ((getHeight() - TAB_WIDTH
						* getWidth())
						* myballpos + TAB_WIDTH * getWidth()));
	}

	private void calclTabRect(final Rect r) {
		final int tabHeight = (int) (getHeight() * TAB_HEIGHT);
		final int ltabPos = (int) ((getHeight() - tabHeight) * mleftTabPosition);
		r.set(mlmargin, ltabPos, mlmargin + (int) (TAB_WIDTH * getWidth()),
				ltabPos + tabHeight);
	}

	private void calcrTabRect(final Rect r) {
		final int tabHeight = (int) (getHeight() * TAB_HEIGHT);
		final int rtabPos = (int) ((getHeight() - tabHeight) * mrightTabPosition);
		r.set(getWidth() - (int) (TAB_WIDTH * getWidth()) - mrmargin, rtabPos,
				getWidth() - mrmargin, rtabPos + tabHeight);
	}

	private int mLineEffect = 0;
	private int mLineColor = 40;
	private int mLineColorSt = 0;
	private long mLinedrawTime = 0;
	private boolean mInc = true;
	final Paint mPaint = new Paint();

	private final Rect tmpRect = new Rect();

	protected void doDraw(final Canvas canvas) {
		if (!mGameThreadLive) {
			return;
			/*final long t = SystemClock.uptimeMillis();
			Log.i("tag", " " + (t - tt));
			tt = t;*/
		}

		mIsDrawing = true;

		//paint.setColor(COLOR_BACKGROUND);
		canvas.drawColor(COLOR_BACKGROUND);
		if (misdemo || mLineColorSt > 20 || !misRunning) {
			mPaint.setColor(mLineColor);
			final int lineHeight = getHeight() / 60;
			final long curTime = SystemClock.uptimeMillis();
			for (int i = -lineHeight; i < getHeight() + lineHeight; i = i
					+ lineHeight) {
				tmpRect.set(0, i - mLineEffect, getWidth(), i - mLineEffect
						+ lineHeight / 2);
				canvas.drawRect(tmpRect, mPaint);
			}
			if (curTime - mLinedrawTime > 30) {
				mLineEffect += 1;

				if (mLineEffect > lineHeight) {
					mLineEffect = 0;
				}
				if (mLineEffect % 3 == 0) {
					mLinedrawTime = curTime;
					if (misdemo || !misRunning) {
						if (mInc) {
							mLineColorSt += 1 + random.nextInt(5);
						} else {
							mLineColorSt -= 1 + random.nextInt(5);
						}
						if (mLineColorSt > 80) {
							mInc = false;
						}
						if (mLineColorSt < 20) {
							mInc = true;
						}
					} else {

						mLineColorSt -= 5;
						if (mLineColorSt < 20) {
							mLineColorSt = 0;
						}
					}
					mLineColor = 0xFF000000 + mLineColorSt + mLineColorSt * 256
							+ mLineColorSt * 65536;
				}

			}
		}
		mPaint.setColor(COLOR_TAB);

		float animationKoef = 0.0f;
		boolean isAnim = false;
		if (mUserPosNotifyStartTime != 0) {
			animationKoef = 1.0f
					- (SystemClock.uptimeMillis() - mUserPosNotifyStartTime)
					/ ((float) USER_NOTIFY_ANIMATION_LENGTH);
			if (animationKoef < 0.0f) {
				animationKoef = 0.0f;
				mUserPosNotifyStartTime = 0;
			}
			animationKoef = (float) Math.sin(animationKoef * Math.PI);
			animationKoef *= animationKoef;
			isAnim = true;
		}

		if (isAnim && (mPlayerTabPos == TAB_LEFT || mPlayerTabPos == TAB_BOTH)) {
			final int vDelta = (int) (mDrawableLTabRect.height() * animationKoef);
			final int hDelta = (int) (mDrawableLTabRect.height()
					* animationKoef / 5);
			canvas.drawRect(mDrawableLTabRect.left, mDrawableLTabRect.top
					- vDelta, mDrawableLTabRect.right + hDelta,
					mDrawableLTabRect.bottom + vDelta, mPaint);
		} else {
			canvas.drawRect(mDrawableLTabRect, mPaint);
		}

		if (isAnim && (mPlayerTabPos == TAB_RIGHT || mPlayerTabPos == TAB_BOTH)) {
			final int vDelta = (int) (mDrawableRTabRect.height() * animationKoef);
			final int hDelta = (int) (mDrawableRTabRect.height()
					* animationKoef / 5);

			canvas.drawRect(mDrawableRTabRect.left - hDelta,
					mDrawableRTabRect.top - vDelta, mDrawableRTabRect.right,
					mDrawableRTabRect.bottom + vDelta, mPaint);
		} else {
			canvas.drawRect(mDrawableRTabRect, mPaint);
		}

		canvas.drawRect(mDrawableBallRect, mPaint);

		mIsDrawing = false;
	}

	public void setTildAngle(final float angle) {
		if (!misRunning) {
			return;
		}
		if (GameSettings.get().getInput() == GameSettings.INPUT.INPUT_TILT2) {
			mtildAngle = angle;
		} else if (GameSettings.get().getInput() == GameSettings.INPUT.INPUT_TILT) {
			mlowPassTiltFilter[mlowPassTiltFilterIndex] = angle;
			mlowPassTiltFilterIndex++;
			if (mlowPassTiltFilterIndex >= mlowPassTiltFilter.length) {
				mlowPassTiltFilterIndex = 0;
			}

			final long curTime = SystemClock.uptimeMillis();
			if (curTime - lastTime > 20) {
				float sum = 0.0f;
				for (int i = 0; i < mlowPassTiltFilter.length; i++) {
					sum += mlowPassTiltFilter[i];
				}
				sum = sum / mlowPassTiltFilter.length;

				if (Math.abs(sum) > GameSettings.get().getSensivityTilt()) {
					sum = Math.signum(sum)
							* GameSettings.get().getSensivityTilt();
				}
				if (mPlayerTabPos == TAB_RIGHT) {
					mrightTabPosition = 0.5f + sum
							/ GameSettings.get().getSensivityTilt();
					checkTabPos();
					sendLanTabPos();
				}
				if (mPlayerTabPos == TAB_LEFT) {
					mleftTabPosition = 0.5f + sum
							/ GameSettings.get().getSensivityTilt();
					checkTabPos();
					sendLanTabPos();
				}
				lastTime = curTime;
			}
		}
	}

	public void setConfirmer(final Runnable checker) {
		mchecker = checker;
	}

	public GameSettings.DIFFICULTY getDifficulty() {
		return mdifficulty;
	}

	public boolean isDemo() {
		return misdemo;
	}

	public int playerTabPos() {
		return mPlayerTabPos;
	}

	public void surfaceChanged(final SurfaceHolder holder, final int format,
			final int width, final int height) {

	}

	public void surfaceCreated(final SurfaceHolder holder) {
		mGameThreadLive = true;
		mGameTimer = new GameTimerEvent();
		mGameTimer.start();

	}

	public void surfaceDestroyed(final SurfaceHolder holder) {
		mGameThreadLive = false;
		try {
			mGameTimer.join();
		} catch (final InterruptedException e) {
			mGameTimer.interrupt();
		}
		mGameTimer = null;
	}

	// ---------------- LAN ----------------------------------------------------------------------------------------------------------

	private LanGame<TennisPacket> mLan = null;

	public void setLan(final LanGame<TennisPacket> lan) {
		mLan = lan;
	}

	private long mNextTimeSendedPos = 0;
	private final static long MIN_SENDING_INTERVAL = 40;
	private float mLastSendedPos = -1.0f;

	private void sendLanTabPos() {
		float pos = -1.0f;
		if (mPlayerTabPos == TAB_LEFT) {
			pos = mleftTabPosition;
		}
		if (mPlayerTabPos == TAB_RIGHT) {
			pos = mrightTabPosition;
		}
		final long curTime = SystemClock.uptimeMillis();
		if (mLan == null || mNextTimeSendedPos > curTime
				|| pos == mLastSendedPos) {
			return;
		}
		mLastSendedPos = pos;
		mNextTimeSendedPos = curTime + MIN_SENDING_INTERVAL;
		prepareDataAndSend(new TennisPacket(TennisPacket.TYPE_TAB_MOVE));
	}

	public TennisPacket mLastActualSendedPack = null;
	public int mLastSendedPacketIncrementalId = 1;
	public int mLastRecivedPackedId = 1;
	public long mLastRecivedPackedTime = 1;

	private void prepareDataAndSend(final TennisPacket pak) {
		if (mLan == null
				|| (mcpunumber != LAN_MULTIPLAYER_SERVER && mcpunumber != LAN_MULTIPLAYER_CLIENT)) {
			return;
		}
		if (mPlayerTabPos == TAB_LEFT) {
			pak.sPos = TennisPacket.SERVER_POS_LEFT;
		} else if (mPlayerTabPos == TAB_RIGHT) {
			pak.sPos = TennisPacket.SERVER_POS_RIGHT;
		}
		if (mPlayerTabPos == TAB_LEFT) {
			if (pak.type != TennisPacket.TYPE_TAB_MOVE) {
				float k = 1.0f;
				if (mxballspeed != 0.0f) {
					final float t = GAME_CYCLE_PERIOD * (1.0f - mxballpos)
							/ Math.abs(mxballspeed);
					k = t / (t + mNetworkDelay);
					if (k < 0.9f) {
						k = 0.9f;
						if (Math.abs(mxballspeed / k) > MAX_SUITABLE_SPEED) {
							k = 1.0f;
						}
					}
				}
				//Log.i("tag","l "+ (1.0f-mxballpos)+" k="+k);
				pak.cScore = (byte) rightTabScore;
				pak.sScore = (byte) leftTabScore;
				pak.xBallPos = mxballpos;
				pak.yBallPos = myballpos;
				pak.xBallSpeed = mxballspeed / k;
				pak.yBallSpeed = myballspeed / k;
			}
			pak.tabPos = mleftTabPosition;
		}
		if (mPlayerTabPos == TAB_RIGHT) {
			if (pak.type != TennisPacket.TYPE_TAB_MOVE) {
				float k = 1.0f;
				if (mxballspeed != 0.0f) {
					final float t = GAME_CYCLE_PERIOD * (mxballpos)
							/ Math.abs(mxballspeed);
					k = t / (t + mNetworkDelay);
					if (k < 0.9f) {
						k = 0.9f;
						if (Math.abs(mxballspeed / k) > MAX_SUITABLE_SPEED) {
							k = 1.0f;
						}
					}
				}
				//Log.i("tag", "r "+(mxballpos)+" k="+k);
				pak.cScore = (byte) leftTabScore;
				pak.sScore = (byte) rightTabScore;
				pak.xBallPos = 1.0f - mxballpos;
				pak.yBallPos = myballpos;
				pak.xBallSpeed = -mxballspeed / k;
				pak.yBallSpeed = myballspeed / k;
			}
			pak.tabPos = mrightTabPosition;
		}
		if (pak.type != TennisPacket.TYPE_TAB_MOVE) {
			pak.id = mLastSendedPacketIncrementalId;
			mLastSendedPacketIncrementalId++;
		}
		mLan.send(pak);

		if (pak.type != TennisPacket.TYPE_TAB_MOVE) {
			mLastActualSendedPack = (TennisPacket) pak.clone();
			mLastActualSendedPack.recoveryType = pak.type;
		}
	}

	public void setLanData(final TennisPacket pak) {
		if (mPlayerTabPos == TAB_NONE) {
			if (pak.sPos == TennisPacket.SERVER_POS_LEFT) {
				mPlayerTabPos = TAB_RIGHT;
				calcMargins();
				mUserPosNotifyStartTime = SystemClock.uptimeMillis();
			}
			if (pak.sPos == TennisPacket.SERVER_POS_RIGHT) {
				mPlayerTabPos = TAB_LEFT;
				calcMargins();
				mUserPosNotifyStartTime = SystemClock.uptimeMillis();
			}
		} else if (pak.type == TennisPacket.TYPE_TAB_MOVE) {
			if (mPlayerTabPos == TAB_LEFT) {
				mrightTabPosition = pak.tabPos;
			}
			if (mPlayerTabPos == TAB_RIGHT) {
				mleftTabPosition = pak.tabPos;
			}
			return;
		}
		mLastRecivedPackedId = pak.id;
		mLastRecivedPackedTime = SystemClock.uptimeMillis();
		if (mcpunumber == LAN_MULTIPLAYER_CLIENT) {

			if (mPlayerTabPos == TAB_LEFT) {
				leftTabScore = pak.cScore;
				rightTabScore = pak.sScore;
			} else if (mPlayerTabPos == TAB_RIGHT) {
				leftTabScore = pak.sScore;
				rightTabScore = pak.cScore;
			}
			loadBallData(pak);

			switch (pak.type) {
			case TennisPacket.TYPE_BALL_GOAL:
				playSnd(SOUND_START);
				if (mchecker != null) {
					m_handler.post(mchecker);
				}
				break;
			case TennisPacket.TYPE_BALL_REFLECTED:
				playSnd(SOUND_REF);
				break;
			}

		} else if (mcpunumber == LAN_MULTIPLAYER_SERVER) {

			switch (pak.type) {
			case TennisPacket.TYPE_BALL_GOAL:
				if (mPlayerTabPos == TAB_LEFT) {
					rTabGoal();
				}
				if (mPlayerTabPos == TAB_RIGHT) {
					lTabGoal();
				}
				loadBallData(pak);
				break;
			case TennisPacket.TYPE_BALL_REFLECTED:
				loadBallData(pak);
				playSnd(SOUND_REF);
				break;
			}

		}
	}

	private void loadBallData(final TennisPacket pak) {
		if (mPlayerTabPos == TAB_LEFT) {
			mxballpos = (1.0f - pak.xBallPos);
			myballpos = pak.yBallPos;
			mxballspeed = -pak.xBallSpeed;
			myballspeed = pak.yBallSpeed;
		} else if (mPlayerTabPos == TAB_RIGHT) {
			mxballpos = pak.xBallPos;
			myballpos = pak.yBallPos;
			mxballspeed = pak.xBallSpeed;
			myballspeed = pak.yBallSpeed;
		}
	}

	public boolean isStack(final int id) {
		return (id > mLastRecivedPackedId || (mxballspeed == 0.0f && SystemClock
				.uptimeMillis() - mLastRecivedPackedTime > 1200));
	}
}
