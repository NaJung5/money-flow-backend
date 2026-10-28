# money-flow-backend

개인 가계부 백엔드. 포트폴리오 및 학습 목적 프로젝트.
Java 25 / Spring Boot 4.1.1 / Gradle / Spring Data JPA / MariaDB / H2 (테스트) / JUnit5 / AssertJ / Mockito / Lombok

## 작업 방식 (가장 중요)

이 프로젝트는 학습이 목적이므로, 구현은 사용자가 직접 한다.

- 파일을 직접 수정하거나 생성하지 않는다. (읽기, 분석, 리뷰만 수행)
- 최초 구현 코드 (Service/Entity/Controller 등)를 먼저 제시하지 않는다.
- 먼저 책임 분리, 처리 순서, 검증 조건, 필요한 메서드, 힌트까지만 제시한다.
- 사용자가 작성한 코드를 기준으로 리뷰한다.
- 사용자가 "최종본 줘", "전체 코드 보여줘", "복사해서 쓸 수 있게 줘"라고 명시한 경우에만 완성 코드를 제공한다.
- 오류 발생 시 순서: 1) 원인 분석 2) 확인할 위치 3) 가능성 높은 원인 4) 최소 수정 방향. 정답 코드를 바로 주지 않는다.

## 리뷰 관점

- 책임이 적절한 위치 (Service/Entity/Repository)에 있는가
- Service가 과도하게 커지지 않았는가, Entity가 빈 데이터 객체가 되지 않았는가
- Repository 쿼리 메서드 이름이 적절한가
- 예외 처리가 일관적인가 (BusinessException + ErrorCode)
- 트랜잭션 범위가 적절한가
- 테스트해야 할 경계 조건이 빠지지 않았는가
- Soft Delete 조건 (deletedAt)을 빼먹은 조회가 없는가
- SOLID 관점 문제, 불필요한 복잡성
- "동작합니다" 수준의 리뷰는 하지 않는다.

## 구조 및 코드 스타일

- 도메인 단위 패키지: category, transaction, global.exception (각 도메인은 controller/dto/entity/repository/service)
- Controller: 요청 수신 → Service 호출 → 응답 반환만 담당
- Service: 업무 흐름과 Repository 조합
- Entity: 자기 상태 변경과 도메인 규칙 관리 (Service가 대상 조회 후 Entity 메서드 호출)
- DTO와 Entity 분리, 단순 DTO는 record 우선
- 조회 Service 메서드는 `@Transactional(readOnly = true)`, 변경은 `@Transactional`
  (`org.springframework.transaction.annotation.Transactional` 사용)

## 도메인 규칙

- 예외: `BusinessException` + `ErrorCode` (global.exception). 개별 Exception 클래스를 늘리지 않는다.
- 삭제: Soft Delete (`deletedAt`). 물리 삭제 지양.
- Money 값 객체: amount > 0
- Transaction: INCOME 거래는 INCOME 카테고리, EXPENSE 거래는 EXPENSE 카테고리만 사용 가능 (수정 시에도 검증)
- Category: 부모/자식 구조, 기본 생성은 CategoryType.CUSTOM
    - 중복 기준: 부모 없음 = name+type / 하위 = name+type+parentCategoryId

## 테스트

- Service 테스트: `@SpringBootTest` + `@Transactional`, given/when/then 구조
- Controller 테스트: MockMvc
- 전체 검증: `./gradlew clean test`

## Git

- 개인 feature 브랜치에서 원격과 차이가 있으면 fetch 후 rebase 우선 (공유 브랜치는 rebase 금지)
- 커밋 메시지: feat / fix / refactor / test / chore / style / docs 접두사 + 한글 설명
    - 이슈 작업이면 끝에 이슈 번호를 붙인다: `feat: 거래 수정 서비스 구현 (#13)`
- 커밋은 사용자가 직접 한다. Claude는 커밋 분리와 메시지만 제안한다. (그 자리에서 명시적으로 요청한 경우에만 직접 커밋)
- 커밋 메시지에 `Co-Authored-By` 등 Claude 관련 trailer를 넣지 않는다.

## 이슈와 PR

- 작업 단위: GitHub Issue 1개 = 브랜치 1개 = PR 1개. 큰 기능은 Epic 이슈 아래 sub-issue로 나눈다.
- 브랜치: 최신 main에서 `<접두사>/<이슈번호>-<영문-설명>` (예: `feat/13-transaction-update-service`). `#`은 쓰지 않는다.
- PR 본문에 `closes #<이슈번호>`를 넣어 머지 시 이슈가 닫히게 한다. 머지 방식은 Merge commit.
- 작업 보드: GitHub Projects `money-flow` (Todo / In Progress / Done)
- PR, 이슈, 코멘트에 `Generated with Claude Code` 같은 홍보성 꼬리말을 넣지 않는다.
- Claude가 PR에 남기는 리뷰는 셀프 리뷰로 오해되지 않도록 출처를 표기한다. (본문 첫 줄 `> AI 코드 리뷰 (Claude)`, 라인 코멘트 앞 `[AI 리뷰]`)
- GitHub 작업은 `gh` CLI로 한다. PATH에 없으면 `"C:\Program Files\GitHub CLI\gh.exe"`

## 프로젝트 스킬

- `/review`: 커밋 전 변경사항 리뷰 (체크 항목의 기준)
- `/test-cases`: 테스트 케이스 목록 추출
- `/commit-plan`: 커밋 분리와 메시지 제안
- `/pr-review`: PR 리뷰 후 GitHub에 게시
