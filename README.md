# Money Flow Backend

개인 수입과 지출을 기록하고 카테고리별로 관리하기 위한 가계부 Backend 프로젝트입니다.

단순 CRUD 구현보다 도메인 객체가 자신의 비즈니스 규칙을 관리하고, Service 계층은 비즈니스 흐름을 조정하는 구조를 목표로 개발하고 있습니다.

현재 Category와 Transaction 도메인을 중심으로 구현하고 있습니다.

## Tech Stack

- Java 25
- Spring Boot 4.1.1
- Spring MVC
- Spring Data JPA
- Jakarta Validation
- MariaDB
- H2
- JUnit 5
- AssertJ
- Mockito
- MockMvc
- Gradle
- Lombok

## 주요 기능

### Category

- 루트 / 자식 카테고리 생성
- 최대 2 Depth 제한
- 부모 / 자식 거래 유형 일치 검증
- SYSTEM / CUSTOM 카테고리 구분
- 활성 / 비활성 상태 관리
- Soft Delete
- 동일 부모 기준 중복 카테고리 검증

### Transaction

- 수입 / 지출 거래 등록
- Money Value Object를 이용한 금액 관리
- 거래와 카테고리 거래 유형 일치 검증
- 비활성 / 삭제된 카테고리 사용 제한
- 거래 정보 수정
- Soft Delete

## 주요 도메인 규칙

### Category

Category는 자기참조 구조를 사용합니다.

```text
식비
├── 외식
└── 카페

교통
├── 버스
└── 지하철
```

카테고리는 최대 2단계까지만 생성할 수 있습니다.

```text
식비
└── 외식
    └── 한식  X
```

부모와 자식 카테고리는 동일한 거래 유형을 가져야 합니다.

카테고리 중복 여부는 부모 카테고리까지 포함하여 판단합니다.

```text
루트 카테고리
name + type + parent IS NULL

자식 카테고리
name + type + parentId
```

따라서 서로 다른 부모 아래의 동일한 이름은 허용할 수 있습니다.

```text
식비
└── 기타

교통
└── 기타
```

### Transaction

거래 유형은 다음과 같이 구분합니다.

```text
INCOME
EXPENSE
```

주요 규칙은 다음과 같습니다.

- 거래 금액은 0보다 커야 합니다.
- 거래에는 카테고리가 반드시 필요합니다.
- 거래 유형과 카테고리의 거래 유형은 일치해야 합니다.
- 비활성 카테고리는 신규 거래에 사용할 수 없습니다.
- 삭제된 카테고리는 사용할 수 없습니다.
- 메모는 최대 50자입니다.
- 거래 장소는 최대 50자입니다.

## API

### Category 등록

```http
POST /api/category
```

Request

```json
{
  "name": "식비",
  "type": "EXPENSE",
  "parentId": null
}
```

Response

```json
{
  "categoryId": 1
}
```

자식 카테고리 등록 시 부모 카테고리 ID를 전달합니다.

```json
{
  "name": "외식",
  "type": "EXPENSE",
  "parentId": 1
}
```

### Transaction 등록

```http
POST /api/transactions
```

Request

```json
{
  "type": "EXPENSE",
  "amount": 10000,
  "transactionDate": "2026-10-01",
  "categoryId": 1,
  "memo": "점심",
  "place": "식당"
}
```

Response

```json
{
  "transactionId": 1
}
```

## Package Structure

```text
com.njung.moneyflow
├── category
│   ├── controller
│   ├── dto
│   ├── entity
│   ├── repository
│   └── service
│
├── transaction
│   ├── controller
│   ├── dto
│   ├── entity
│   ├── repository
│   └── service
│
└── global
    └── exception
```

도메인 단위로 패키지를 분리하고 있습니다.

## Layer Responsibility

### Entity

자신의 상태와 도메인 규칙을 관리합니다.

예:

- Category 계층 및 상태 검증
- Transaction과 Category의 거래 유형 검증
- Money 금액 검증
- Soft Delete 상태 관리

### Service

Repository와 Entity를 조합하여 비즈니스 흐름을 처리합니다.

```text
Category 생성

부모 카테고리 조회
→ 중복 검증
→ Category 생성
→ 저장
```

```text
Transaction 생성

Category 조회
→ Money 생성
→ Transaction 생성
→ 저장
```

### Controller

HTTP Request / Response 처리를 담당하며 비즈니스 로직은 최소화합니다.

```text
Request
→ Validation
→ Service
→ Response
```

### Repository

Entity 저장 및 조회를 담당합니다.

## Test

계층별 책임에 따라 테스트를 분리하고 있습니다.

- Entity: 순수 단위 테스트
- Repository: `@DataJpaTest`
- Service: `@SpringBootTest`
- Controller: `@WebMvcTest`, `MockMvc`

테스트는 주로 Given-When-Then 구조로 작성합니다.

```java
@Test
void createTransaction() {
    // given
    ...

    // when
    ...

    // then
    ...
}
```

예외 테스트는 다음과 같이 작성합니다.

```java
// when & then
assertThatThrownBy(() -> service.create(request))
    .isInstanceOf(BusinessException.class);
```

## Run

### Requirements

- Java 25
- MariaDB

Repository Clone

```bash
git clone https://github.com/NaJung5/money-flow-backend.git
cd money-flow-backend
```

Windows

```bash
gradlew.bat bootRun
```

macOS / Linux

```bash
./gradlew bootRun
```

## Test 실행

Windows

```bash
gradlew.bat clean test
```

macOS / Linux

```bash
./gradlew clean test
```

## 현재 진행 상태

### Category

- [x] Entity 설계
- [x] 계층형 Category
- [x] 최대 2 Depth 제한
- [x] SYSTEM / CUSTOM 구분
- [x] 활성 / 비활성
- [x] Soft Delete
- [x] 부모 / 자식 거래 유형 검증
- [x] 중복 카테고리 검증
- [x] Category 생성 Service
- [x] Category 생성 API
- [x] Entity / Repository / Service / Controller 테스트
- [ ] Category 조회 API
- [ ] Category 수정 API
- [ ] Category 활성 / 비활성 API
- [ ] Category 삭제 API

### Transaction

- [x] Entity 설계
- [x] Money Value Object
- [x] Transaction / Category 거래 유형 검증
- [x] 비활성 / 삭제 Category 검증
- [x] Transaction 수정 도메인 로직
- [x] Soft Delete
- [x] Transaction 등록 Service
- [x] Transaction 등록 API
- [x] Entity / Service / Controller 테스트
- [ ] Transaction 월별 조회
- [ ] Transaction 수정 API
- [ ] Transaction 삭제 API
- [ ] 월별 수입 / 지출 집계
- [ ] 카테고리별 지출 통계

## Roadmap

MVP 이후 다음 기능을 검토할 예정입니다.

- 계좌 관리
- 예산 관리
- 반복 거래
- 사용자 / 로그인
- 저축 목표
- 통계 기능 확장

## Documentation

상세 도메인 설계 및 규칙은 `docs` 디렉토리에서 관리합니다.

```text
docs/
├── domain-design.md
└── domain-rules.md
```

- `domain-design.md`: 도메인 분석 및 설계 과정
- `domain-rules.md`: 현재 적용 중인 도메인 규칙
