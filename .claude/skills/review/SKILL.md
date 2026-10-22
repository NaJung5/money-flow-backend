---
name: review
description: 작업 중인 변경사항(커밋 전 diff)을 CLAUDE.md 리뷰 관점으로 리뷰하고 테스트를 실행한다. 사용자가 "봐줘", "확인해줘", "리뷰해줘"라고 하거나 /review를 입력할 때 사용. 인자로 클래스나 파일 이름을 주면 그 범위만 본다.
disable-model-invocation: true
---

# 작업 중 변경사항 리뷰

리뷰 범위: $ARGUMENTS (비어 있으면 커밋되지 않은 변경 전체)

## 절차
1. `git status --short`와 `git diff`로 변경된 파일을 확인한다.
   - untracked 파일(`??`)은 diff에 나오지 않으니 직접 읽는다.
   - 변경된 부분만으로 판단이 어려우면 파일 전체와 호출하는 쪽 코드까지 읽는다.
2. `.\gradlew.bat clean test`를 실행해서 컴파일과 테스트 결과를 확인한다.
   - 실패하면 어떤 테스트가 왜 실패했는지, 의도된 실패(동작이 바뀌어서 기대값을 고쳐야 함)인지 버그인지 구분한다.
3. 아래 체크 항목으로 리뷰한다.
4. 아래 출력 형식으로 답한다.

## 체크 항목
**책임 위치**
- Controller는 요청 수신 → Service 호출 → 응답 반환만 하는가
- Service가 과도하게 커지지 않았는가. Entity가 할 상태 변경과 검증을 Service가 하고 있지 않은가
- Service와 Entity가 같은 검증을 중복하고 있지 않은가. 실행될 수 없는 검사(dead code)가 남아 있지 않은가
- DTO와 Entity가 분리돼 있는가

**조회와 Soft Delete**
- 삭제된 데이터를 조회하면 안 되는 곳에서 `findById`처럼 `deletedAt` 조건 없는 조회를 쓰고 있지 않은가
- 삭제된 대상은 조회 단계에서 걸러서 `*_NOT_FOUND`로 처리하는 기준과 맞는가
- Repository 쿼리 메서드 이름이 실제 조건과 맞는가. 단건 조회는 `Optional`을 반환하는가

**예외**
- `BusinessException` + `ErrorCode`만 쓰는가. `IllegalArgumentException` 같은 표준 예외나 개별 Exception 클래스를 새로 만들지 않았는가
- ErrorCode의 의미와 HTTP 상태가 상황에 맞는가 (입력값 400, 상태 충돌 409, 정책 금지 403, 없음 404)
- detail은 `key=value` 형식, `=` 뒤 공백 없음, 실제 값을 남기는가 (`categoryId=1`, `memo.length=51`)

**트랜잭션과 검증**
- 조회 메서드는 `@Transactional(readOnly = true)`, 변경은 `@Transactional` (`org.springframework.transaction.annotation`)
- `@RequestBody` 파라미터에 `@Valid`가 있는가
- 거래 타입과 카테고리 타입 일치 규칙이 생성과 수정 모두에서 검증되는가

**테스트**
- 경계 조건이 빠지지 않았는가 (50자/51자, 0/1, null, 삭제됨, 비활성, SYSTEM, 타입 불일치, 계층 깊이)
- 예외 테스트는 `hasMessage` 대신 `extracting("errorCode").isEqualTo(...)`로 검증하는가
- Controller 검증 실패 테스트에서 `verifyNoInteractions(service)`로 Service가 호출되지 않았음을 확인하는가
- Service 테스트는 `@SpringBootTest` + `@Transactional`, given/when/then 구조인가

**이름**
- 메서드, 변수, ErrorCode 이름이 실제 동작과 맞는가. 물어보지 않아도 어색한 이름은 먼저 짚는다
- `find~`는 없을 수 있음(Optional/null), `get~`은 없으면 예외라는 관례를 따르는가
- 오타, 띄어쓰기, 메시지 표기가 기존 코드와 일관적인가

## 출력 형식
1. **결론 한 줄**: 방향이 맞는지, 지금 바로 고칠 것이 있는지
2. **테스트 결과**: 통과/실패, 실패하면 원인
3. **문제점**: 심각한 순서대로. 각 항목에 `파일:줄`, 무엇이 문제인지, 어떤 상황에서 문제가 되는지, 수정 방향 힌트
4. **잘 된 것**: 짧게
5. **다음 단계**: 이어서 할 작업

## 하지 말 것
- 파일을 수정하거나 생성하지 않는다. 리뷰만 한다.
- 정답 코드를 먼저 제시하지 않는다. 힌트(필요한 메서드, 처리 순서, 확인할 위치)까지만 준다. 사용자가 "고쳐줘", "최종본 줘"라고 하면 그때 수정한다.
- "동작합니다" 수준의 리뷰는 하지 않는다.
