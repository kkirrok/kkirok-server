# 🍚 Kkirok Back-End

## 프로젝트 소개
끼니 기록을 통한 건강 관리 지원 앱
사용자가 식사를 기록하면 AI가 식습관을 분석해 리포트를 제공하고, 캐릭터 성장·미션으로 건강한 식습관을 이어가도록 돕습니다.

#### 주요 기능
- **끼니(식사) 기록**: 식사 내용·사진을 기록하고 날짜별로 관리합니다.
- **AI 식사 분석 & 리포트**: 기록된 식사를 외부 AI 분석 서버로 분석해 식습관 리포트를 제공합니다.
- **캐릭터 성장 (게이미피케이션)**: 기록·미션 달성에 따라 캐릭터가 성장합니다.
- **끼니팝 미션**: 미션 수행 기반의 리워드를 제공합니다.
- **소셜 로그인**: 카카오·네이버 OAuth2 로그인 및 JWT 기반 인증을 지원합니다.
- **알림**: Firebase Cloud Messaging(FCM) 기반 푸시 알림을 제공합니다.
- **홈 · 캘린더**: 날짜별 식사 기록과 요약을 조회합니다.
- **관리자 기능**: 관리자용 인증·관리 API를 제공합니다.


## 기술 스택
- Language: Java 17
- Framework: Spring Boot, Spring Security, Spring Data JPA, Spring AOP
- Auth: OAuth2 (카카오 · 네이버), JWT
- Database: PostgreSQL, Redis
- Storage: Cloudflare R2
- Email: AWS SES
- Push: Firebase Cloud Messaging (FCM)
- External API: OpenFeign (AI 분석 서버 연동)
- API Docs: Swagger (springdoc-openapi)
- Logging: Log4j2
- Build Tool: Gradle
- Deploy: Docker, Docker Compose, Cloudflare

## 프로젝트 구조
#### 도메인형
- 각 도메인 패키지는 엔티티, DTO, 컨트롤러, 서비스, 리포지토리 등 하위 패키지를 포함
- Base package: `com.kkirok.server`

```
src/
└── main/
    └── java/com/kkirok/server
        ├── ServerApplication.java
        ├── global/            # auth(JWT·RBAC), common, external, firebase, swagger, webhook
        └── domain/
            ├── member/        # 회원 · 소셜 로그인 · 인증
            ├── user/          # 사용자 · 권한(Role)
            ├── meal/          # 끼니(식사) 기록
            ├── report/        # AI 식사 분석 리포트
            ├── character/     # 캐릭터 (게이미피케이션)
            ├── kkinipop/      # 끼니팝 미션 · 리워드
            ├── home/          # 홈 · 캘린더
            └── notification/  # 알림 (FCM)
```

#### Branch Strategy
- main: 배포 가능한 최종 코드만 관리합니다.
- dev: 개발 중인 기능을 통합하는 브랜치입니다.
- feat: 새로운 기능 개발 시 사용합니다. (예: `feat/meal`)
- fix: 버그 수정 시 사용합니다. (예: `fix/meal`)
- refactor: 코드 리팩토링 시 사용합니다. (예: `refactor/meal`)
- chore: 자잘한 수정이나 빌드를 할 때 사용합니다. (예: `chore/meal`)

#### Issue
- 구현해야 하는 기능, 문제점, 예상 작업 항목 등을 이슈로 등록합니다.
- 이슈 템플릿을 참고하며, 제목 앞에 [타입/#이슈 번호]을 붙입니다. (예: `[Feat/#1] 끼니 기록 기능 구현`)
- 해당하는 라벨을 추가합니다.

#### Pull Request (PR)
- PR 템플릿을 참고하여 작성합니다.
- 제목 앞에 [타입][작성자]를 붙이고, 관련 이슈가 있다면 연결합니다. (예: `[FEAT][name]: 끼니 기록 기능 구현`)
- 코드 리뷰를 거친 후 dev 브랜치로 머지합니다.

<table>
  <tr>
    <!-- 팀원 수만큼 td 추가/삭제 -->
    <td align="center">
      <a href="https://github.com/ggamnunq">
        <img width="170" src="https://avatars.githubusercontent.com/u/93406666?v=4" alt="김준용" />
      </a>
    </td>
    <td align="center">
      <a href="https://github.com/Yujin1219">
        <img width="170" src="https://avatars.githubusercontent.com/u/127809173?v=4" alt="팀원2" />
      </a>
    </td>
  </tr>
  <tr>
    <td align="center"><b>김준용</b></td>
    <td align="center"><b>김시아</b></td>
  </tr>
  <tr>
    <td align="center">유저·인증·끼니팝·알림</td>
    <td align="center">식사기록·분석·리포트</td>
  </tr>
</table>
</div>


