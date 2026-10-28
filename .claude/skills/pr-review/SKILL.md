---
name: pr-review
description: GitHub PR을 /review 체크 항목 기준으로 리뷰하고, 사용자 확인 후 gh로 PR에 리뷰(라인 코멘트 포함)를 게시한다. 사용자가 "PR 리뷰해줘", "리뷰 남겨줘"라고 하거나 /pr-review를 입력할 때 사용. 인자로 PR 번호를 받는다.
argument-hint: "[PR 번호] (생략 시 현재 브랜치의 PR)"
disable-model-invocation: true
---

# PR 리뷰

대상: $ARGUMENTS (비어 있으면 `gh pr view --json number`로 현재 브랜치의 PR. 없으면 중단하고 알린다)

`/review`가 커밋 전 로컬 변경을 본다면, 이 스킬은 **PR 단위**로 보고 결과를 **GitHub에 남긴다.** 리뷰 기준은 `/review`와 같아야 하므로 체크 항목을 여기에 따로 두지 않는다.

## 절차
1. **정보 수집**
   - `gh pr view <번호> --json title,body,baseRefName,headRefName,headRefOid,files,closingIssuesReferences`
   - `gh pr diff <번호>`
   - 연결된 이슈(`closes #N`)가 있으면 `gh issue view N`으로 완료 조건 체크리스트를 읽는다.
   - diff만으로 판단이 어려우면 변경된 파일 전체와 호출하는 쪽 코드까지 읽는다.
2. **테스트**: 로컬의 커밋되지 않은 변경이 섞이지 않도록 PR head를 별도 worktree에 받아 실행한다.
   ```
   git fetch origin
   git worktree add --detach <scratchpad>/pr<번호> <headRefOid>
   cd <scratchpad>/pr<번호> && ./gradlew clean test
   ```
   - 정리할 때 `git worktree remove`가 경로 길이 제한으로 실패하면, PowerShell에서 `Remove-Item -LiteralPath "\\?\<절대경로>" -Recurse -Force -Confirm:$false` 후 `git worktree prune`.
3. **리뷰**: `.claude/skills/review/SKILL.md`의 **체크 항목**을 읽고 그대로 적용한다. 추가로 확인할 것:
   - 연결된 이슈의 완료 조건이 모두 충족되었는가
   - PR 제목/본문이 실제 변경 내용과 맞는가, 이슈와 관계없는 변경이 섞이지 않았는가
4. **초안 보고**: 터미널에 리뷰 초안(본문 + 라인 코멘트 목록)을 보여주고 **사용자 확인을 받은 뒤** 게시한다.
5. **게시**: 리뷰 JSON을 scratchpad에 쓰고
   `gh api -X POST repos/NaJung5/money-flow-backend/pulls/<번호>/reviews --input <파일>`
   - `commit_id`: headRefOid
   - `event`: `COMMENT` (본인 계정이라 자기 PR은 APPROVE / REQUEST_CHANGES 불가)
   - `comments[]`: `path`, `line`(새 파일 기준), `side: "RIGHT"`, `body`
   - 라인 코멘트는 diff hunk 안의 줄에만 달 수 있다. hunk 밖의 지적은 본문에 `파일:줄`로 적는다.

## 리뷰 본문 형식
```
> AI 코드 리뷰 (Claude)

## 리뷰 요약
테스트 결과, 연결 이슈 완료 조건 충족 여부

### 주요
### 일관성
### 사소 (선택)
### 이후 이슈에서 고려할 점 (이번 PR 범위 밖)
### 좋은 점
```
- 비어 있는 섹션은 생략한다.
- 라인 코멘트는 `[AI 리뷰]`로 시작한다.
- 지적마다 어떤 상황에서 문제가 되는지(입력 → 잘못된 결과), 확인할 위치, 수정 방향 힌트를 쓴다.

## 게시 후 보고
- 리뷰 링크, 테스트 결과, 주요 지적 요약
- 다음 단계: 이 PR에서 고칠지, 별도 이슈로 미룰지 묻는다. 미루면 이슈 등록을 제안한다.

## 하지 말 것
- 코드 파일을 수정하지 않는다. 정답 코드를 주지 않고 힌트까지만 준다. (`/review`와 같음)
- 사용자 확인 없이 게시하지 않는다.
- `Generated with Claude Code`, `Co-Authored-By` 같은 홍보성 꼬리말을 넣지 않는다.
