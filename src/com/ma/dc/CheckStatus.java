package com.ma.dc;

enum CheckStatus {
    ALARM(4), TIME_TO_CHECK(3), OUT_OF_ORDER(2), CHECK_OK(1);

    private final int value;

    private CheckStatus(int value) {
        this.value = value;
    }

    static CheckStatus getFromValue(int value) {
        switch (value) {
        case 1:
            return CHECK_OK;
        case 2:
            return OUT_OF_ORDER;
        case 3:
            return TIME_TO_CHECK;
        case 4:
            return ALARM;
        }

        // default value
        return TIME_TO_CHECK;
    }

    int getValue() {
        return value;
    }
}
