<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="ko">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>DPL 법규정보 관리시스템 — 롯데중앙연구소</title>
<style>
  *{box-sizing:border-box;margin:0;padding:0}
  body{font-family:'Malgun Gothic',sans-serif;background:#f0f2f5;color:#222;min-height:100vh}

  /* 헤더 */
  .header{background:#1a3d8a;color:#fff;padding:18px 32px;display:flex;align-items:center;gap:16px;box-shadow:0 2px 8px rgba(0,0,0,.25)}
  .header h1{font-size:20px;font-weight:bold;letter-spacing:-0.3px}
  .header .sub{font-size:13px;opacity:.75;margin-top:3px}
  .header-right{margin-left:auto;font-size:12px;opacity:.7}

  /* 계층 배너 */
  .hierarchy{background:#e8eef8;border-bottom:1px solid #c5d0e8;padding:10px 32px;font-size:12px;color:#555;display:flex;gap:8px;align-items:center;flex-wrap:wrap}
  .hierarchy span{color:#888}
  .hierarchy b{color:#1a3d8a}
  .hier-arrow{color:#bbb}

  /* 메인 */
  .main{max-width:1100px;margin:32px auto;padding:0 20px}

  /* 섹션 타이틀 */
  .section-title{font-size:15px;font-weight:bold;color:#333;margin-bottom:16px;padding-bottom:8px;border-bottom:2px solid #1a3d8a;display:flex;align-items:center;gap:8px}
  .section-title .badge{background:#1a3d8a;color:#fff;font-size:11px;padding:2px 8px;border-radius:20px;font-weight:normal}

  /* 카드 그리드 */
  .card-grid{display:grid;grid-template-columns:repeat(3,1fr);gap:20px;margin-bottom:40px}
  @media(max-width:720px){.card-grid{grid-template-columns:1fr 1fr}}

  .card{background:#fff;border-radius:10px;box-shadow:0 1px 4px rgba(0,0,0,.08);padding:24px 20px;text-decoration:none;color:#333;transition:all .18s;border:2px solid transparent;position:relative;overflow:hidden}
  .card:hover{border-color:#1a3d8a;box-shadow:0 4px 16px rgba(26,61,138,.15);transform:translateY(-2px)}
  .card-icon{font-size:28px;margin-bottom:12px}
  .card-name{font-size:15px;font-weight:bold;margin-bottom:6px}
  .card-desc{font-size:12px;color:#888;line-height:1.5;margin-bottom:14px}
  .card-count{font-size:28px;font-weight:bold;color:#1a3d8a}
  .card-count-label{font-size:11px;color:#aaa;margin-top:2px}
  .card-arrow{position:absolute;right:16px;bottom:16px;font-size:18px;color:#ccc}
  .card:hover .card-arrow{color:#1a3d8a}

  /* 계층 설명 */
  .flow-box{background:#fff;border-radius:10px;box-shadow:0 1px 4px rgba(0,0,0,.08);padding:20px 24px;margin-bottom:40px}
  .flow{display:flex;align-items:center;gap:0;flex-wrap:wrap}
  .flow-item{text-align:center;padding:10px 18px}
  .flow-item .f-name{font-size:13px;font-weight:bold;color:#1a3d8a}
  .flow-item .f-sub{font-size:11px;color:#aaa;margin-top:3px}
  .flow-sep{font-size:22px;color:#ccd;padding:0 4px}

  /* 로딩 스피너 */
  .count-loading{display:inline-block;width:24px;height:24px;border:3px solid #e0e7f5;border-top-color:#1a3d8a;border-radius:50%;animation:spin .7s linear infinite;vertical-align:middle}
  @keyframes spin{to{transform:rotate(360deg)}}

  .footer{text-align:center;padding:24px;font-size:12px;color:#bbb;margin-top:16px}
</style>
</head>
<body>

<div class="header">
  <div>
    <div class="header h1" style="font-size:20px;font-weight:bold">🏭 디지털 제품안전 라이브러리</div>
    <div class="sub">롯데중앙연구소 — 법규정보 통합 관리시스템</div>
  </div>
  <div class="header-right">DPL JSP 마이그레이션 데모 v0.6</div>
</div>

<%-- ASP 원본 계층 구조 --%>
<div class="hierarchy">
  <b>규제법률</b>
  <span class="hier-arrow">▶</span>
  <b>규제사항</b>
  <span class="hier-arrow">▶</span>
  <b>고시(부속서)</b>
  <span class="hier-arrow">▶</span>
  <b>안전요건</b>
  <span class="hier-arrow">▶</span>
  <b>품목정보</b>
  <span class="hier-arrow">▶</span>
  <b>세부품목정보</b>
  <span style="color:#aaa;margin-left:12px">← 각 단계가 상위 항목에 종속됩니다</span>
</div>

<div class="main">

  <div class="section-title">
    📋 모듈 현황
    <span class="badge">6개 모듈 라이브</span>
  </div>

  <div class="card-grid">
    <a class="card" href="legal/?mode=list">
      <div class="card-icon">⚖️</div>
      <div class="card-name">규제법률</div>
      <div class="card-desc">산업안전보건법, 화학물질관리법 등<br>최상위 법률 분류</div>
      <div class="card-count" id="cnt-legal"><span class="count-loading"></span></div>
      <div class="card-count-label">등록 건수</div>
      <div class="card-arrow">›</div>
    </a>
    <a class="card" href="regulation/?mode=list">
      <div class="card-icon">📜</div>
      <div class="card-name">규제사항</div>
      <div class="card-desc">규제법률별 세부 관리제도<br>(안전보건관리체계 구축 등)</div>
      <div class="card-count" id="cnt-regulation"><span class="count-loading"></span></div>
      <div class="card-count-label">등록 건수</div>
      <div class="card-arrow">›</div>
    </a>
    <a class="card" href="notify/?mode=list">
      <div class="card-icon">📢</div>
      <div class="card-name">고시(부속서)</div>
      <div class="card-desc">정부 고시 및 부속서<br>규제사항에 종속</div>
      <div class="card-count" id="cnt-notify"><span class="count-loading"></span></div>
      <div class="card-count-label">등록 건수</div>
      <div class="card-arrow">›</div>
    </a>
    <a class="card" href="safety/?mode=list">
      <div class="card-icon">🛡️</div>
      <div class="card-name">안전요건</div>
      <div class="card-desc">고시별 세부 안전 요건<br>상세 내용 포함</div>
      <div class="card-count" id="cnt-safety"><span class="count-loading"></span></div>
      <div class="card-count-label">등록 건수</div>
      <div class="card-arrow">›</div>
    </a>
    <a class="card" href="items_def/?mode=list">
      <div class="card-icon">📦</div>
      <div class="card-name">품목정보</div>
      <div class="card-desc">법정 품목명 · 적용범위<br>제외대상 관리</div>
      <div class="card-count" id="cnt-items_def"><span class="count-loading"></span></div>
      <div class="card-count-label">등록 건수</div>
      <div class="card-arrow">›</div>
    </a>
    <a class="card" href="items_detail/?mode=list">
      <div class="card-icon">🔍</div>
      <div class="card-name">세부품목정보</div>
      <div class="card-desc">일반 품목명 · 사용연령<br>재질(형태) 관리</div>
      <div class="card-count" id="cnt-items_detail"><span class="count-loading"></span></div>
      <div class="card-count-label">등록 건수</div>
      <div class="card-arrow">›</div>
    </a>
  </div>

  <div class="section-title">🔗 데이터 계층 구조</div>
  <div class="flow-box">
    <div class="flow">
      <div class="flow-item"><div class="f-name">⚖️ 규제법률</div><div class="f-sub">L1 최상위</div></div>
      <div class="flow-sep">›</div>
      <div class="flow-item"><div class="f-name">📜 규제사항</div><div class="f-sub">L2 법률별 제도</div></div>
      <div class="flow-sep">›</div>
      <div class="flow-item"><div class="f-name">📢 고시(부속서)</div><div class="f-sub">L3 정부 고시</div></div>
      <div class="flow-sep">›</div>
      <div class="flow-item"><div class="f-name">🛡️ 안전요건</div><div class="f-sub">L4 세부 요건</div></div>
      <div class="flow-sep">›</div>
      <div class="flow-item"><div class="f-name">📦 품목정보</div><div class="f-sub">L5 법정 품목</div></div>
      <div class="flow-sep">›</div>
      <div class="flow-item"><div class="f-name">🔍 세부품목</div><div class="f-sub">L6 일반 품목</div></div>
    </div>
  </div>

</div>

<div class="footer">롯데중앙연구소 DPL · ASP → JSP 마이그레이션 데모 · ㈜비즈스프링 × ㈜엠피알디</div>

<script>
// AJAX로 각 모듈 건수 로드
fetch('/ajax/?type=counts')
  .then(r => r.json())
  .then(data => {
    const map = {
      'cnt-legal':        data.legal,
      'cnt-regulation':   data.regulation,
      'cnt-notify':       data.notify,
      'cnt-safety':       data.safety,
      'cnt-items_def':    data.items_def,
      'cnt-items_detail': data.items_detail
    };
    Object.entries(map).forEach(([id, cnt]) => {
      const el = document.getElementById(id);
      if (el) el.innerHTML = (cnt || 0).toLocaleString();
    });
  })
  .catch(() => {
    document.querySelectorAll('.card-count').forEach(el => {
      el.innerHTML = '-';
    });
  });
</script>
</body>
</html>
