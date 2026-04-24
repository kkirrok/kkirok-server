package com.kkirok.server.domain.kkinipop.application.service;

import com.kkirok.server.domain.kkinipop.dao.KkinipopMissionRepository;
import com.kkirok.server.domain.kkinipop.domain.KkinipopMission;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 끼니팝 미션 기본 저장 구현.
 *
 * <p>현재는 JPA repository의 saveAll을 사용한다.
 * 나중에 대량 저장 최적화가 필요해지면 이 구현만 bulk insert 방식으로 교체하면 된다.
 */
@Service
@RequiredArgsConstructor
public class KkinipopMissionJpaInsertService implements KkinipopMissionInsertService {

    private final KkinipopMissionRepository missionRepository;

    @Override
    public void insertMissions(List<KkinipopMission> missions) {
        missionRepository.saveAll(missions);
    }
}
