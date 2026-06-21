<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html><html><head><meta charset="UTF-8"><title>안전요건 목록</title>
<style>
  body{font-family:'Malgun Gothic',sans-serif;font-size:13px}
  .wrap{max-width:1200px;margin:20px auto;padding:0 15px}
  h2{font-size:18px;margin-bottom:15px}
  .search-bar{background:#f5f5f5;padding:10px 15px;border-radius:4px;display:flex;gap:8px;align-items:center;margin-bottom:15px;flex-wrap:wrap}
  .search-bar select,.search-bar input[type=text]{padding:5px 8px;border:1px solid #ccc;border-radius:3px;font-size:13px}
  .btn{padding:5px 14px;border:none;border-radius:3px;cursor:pointer;font-size:13px}
  .btn-primary{background:#3d6fd4;color:#fff}.btn-default{background:#aaa;color:#fff}.btn-success{background:#2e7d32;color:#fff}
  .tbl{width:100%;border-collapse:collapse;margin-bottom:10px}
  .tbl th{background:#3d6fd4;color:#fff;padding:8px;font-weight:normal;border:1px solid #2a56b0;text-align:center}
  .tbl td{padding:7px 10px;border:1px solid #ddd;vertical-align:middle}
  .tbl tr:hover td{background:#f0f4ff}.tbl td.center{text-align:center}
  .tbl td a{color:#1a3d8a;text-decoration:none}.tbl td a:hover{text-decoration:underline}
  .paging{text-align:center;margin-top:10px}
  .paging a{display:inline-block;padding:4px 10px;margin:0 2px;border:1px solid #ccc;border-radius:3px;text-decoration:none;color:#333}
  .paging a.cur{background:#3d6fd4;color:#fff;border-color:#3d6fd4}
  .total-bar{text-align:right;font-size:12px;color:#666;margin-bottom:4px}
</style>
</head><body>
<div class="wrap">
  <h2>안전요건 관리</h2>
  <form id="frmSearch" method="get" action="">
    <input type="hidden" name="mode" value="list">
    <input type="hidden" name="qSort" id="qSort" value="${fn:escapeXml(qSort)}">
    <div class="search-bar">
      <%-- ASP 원본: 3단계 드롭다운 qLL→qLR→qLN --%>
      <select name="qLL" id="qLL" onchange="loadLR(this.value)">
        <option value="0">-- 규제법률 전체 --</option>
        <c:forEach var="ll" items="${legalList}">
          <option value="${ll.ll_idx}" <c:if test="${ll.ll_idx==qLL}">selected</c:if>>${fn:escapeXml(ll.ll_title)}</option>
        </c:forEach>
      </select>
      <select name="qLR" id="qLR" onchange="loadLN(this.value)">
        <option value="0">-- 규제사항 --</option>
        <c:forEach var="lr" items="${regulationList}">
          <option value="${lr.lr_idx}" <c:if test="${lr.lr_idx==qLR}">selected</c:if>>${fn:escapeXml(lr.lr_title)}</option>
        </c:forEach>
      </select>
      <select name="qLN" id="qLN">
        <option value="0">-- 고시 --</option>
        <c:forEach var="ln" items="${notifyList}">
          <option value="${ln.ln_idx}" <c:if test="${ln.ln_idx==qLN}">selected</c:if>>${fn:escapeXml(ln.ln_title != null ? ln.ln_title : '')}</option>
        </c:forEach>
      </select>
      <select name="qKey">
        <option value="" <c:if test="${qKey==''}">selected</c:if>>전체</option>
        <option value="TITLE" <c:if test="${qKey=='TITLE'}">selected</c:if>>안전요건</option>
      </select>
      <input type="text" name="qWord" value="${fn:escapeXml(qWord)}" placeholder="검색어" style="width:180px">
      <button type="submit" class="btn btn-primary">검색</button>
      <button type="button" class="btn btn-default" onclick="jfReset()">초기화</button>
      <div style="flex:1"></div>
      <button type="button" class="btn btn-success" onclick="location.href='?mode=form'">신규등록</button>
    </div>
  </form>

  <div class="total-bar">전체 <strong>${total}</strong>건</div>
  <%-- ASP 원본 컬럼: No / 안전요건(LS_TITLE) / 규제법규(LL_TITLE) / 규제제도(LR_TITLE) / 정부고시(LN_NOTIFY) / 노출 / 등록일 --%>
  <table class="tbl">
    <thead>
      <tr>
        <th style="width:60px">No.</th>
        <th>안전요건</th>
        <th style="width:150px">규제법규</th>
        <th style="width:150px">규제제도</th>
        <th style="width:180px">정부고시(부속서)</th>
        <th style="width:60px">노출</th>
        <th style="width:110px">등록일</th>
      </tr>
    </thead>
    <tbody>
      <c:choose>
        <c:when test="${empty list}">
          <tr><td colspan="7" class="center" style="padding:30px;color:#999">검색된 내용이 없습니다.</td></tr>
        </c:when>
        <c:otherwise>
          <c:forEach var="row" items="${list}" varStatus="st">
            <tr>
              <td class="center">${total-(page-1)*listSize-st.index}</td>
              <td><a href="?mode=form&ls_idx=${row.ls_idx}&page=${page}&qKey=${fn:escapeXml(qKey)}&qWord=${fn:escapeXml(qWord)}&qLL=${qLL}&qLR=${qLR}&qLN=${qLN}">${fn:escapeXml(row.ls_title != null ? row.ls_title : '')}</a></td>
              <td class="center">${fn:escapeXml(row.ll_title != null ? row.ll_title : '')}</td>
              <td class="center">${fn:escapeXml(row.lr_title != null ? row.lr_title : '')}</td>
              <td class="center">${fn:escapeXml(row.ln_title != null ? row.ln_title : (row.ln_notify != null ? row.ln_notify : ''))}</td>
              <td class="center">${row.ls_is_use eq 'Y' ? '사용' : '미사용'}</td>
              <td class="center">${row.ls_reg_date}</td>
            </tr>
          </c:forEach>
        </c:otherwise>
      </c:choose>
    </tbody>
  </table>
  <div class="paging">
    <c:if test="${page>1}">
      <a href="?mode=list&page=1&qKey=${fn:escapeXml(qKey)}&qWord=${fn:escapeXml(qWord)}&qLL=${qLL}&qLR=${qLR}&qLN=${qLN}">◀◀</a>
      <a href="?mode=list&page=${page-1}&qKey=${fn:escapeXml(qKey)}&qWord=${fn:escapeXml(qWord)}&qLL=${qLL}&qLR=${qLR}&qLN=${qLN}">◀</a>
    </c:if>
    <c:forEach begin="1" end="${pageCnt}" var="p">
      <c:if test="${p>=page-4 && p<=page+4}">
        <a href="?mode=list&page=${p}&qKey=${fn:escapeXml(qKey)}&qWord=${fn:escapeXml(qWord)}&qLL=${qLL}&qLR=${qLR}&qLN=${qLN}" class="${p==page?'cur':''}">${p}</a>
      </c:if>
    </c:forEach>
    <c:if test="${page<pageCnt}">
      <a href="?mode=list&page=${page+1}&qKey=${fn:escapeXml(qKey)}&qWord=${fn:escapeXml(qWord)}&qLL=${qLL}&qLR=${qLR}&qLN=${qLN}">▶</a>
      <a href="?mode=list&page=${pageCnt}&qKey=${fn:escapeXml(qKey)}&qWord=${fn:escapeXml(qWord)}&qLL=${qLL}&qLR=${qLR}&qLN=${qLN}">▶▶</a>
    </c:if>
  </div>
</div>
<script>
function loadLR(llIdx) {
  var $lr=document.getElementById('qLR'), $ln=document.getElementById('qLN');
  while($lr.options.length>1)$lr.remove(1);
  while($ln.options.length>1)$ln.remove(1);
  if(!llIdx||llIdx==='0')return;
  fetch('/ajax/?type=regulation&qLL='+llIdx).then(r=>r.json()).then(d=>{
    (d.rows||[]).forEach(row=>{var o=document.createElement('option');o.value=row.lr_idx;o.text=row.lr_title;$lr.add(o);});
  }).catch(()=>{});
}
function loadLN(lrIdx) {
  var $ln=document.getElementById('qLN');
  while($ln.options.length>1)$ln.remove(1);
  if(!lrIdx||lrIdx==='0')return;
  fetch('/ajax/?type=notify&qLR='+lrIdx).then(r=>r.json()).then(d=>{
    (d.rows||[]).forEach(row=>{var o=document.createElement('option');o.value=row.ln_idx;o.text=row.ln_title||row.ln_notify;$ln.add(o);});
  }).catch(()=>{});
}
function jfReset(){
  document.querySelectorAll('#frmSearch select').forEach(e=>e.selectedIndex=0);
  document.querySelectorAll('#frmSearch input[type=text]').forEach(e=>e.value='');
  document.getElementById('qSort').value='';
  document.getElementById('frmSearch').submit();
}
</script>
</body></html>
