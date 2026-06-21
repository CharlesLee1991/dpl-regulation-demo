<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>고시(부속서) 목록</title>
<style>
  body { font-family: 'Malgun Gothic', sans-serif; font-size: 13px; }
  .wrap { max-width: 1200px; margin: 20px auto; padding: 0 15px; }
  h2 { font-size: 18px; margin-bottom: 15px; }
  .search-bar { background: #f5f5f5; padding: 10px 15px; border-radius: 4px;
                display: flex; gap: 8px; align-items: center; margin-bottom: 15px; flex-wrap: wrap; }
  .search-bar select, .search-bar input[type=text] {
    padding: 5px 8px; border: 1px solid #ccc; border-radius: 3px; font-size: 13px; }
  .btn { padding: 5px 14px; border: none; border-radius: 3px; cursor: pointer; font-size: 13px; }
  .btn-primary { background: #3d6fd4; color: #fff; }
  .btn-default { background: #aaa; color: #fff; }
  .btn-success { background: #2e7d32; color: #fff; }
  .tbl { width: 100%; border-collapse: collapse; margin-bottom: 10px; }
  .tbl th { background: #3d6fd4; color: #fff; padding: 8px; font-weight: normal;
             border: 1px solid #2a56b0; text-align: center; }
  .tbl td { padding: 7px 10px; border: 1px solid #ddd; vertical-align: middle; }
  .tbl tr:hover td { background: #f0f4ff; }
  .tbl td.center { text-align: center; }
  .tbl td a { color: #1a3d8a; text-decoration: none; }
  .tbl td a:hover { text-decoration: underline; }
  .paging { text-align: center; margin-top: 10px; }
  .paging a { display: inline-block; padding: 4px 10px; margin: 0 2px;
               border: 1px solid #ccc; border-radius: 3px; text-decoration: none; color: #333; }
  .paging a.cur { background: #3d6fd4; color: #fff; border-color: #3d6fd4; }
  .total-bar { text-align: right; font-size: 12px; color: #666; margin-bottom: 4px; }
</style>
</head>
<body>
<div class="wrap">
  <h2>고시(부속서) 관리</h2>

  <%-- ASP 원본: frmSearch — qLL(규제법률) / qLR(규제사항) / qKey·qWord 검색 --%>
  <form id="frmSearch" method="get" action="">
    <input type="hidden" name="mode" value="list">
    <input type="hidden" name="qSort" id="qSort" value="${fn:escapeXml(qSort)}">
    <div class="search-bar">
      <%-- 1단계: 규제법률 --%>
      <select name="qLL" id="qLL" onchange="loadRegulation(this.value)">
        <option value="0">-- 규제법률 전체 --</option>
        <c:forEach var="ll" items="${legalList}">
          <option value="${ll.ll_idx}"
            <c:if test="${ll.ll_idx == qLL}">selected</c:if>>
            ${fn:escapeXml(ll.ll_title)}
          </option>
        </c:forEach>
      </select>
      <%-- 2단계: 규제사항 (qLL 선택 시 동적 로드 — 수정 시 서버사이드 pre-fill) --%>
      <select name="qLR" id="qLR">
        <option value="0">-- 규제사항 전체 --</option>
        <c:forEach var="lr" items="${regulationList}">
          <option value="${lr.lr_idx}"
            <c:if test="${lr.lr_idx == qLR}">selected</c:if>>
            ${fn:escapeXml(lr.lr_title)}
          </option>
        </c:forEach>
      </select>
      <%-- 검색어: ASP 원본 qKey=TITLE → 정부고시 --%>
      <select name="qKey">
        <option value="" <c:if test="${qKey==''}">selected</c:if>>전체</option>
        <option value="TITLE" <c:if test="${qKey=='TITLE'}">selected</c:if>>정부고시</option>
      </select>
      <input type="text" name="qWord" value="${fn:escapeXml(qWord)}" placeholder="검색어" style="width:200px">
      <button type="submit" class="btn btn-primary">검색</button>
      <button type="button" class="btn btn-default" onclick="jfSearchReset()">초기화</button>
      <div style="flex:1"></div>
      <button type="button" class="btn btn-success"
        onclick="location.href='?mode=form'">신규등록</button>
    </div>
  </form>

  <div class="total-bar">전체 <strong>${total}</strong>건</div>
  <%-- 컬럼: No / 정부고시(부속서)(LN_TITLE) / 규제법규(LL_TITLE) / 규제제도(LR_TITLE) / 노출 / 등록일 --%>
  <table class="tbl">
    <thead>
      <tr>
        <th style="width:60px">No.</th>
        <th>정부고시(부속서)</th>
        <th style="width:160px">규제법규</th>
        <th style="width:160px">규제제도</th>
        <th style="width:60px">노출</th>
        <th style="width:110px">등록일</th>
      </tr>
    </thead>
    <tbody>
      <c:choose>
        <c:when test="${empty list}">
          <tr><td colspan="6" class="center" style="padding:30px;color:#999">검색된 내용이 없습니다.</td></tr>
        </c:when>
        <c:otherwise>
          <c:forEach var="row" items="${list}" varStatus="st">
            <tr>
              <td class="center">${total - (page-1)*listSize - st.index}</td>
              <td>
                <a href="?mode=form&ln_idx=${row.ln_idx}&page=${page}&qKey=${fn:escapeXml(qKey)}&qWord=${fn:escapeXml(qWord)}&qLL=${qLL}&qLR=${qLR}">
                  ${fn:escapeXml(row.ln_title != null ? row.ln_title : (row.ln_notify != null ? row.ln_notify : ''))}
                </a>
              </td>
              <td class="center">${fn:escapeXml(row.ll_title != null ? row.ll_title : '')}</td>
              <td class="center">${fn:escapeXml(row.lr_title != null ? row.lr_title : '')}</td>
              <td class="center">${row.ln_is_use eq 'Y' ? '사용' : '미사용'}</td>
              <td class="center">${row.ln_reg_date}</td>
            </tr>
          </c:forEach>
        </c:otherwise>
      </c:choose>
    </tbody>
  </table>

  <%-- 페이징 --%>
  <div class="paging">
    <c:if test="${page > 1}">
      <a href="?mode=list&page=1&qKey=${fn:escapeXml(qKey)}&qWord=${fn:escapeXml(qWord)}&qLL=${qLL}&qLR=${qLR}">◀◀</a>
      <a href="?mode=list&page=${page-1}&qKey=${fn:escapeXml(qKey)}&qWord=${fn:escapeXml(qWord)}&qLL=${qLL}&qLR=${qLR}">◀</a>
    </c:if>
    <c:forEach begin="1" end="${pageCnt}" var="p">
      <c:if test="${p >= page-4 && p <= page+4}">
        <a href="?mode=list&page=${p}&qKey=${fn:escapeXml(qKey)}&qWord=${fn:escapeXml(qWord)}&qLL=${qLL}&qLR=${qLR}"
           class="${p==page ? 'cur' : ''}">${p}</a>
      </c:if>
    </c:forEach>
    <c:if test="${page < pageCnt}">
      <a href="?mode=list&page=${page+1}&qKey=${fn:escapeXml(qKey)}&qWord=${fn:escapeXml(qWord)}&qLL=${qLL}&qLR=${qLR}">▶</a>
      <a href="?mode=list&page=${pageCnt}&qKey=${fn:escapeXml(qKey)}&qWord=${fn:escapeXml(qWord)}&qLL=${qLL}&qLR=${qLR}">▶▶</a>
    </c:if>
  </div>
</div>
<script>
/* ASP 원본: 규제법률 변경 → AJAX로 규제사항 목록 동적 로드 */
function loadRegulation(llIdx) {
  var $lr = document.getElementById('qLR');
  // 옵션 초기화
  while ($lr.options.length > 1) $lr.remove(1);
  if (!llIdx || llIdx === '0') return;
  // 서버에서 JSON 조회 (regulation_search.json.asp 대체 — 동일 servlet AJAX 엔드포인트)
  fetch('?mode=list&ajax=regulation&qLL=' + llIdx)
    .then(function(r){ return r.json(); })
    .then(function(data){
      if (data && data.rows) {
        data.rows.forEach(function(row){
          var opt = document.createElement('option');
          opt.value = row.lr_idx;
          opt.text  = row.lr_title;
          $lr.add(opt);
        });
      }
    }).catch(function(){});
}

function jfSearchReset() {
  document.querySelectorAll('#frmSearch input[type=text]').forEach(function(e){ e.value=''; });
  document.querySelectorAll('#frmSearch select').forEach(function(e){ e.selectedIndex=0; });
  document.getElementById('qSort').value = '';
  document.getElementById('frmSearch').submit();
}
</script>
</body>
</html>
