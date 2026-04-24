package com.kkirok.server.domain.kkinipop.application.service;

import com.kkirok.server.domain.kkinipop.domain.KkinipopMission;
import java.util.List;

/**
 * 끼니팝 미션 저장 전략 인터페이스.
 *
 * <p>현재는 JPA saveAll 구현을 사용하지만, 이후 bulk insert 또는 다른 저장 전략으로
 * 쉽게 교체할 수 있도록 저장 책임을 분리한다.
 */
public interface KkinipopMissionInsertService {

    void insertMissions(List<KkinipopMission> missions);
}
