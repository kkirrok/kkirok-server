package com.kkirok.server.domain.kkinipop.domain;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class KkinipopMissionPolicy {

    public static final int DAILY_MISSION_COUNT = 5;
    public static final int DAILY_MISSION_CANDIDATE_COUNT = 20;
    public static final int MISSION_GENERATION_LEAD_MINUTES = 5;
    public static final int REALTIME_DURATION_MINUTES = 10;
    public static final int REALTIME_SLOT_INTERVAL_MINUTES = 30;
    public static final int MAX_GENERATION_ATTEMPTS = 5;
    public static final int FALLBACK_LOOKBACK_DAYS = 14;

}
