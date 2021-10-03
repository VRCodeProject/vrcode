#pragma clang diagnostic push
#pragma ide diagnostic ignored "readability-non-const-parameter"
#pragma ide diagnostic ignored "OCDFAInspection"
#include <stdlib.h>
#include <string.h>
#include "input-event-codes.h"
#include "android-keycodes.h"
#include "evdev-keycodes.h"

#define KEYCODE1(c) case ANDROID_KEYCODE_##c : *eventCode =  KEY_##c; return 1
#define KEYCODE2(kc, ec) case ANDROID_KEYCODE_##kc : *eventCode = KEY_##ec; return 1
#define KEYCODE2shift(kc, ec) case ANDROID_KEYCODE_##kc : *eventCode = KEY_##ec; *shift = 1; return 1
int android_keycode_to_linux_event_code(int keyCode, int *eventCode, int *shift) {
	//if (!eventCode || !shift) return;
	switch (keyCode) {
		KEYCODE1(1);
		KEYCODE1(2);
		KEYCODE1(3);
		KEYCODE1(4);
		KEYCODE1(5);
		KEYCODE1(6);
		KEYCODE1(7);
		KEYCODE1(8);
		KEYCODE1(9);
		KEYCODE1(0);
		KEYCODE1(A);
		KEYCODE1(B);
		KEYCODE1(C);
		KEYCODE1(D);
		KEYCODE1(E);
		KEYCODE1(F);
		KEYCODE1(G);
		KEYCODE1(H);
		KEYCODE1(I);
		KEYCODE1(J);
		KEYCODE1(K);
		KEYCODE1(L);
		KEYCODE1(M);
		KEYCODE1(N);
		KEYCODE1(O);
		KEYCODE1(P);
		KEYCODE1(Q);
		KEYCODE1(R);
		KEYCODE1(S);
		KEYCODE1(T);
		KEYCODE1(U);
		KEYCODE1(V);
		KEYCODE1(W);
		KEYCODE1(X);
		KEYCODE1(Y);
		KEYCODE1(Z);
		KEYCODE1(COMMA);
		KEYCODE2(PERIOD, DOT);
		KEYCODE2(DPAD_UP, UP);
		KEYCODE2(DPAD_LEFT, LEFT);
		KEYCODE2(DPAD_DOWN, DOWN);
		KEYCODE2(DPAD_RIGHT, RIGHT);
		KEYCODE2(DPAD_CENTER, ENTER);
		KEYCODE2(ALT_LEFT, LEFTALT);
		KEYCODE2(ALT_RIGHT, RIGHTALT);
		KEYCODE2(SHIFT_LEFT, LEFTSHIFT);
		KEYCODE2(SHIFT_RIGHT, RIGHTSHIFT);
		KEYCODE1(TAB);
		KEYCODE1(SPACE);
		KEYCODE2(EXPLORER, WWW);
		KEYCODE2(ENVELOPE, MAIL);
		KEYCODE1(ENTER);
		KEYCODE2(DEL, BACKSPACE);
		KEYCODE1(GRAVE);
		KEYCODE1(MINUS);
		KEYCODE2(EQUALS, EQUAL);
		KEYCODE2(LEFT_BRACKET, LEFTBRACE);
		KEYCODE2(RIGHT_BRACKET, RIGHTBRACE);
		KEYCODE1(BACKSLASH);
		KEYCODE1(SEMICOLON);
		KEYCODE1(APOSTROPHE);
		KEYCODE1(SLASH);
		KEYCODE2shift(AT, 2);
		KEYCODE2shift(POUND, 3);
		KEYCODE2shift(STAR, 8);
		KEYCODE2shift(PLUS, EQUAL);
		KEYCODE1(MENU);
		KEYCODE1(SEARCH);
		KEYCODE2(MEDIA_PLAY_PAUSE, PLAYPAUSE);
		KEYCODE2(MEDIA_PLAY, PLAY);
		KEYCODE2(MEDIA_STOP, STOP_RECORD);
		KEYCODE2(MEDIA_NEXT, NEXTSONG);
		KEYCODE2(MEDIA_PREVIOUS, PREVIOUSSONG);
		KEYCODE2(MEDIA_REWIND, REWIND);
		KEYCODE2(MEDIA_FAST_FORWARD, FASTFORWARD);
		KEYCODE2(MEDIA_CLOSE, CLOSECD);
		KEYCODE2(MEDIA_EJECT, EJECTCD);
		KEYCODE2(MEDIA_RECORD, RECORD);
		KEYCODE2(MUTE, MICMUTE);
		KEYCODE2(PAGE_UP, PAGEUP);
		KEYCODE2(PAGE_DOWN, PAGEDOWN);
		KEYCODE2(ESCAPE, ESC);
		KEYCODE2(FORWARD_DEL, DELETE);
		KEYCODE2(CTRL_LEFT, LEFTCTRL);
		KEYCODE2(CTRL_RIGHT, RIGHTCTRL);
		KEYCODE2(CAPS_LOCK, CAPSLOCK);
		KEYCODE2(SCROLL_LOCK, SCROLLLOCK);
		KEYCODE2(NUM_LOCK, NUMLOCK);
		KEYCODE2(META_LEFT, LEFTMETA);
		KEYCODE2(META_RIGHT, RIGHTMETA);
		KEYCODE1(SYSRQ); // Print screen key
		KEYCODE1(BREAK); // Pause key
		KEYCODE2(MOVE_HOME, HOME);
		KEYCODE2(MOVE_END, END);
		KEYCODE1(INSERT);
		KEYCODE1(FORWARD);
		KEYCODE2(BACK, ESC);
		KEYCODE1(F1);
		KEYCODE1(F2);
		KEYCODE1(F3);
		KEYCODE1(F4);
		KEYCODE1(F5);
		KEYCODE1(F6);
		KEYCODE1(F7);
		KEYCODE1(F8);
		KEYCODE1(F9);
		KEYCODE1(F10);
		KEYCODE1(F11);
		KEYCODE1(F12);
		KEYCODE2(NUMPAD_0, KP0);
		KEYCODE2(NUMPAD_1, KP1);
		KEYCODE2(NUMPAD_2, KP2);
		KEYCODE2(NUMPAD_3, KP3);
		KEYCODE2(NUMPAD_4, KP4);
		KEYCODE2(NUMPAD_5, KP5);
		KEYCODE2(NUMPAD_6, KP6);
		KEYCODE2(NUMPAD_7, KP7);
		KEYCODE2(NUMPAD_8, KP8);
		KEYCODE2(NUMPAD_9, KP9);
		KEYCODE2(NUMPAD_DIVIDE, KPSLASH);
		KEYCODE2(NUMPAD_MULTIPLY, KPASTERISK);
		KEYCODE2(NUMPAD_SUBTRACT, KPMINUS);
		KEYCODE2(NUMPAD_ADD, KPPLUS);
		KEYCODE2(NUMPAD_DOT, KPDOT);
		KEYCODE2(NUMPAD_COMMA, KPCOMMA);
		KEYCODE2(NUMPAD_ENTER, KPENTER);
		KEYCODE2(NUMPAD_EQUALS, KPEQUAL);
		KEYCODE2(NUMPAD_LEFT_PAREN, KPLEFTPAREN);
		KEYCODE2(NUMPAD_RIGHT_PAREN, KPRIGHTPAREN);
		KEYCODE1(POWER);
		KEYCODE1(CAMERA);
		KEYCODE2(VOLUME_MUTE, MUTE);
		KEYCODE2(VOLUME_UP, VOLUMEUP);
		KEYCODE2(VOLUME_DOWN, VOLUMEDOWN);
		KEYCODE1(INFO);
		KEYCODE2(CHANNEL_UP, CHANNELUP);
		KEYCODE2(CHANNEL_DOWN, CHANNELDOWN);
		KEYCODE2(ZOOM_IN, ZOOMIN);
		KEYCODE2(ZOOM_OUT, ZOOMOUT);
		KEYCODE1(TV);
		KEYCODE2(BOOKMARK, BOOKMARKS);
		KEYCODE2(PROG_RED, RED);
		KEYCODE2(PROG_GREEN, GREEN);
		KEYCODE2(PROG_YELLOW, YELLOW);
		KEYCODE2(PROG_BLUE, BLUE);
		KEYCODE2(CONTACTS, ADDRESSBOOK);
		KEYCODE1(CALENDAR);
		KEYCODE2(MUSIC, PLAYER);
		KEYCODE2(CALCULATOR, CALC);
		KEYCODE2(BRIGHTNESS_DOWN, BRIGHTNESSDOWN);
		KEYCODE2(BRIGHTNESS_UP, BRIGHTNESSUP);
        default: *eventCode = KEY_RESERVED; return 0;
	}
	*eventCode = KEY_RESERVED;
	return 0;
}
#undef KEYCODE1
#undef KEYCODE2

#pragma clang diagnostic pop
