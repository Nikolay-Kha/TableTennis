package com.greenlogger.tennis;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.hardware.Sensor;
import android.hardware.SensorManager;

public class GameSettings {
	static private GameSettings m_settings = null;
	public static final String SAVENAME = "tennis_game_settings";
	public static final String SAVE_SFX = "SFX";
	public static final String SAVE_PLAYERS = "Players";
	public static final String SAVE_DIFFICULTY = "Difficulty";
	public static final String SAVE_LASTPOS = "LastPos";
	public static final String SAVE_INPUT = "Control";
	public static final String SAVE_POS = "Pos";
	public static final String SAVE_DEFAULT_TILT = "DefaultTilt3";
	public static final String SAVE_SENSIVITY_TILT = "TiltSensivity";
	public static final String SAVE_RUNNING_COUNT = "RunningCount";
	public static final int MAX_RUNNINGCOUNT = 7;

	public enum SFX {
		SFX_ON, SFX_OFF
	};

	public enum PLAYERS {
		PLAYERS_ONE, PLAYERS_TWO, PLAYERS_BLUETOOTH, PLAYERS_WIFI, PLAYERS_ARCADE
	};

	public enum DIFFICULTY {
		DIFFICULTY_EASY, DIFFICULTY_NORMAL, DIFFICULTY_HARD, DIFFICULTY_VERYHARD, DIFFICULTY_NIGHTMARE
	};

	public enum LASTPOS {
		LP_LEFT, LP_RIGHT
	};

	public enum INPUT {
		INPUT_HAND, INPUT_TILT, INPUT_TILT2
	};

	public enum POS {
		POS_LEFT, POS_RIGHT, POS_SWAP
	};

	private final SharedPreferences m_sharedsettings;
	private SFX m_sfx;
	private PLAYERS m_players;
	private DIFFICULTY m_difficulty;
	private LASTPOS m_lastpos;
	private POS m_pos;
	private INPUT m_input;
	private final Editor m_editor;
	private float m_defaultTilt;
	private float m_sensivityTilt;

	private GameSettings(final Context context) {
		m_sharedsettings = context.getSharedPreferences(SAVENAME,
				Context.MODE_PRIVATE);
		if (m_sharedsettings.getBoolean(SAVE_SFX, true)) {
			m_sfx = SFX.SFX_ON;
		} else {
			m_sfx = SFX.SFX_OFF;
		}
		if (m_sharedsettings.getInt(SAVE_PLAYERS,
				PLAYERS.PLAYERS_ARCADE.hashCode()) == PLAYERS.PLAYERS_ARCADE
				.hashCode()) {
			m_players = PLAYERS.PLAYERS_ARCADE;
		} else if (m_sharedsettings.getInt(SAVE_PLAYERS,
				PLAYERS.PLAYERS_ARCADE.hashCode()) == PLAYERS.PLAYERS_ONE
				.hashCode()) {
			m_players = PLAYERS.PLAYERS_ONE;
		} else if (m_sharedsettings.getInt(SAVE_PLAYERS,
				PLAYERS.PLAYERS_ARCADE.hashCode()) == PLAYERS.PLAYERS_BLUETOOTH
				.hashCode()) {
			m_players = PLAYERS.PLAYERS_BLUETOOTH;
		} else if (m_sharedsettings.getInt(SAVE_PLAYERS,
				PLAYERS.PLAYERS_ARCADE.hashCode()) == PLAYERS.PLAYERS_WIFI
				.hashCode()) {
			m_players = PLAYERS.PLAYERS_WIFI;
		} else {
			m_players = PLAYERS.PLAYERS_TWO;
		}
		if (m_sharedsettings.getInt(SAVE_DIFFICULTY,
				DIFFICULTY.DIFFICULTY_NORMAL.hashCode()) == DIFFICULTY.DIFFICULTY_EASY
				.hashCode()) {
			m_difficulty = DIFFICULTY.DIFFICULTY_EASY;
		} else if (m_sharedsettings.getInt(SAVE_DIFFICULTY,
				DIFFICULTY.DIFFICULTY_NORMAL.hashCode()) == DIFFICULTY.DIFFICULTY_NORMAL
				.hashCode()) {
			m_difficulty = DIFFICULTY.DIFFICULTY_NORMAL;
		} else if (m_sharedsettings.getInt(SAVE_DIFFICULTY,
				DIFFICULTY.DIFFICULTY_NORMAL.hashCode()) == DIFFICULTY.DIFFICULTY_HARD
				.hashCode()) {
			m_difficulty = DIFFICULTY.DIFFICULTY_HARD;
		} else if (m_sharedsettings.getInt(SAVE_DIFFICULTY,
				DIFFICULTY.DIFFICULTY_NORMAL.hashCode()) == DIFFICULTY.DIFFICULTY_VERYHARD
				.hashCode()) {
			m_difficulty = DIFFICULTY.DIFFICULTY_VERYHARD;
		} else {
			m_difficulty = DIFFICULTY.DIFFICULTY_NIGHTMARE;
		}

		if (m_sharedsettings.getInt(SAVE_LASTPOS, LASTPOS.LP_LEFT.hashCode()) == LASTPOS.LP_LEFT
				.hashCode()) {
			m_lastpos = LASTPOS.LP_LEFT;
		} else {
			m_lastpos = LASTPOS.LP_RIGHT;
		}

		if (m_sharedsettings.getInt(SAVE_INPUT, INPUT.INPUT_HAND.hashCode()) == INPUT.INPUT_HAND
				.hashCode()) {
			m_input = INPUT.INPUT_HAND;
		} else if (m_sharedsettings.getInt(SAVE_INPUT,
				INPUT.INPUT_TILT.hashCode()) == INPUT.INPUT_HAND.hashCode()) {
			m_input = INPUT.INPUT_TILT;
		} else {
			m_input = INPUT.INPUT_TILT2;
		}
		final SensorManager sm = (SensorManager) context
				.getSystemService(Context.SENSOR_SERVICE);
		if (sm.getSensorList(Sensor.TYPE_ACCELEROMETER).size() == 0) {
			m_input = INPUT.INPUT_HAND;
		}

		if (m_sharedsettings.getInt(SAVE_POS, POS.POS_SWAP.hashCode()) == POS.POS_LEFT
				.hashCode()) {
			m_pos = POS.POS_LEFT;
		} else if (m_sharedsettings.getInt(SAVE_POS, POS.POS_SWAP.hashCode()) == POS.POS_RIGHT
				.hashCode()) {
			m_pos = POS.POS_RIGHT;
		} else {
			m_pos = POS.POS_SWAP;
		}

		m_defaultTilt = m_sharedsettings.getFloat(SAVE_DEFAULT_TILT, -45.0f);
		m_sensivityTilt = m_sharedsettings.getFloat(SAVE_SENSIVITY_TILT, 35.0f);

		m_editor = m_sharedsettings.edit();

	}

