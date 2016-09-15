LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := mainfn
LOCAL_SRC_FILES := mainfn.c my_flite_hts_imp.c us_int_accent_cart.c us_int_tone_cart.c us_nums_cart.c us_phrasing_cart.c us_pos_cart.c transliterator.c
LOCAL_LDLIBS +=  -lm -llog

include $(BUILD_SHARED_LIBRARY)
