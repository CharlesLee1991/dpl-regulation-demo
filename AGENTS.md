# AGENTS.md — T-DPL (dpl-regulation-demo) 개발 표준 (SSOT)

> 이 문서는 본 레포의 **단일 진실 원천(SSOT)** 입니다.
> 사람이든 LLM 에이전트든, 코드를 수정하기 전에 반드시 이 문서를 먼저 읽으십시오.
> 최종 갱신: 2026-07-16 (v5.4 기준, HEAD `76fbb49`)

---

## 1. 프로젝트 개요

- **목적**: 롯데중앙연구소 규제법규 시스템(LSAFE)의 **ASP → JSP(Java Servlet) 마이그레이션** 데모
- **데모 URL**: https://regulation-production-f5b3.up.railway.app/front/
- **스택**: Java Servlet + JSP (Tomcat, `web-app 4.0`), MS SQL Server 2012, Railway 배포(Docker)
- **DB**: 실 LSAFE 서버 `121.78.147.103:16868`, DB `LSAFE`, user `lsafe`
  - 비밀번호는 **환경변수 `DB_PASS`** 로만 주입 (Railway env). **코드/설정에 평문 기록 절대 금지.**

## 2. 🔴 절대 규칙 (위반 금지)

1. **원본 무손상**: 앱은 원본 `LAW_*` / `ILIMS_*` 테이블에 **절대 쓰지 않는다** (SELECT only).
   모든 CRUD는 자체 미러 테이블 `dpl_*` 에만 수행한다.
2. **DDL 원본 컬럼명 준수**: 모든 `dpl_*` 테이블 컬럼명은 원본과 **완전 동일**해야 한다
   (접두 `dpl_` 테이블명만 다름). 자체 컬럼 추가 금지 → 실환경 이식이 "테이블명 매핑만"으로
   가능해야 하기 때문. (v5.3에서 이 규칙 위반이 미러 불가의 근본원인이었음)
3. **JDBC 접속 옵션 고정**: SQL Server 2012 특성상
   `encrypt=false;trustServerCertificate=true` 필수, TDS는 사실상 7.0 호환으로 동작.
   접속 문자열 임의 변경 금지.
4. **최소·수술적 변경**: 요청된 부분만 수정. 인접 리팩토링·선제 추상화 금지 (AP-26/27).
5. **시크릿**: 어떤 커밋에도 비밀번호·토큰 평문 금지. push 전 시크릿 스캔.

## 3. 구조

```
WEB-INF/web.xml            서블릿 매핑 + DB_URL/DB_USER (DB_PASS는 env)
WEB-INF/src/db/            DBPool, 부트 리스너(스키마 셋업·미러 적재)
WEB-INF/src/ctrl/          서블릿 13종
  FrontServlet             프론트 전 화면 라우팅 (실데이터 조회의 중심)
  AjaxServlet              db_test 등 진단 엔드포인트
  *AdminServlet, *Servlet  관리자 9모듈 CRUD (dpl_* 대상)
jsp/front/                 프론트 JSP 14종
jsp/{legal,regulation,...} 관리자 JSP
static/                    정적 자원 (js/css/img)
Dockerfile                 Railway 배포
```

## 4. 미러 시스템 (dpl_* ← LAW_*)

부트 시 리스너가 원본 → 미러 적재(mirror_reload). **12 테이블 / 9,626행** (v5.4 정본).

### 미러 4패턴 — 테이블 확장 시 상시 점검
1. **리네이밍**: 원본↔미러 컬럼명 상이 시 pairs extra 매핑 (예: `LN_TITLE ← LN_NOTIFY`)
2. **정규화 차이**: 원본에 없는 비정규화 FK는 체인 역산 backfill (LI→LN→LR→LL)
3. **누락 감지**: 완료조건 = `grep -oE 'dpl_[a-z_]+' FrontServlet.java` ↔ pairs 목록 완전 대조
4. **길이/자체컬럼**: 원본 text형 → `NVARCHAR(MAX)`. 스키마 DROP 가드는 **길이 기준**
   (컬럼명 기준은 중간버전을 못 걸러냄)

### 원본 이식 3함정
1. **유틸함수 미이식은 실데이터 투입 전엔 안 드러남** (예: `FN_ClearTag` 누락 → raw HTML 노출)
2. **`sql()` 헬퍼는 컬럼키를 `toLowerCase()` 저장** — 후처리 키도 소문자여야 JSP 반영됨
3. **실 DB 본문은 HTML 엔티티 이중 인코딩** — 디코드→태그제거를 **2회 반복** 필요

## 5. 검증 (수정 후 필수)

레포 외부 스크립트 2종이 완료 조건 (릴레이 키트에 포함, 요청 시 제공):
- `dpl_e2e_v54.sh` — 36항목 (연결/적재량/⭐메인카운트 1,194·2,177·224 원본일치/전화면/회귀가드/원본무손상)
- `dpl_browser_verify.py` — 24항목 (Playwright: JS에러·404·롤링카운터 실렌더·swiper 생존)
  - Railway 도메인은 인증서 오류 → `--ignore-certificate-errors` + `ignore_https_errors=True` 필수

**"일부 화면만 확인" 금지** — v5.1이 3화면만 보고 통과시킨 것이 버그 7건의 원인.
**전 화면(프론트 11 + 관리자 9) 검증이 완료 조건.**

## 6. LLM 에이전트 작업 절차

1. 이 문서 + `DEVLOG.md` 읽기
2. 변경 설계를 먼저 제시하고 승인받기 (설계 없이 실행 금지)
3. 수정은 요청 범위만, additive 우선
4. §5 검증 통과 확인 후 커밋 (author: `lcseung@bizspring.co.kr`)
5. 커밋 메시지: `fix:`/`feat:` + 버전 태그 관례 유지 (예: `fix: v5.4.5 - ...`)

## 7. 알려진 미결 (경미)

- 숏클래스 썸네일: 원본 서버 파일(`/law_new/_upload/...`) 참조 → 데모 표시 불가
  (404는 서버단 차단됨, 실환경 이식 시 자동 해소)
- `dpl_safety` DDL 자체컬럼 4개(LL_IDX·LR_IDX·LS_REG_IP·LS_UPD_IP) 잔존 + `LS_ICODE` 누락
  — 미러 교집합엔 무해, 실환경 이식 전 정리 필요 (§2-2 규칙 위반분)
- 로그인 미구현 (승인 대기 항목)