	public static void init(final Context context) {
		m_settings = new GameSettings(context);
	}

	public static GameSettings get() {
		return m_settings;
	}

	public SFX getSFX() {
		return m_sfx;
	}

	public PLAYERS getPlayers() {
		return m_players;
	}

	public DIFFICULTY getDifficulty() {
		return m_difficulty;
	}

	public LASTPOS getLastPos() {
		return m_lastpos;
	}

	public POS getPos() {
		return m_pos;
	}

	public INPUT getInput() {
		return m_input;
	}

	public float getDefaultTilt() {
		return m_defaultTilt;
	}

	public float getSensivityTilt() {
		return m_sensivityTilt;
	}

	public void setSFX(final SFX sfx) {
		m_sfx = sfx;
		switch (sfx) {
		case SFX_ON:
			m_editor.putBoolean(SAVE_SFX, true);
			break;
		case SFX_OFF:
			m_editor.putBoolean(SAVE_SFX, false);
			break;
		}
	}

	public void setPlayers(final PLAYERS players) {
		m_players = players;
		m_editor.putInt(SAVE_PLAYERS, players.hashCode());
	}

	public void setDifficulty(final DIFFICULTY difficulty) {
		m_difficulty = difficulty;
		m_editor.putInt(SAVE_DIFFICULTY, difficulty.hashCode());
	}

	public LASTPOS getLPAndinvertLPAndCommit() {
		if (m_lastpos == LASTPOS.LP_LEFT) {
			m_lastpos = LASTPOS.LP_RIGHT;
			m_editor.putInt(SAVE_LASTPOS, LASTPOS.LP_RIGHT.hashCode());
			commit();
			return LASTPOS.LP_LEFT;
		} else {
			m_lastpos = LASTPOS.LP_LEFT;
			m_editor.putInt(SAVE_LASTPOS, LASTPOS.LP_LEFT.hashCode());
			commit();
			return LASTPOS.LP_RIGHT;
		}
	}

	public void setInput(final INPUT input) {
		m_input = input;
		m_editor.putInt(SAVE_INPUT, input.hashCode());
	}

	public void setPos(final POS pos) {
		m_pos = pos;
		m_editor.putInt(SAVE_POS, pos.hashCode());
	}

	public void commit() {
		m_editor.commit();
	}

	public void setSensivityTilt(final float sens) {
		m_sensivityTilt = sens;
		m_editor.putFloat(SAVE_SENSIVITY_TILT, sens);
	}

	public void setAndCommitDefaultTilt(final float angle) {
		m_defaultTilt = angle;
		m_editor.putFloat(SAVE_DEFAULT_TILT, angle);
		commit();
	}

	public void incrementRunningCount() {
		int count = m_sharedsettings.getInt(SAVE_RUNNING_COUNT, 0);
		count++;
		if (count <= MAX_RUNNINGCOUNT + 1) {
			m_editor.putInt(SAVE_RUNNING_COUNT, count).commit();
		}
	}

	public boolean isRunningCountRich() {
		final int count = m_sharedsettings.getInt(SAVE_RUNNING_COUNT, 0);
		return count == MAX_RUNNINGCOUNT;
	}

	public void resetRunningCount() {
		m_editor.putInt(SAVE_RUNNING_COUNT, 0).commit();
	}

}
