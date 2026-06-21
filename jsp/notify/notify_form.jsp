<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>고시(부속서) ${action eq 'ADD' ? '등록' : '수정'}</title>
<style>
  body { font-family: 'Malgun Gothic', sans-serif; font-size: 13px; }
  .wrap { max-width: 960px; margin: 20px auto; padding: 0 15px; }
  h2 { font-size: 18px; margin-bottom: 15px; }
  .tbl-form { width: 100%; border-collapse: collapse; margin-bottom: 15px; }
  .tbl-form th { background: #f0f4ff; width: 18%; padding: 10px;
                  border: 1px solid #ddd; text-align: left; font-weight: bold; vertical-align: top; }
  .tbl-form td { padding: 8px 12px; border: 1px solid #ddd; }
  .tbl-form input[type=text], .tbl-form select, .tbl-form textarea {
    padding: 6px 8px; border: 1px solid #ccc; border-radius: 3px;
    font-size: 13px; font-family: inherit; box-sizing: border-box; }
  .tbl-form input[type=text] { width: 98%; }
  .tbl-form textarea { width: 98%; height: 120px; resize: vertical; }
  .req { color: #e53935; }
  .radio-group label { margin-right: 15px; cursor: pointer; }
  .btn-wrap { text-align: center; margin-top: 10px; }
  .btn { padding: 7px 20px; border: none; border-radius: 3px; cursor: pointer; font-size: 13px; margin: 0 4px; }
  .btn-primary { background: #3d6fd4; color: #fff; }
  .btn-danger  { background: #c62828; color: #fff; }
  .btn-default { background: #777; color: #fff; }
  /* 첨부파일 그리드 (ASP 원본: ul._files li float:left width:50%) */
  .files-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 6px; }
  .files-grid li { list-style: none; display: flex; align-items: center; gap: 6px; }
  .files-grid li label { width: 20px; text-align: right; color: #555; flex-shrink: 0; }
</style>
</head>
<body>
<div class="wrap">
  <h2>고시(부속서) ${action eq 'ADD' ? '등록' : '수정'}</h2>

  <%-- ASP 원본: form action=notify_proc.asp enctype=multipart/form-data --%>
  <form id="frmInfo" method="post" action="?mode=proc" enctype="multipart/form-data" onsubmit="return jfSubmit()">
    <input type="hidden" name="mode"     value="proc">
    <input type="hidden" name="action"   id="action-field" value="${action}">
    <input type="hidden" name="ln_idx"   value="${lnIdx}">
    <input type="hidden" name="page"     value="${page}">
    <input type="hidden" name="qKey"     value="${fn:escapeXml(qKey)}">
    <input type="hidden" name="qWord"    value="${fn:escapeXml(qWord)}">
    <input type="hidden" name="qLL"      value="${qLL}">
    <input type="hidden" name="qLR"      value="${qLR}">

    <table class="tbl-form">
      <%-- ASP 원본: "정부고시(부속서)" (= ln_notify) --%>
      <tr>
        <th><i class="req">*</i> 정부고시(부속서)</th>
        <td>
          <input type="text" id="ln_notify" name="ln_notify"
                 value="${fn:escapeXml(info.ln_notify != null ? info.ln_notify : (info.ln_title != null ? info.ln_title : ''))}"
                 placeholder="정부고시명 입력" maxlength="100">
        </td>
      </tr>
      <%-- ASP 원본: 규제법률/사항 — 2단계 연동 드롭다운 --%>
      <tr>
        <th><i class="req">*</i> 규제법률/사항</th>
        <td>
          <span>
            <select id="ll_idx" name="ll_idx" onchange="loadRegulation(this.value)">
              <option value="0">-- 선택 --</option>
              <c:forEach var="ll" items="${legalList}">
                <option value="${ll.ll_idx}"
                  <c:if test="${ll.ll_idx == info.ll_idx}">selected</c:if>>
                  ${fn:escapeXml(ll.ll_title)}
                </option>
              </c:forEach>
            </select>
          </span>
          <span>
            <select id="lr_idx" name="lr_idx">
              <option value="0">-- 선택 --</option>
              <c:forEach var="lr" items="${regulationList}">
                <option value="${lr.lr_idx}"
                  <c:if test="${lr.lr_idx == info.lr_idx}">selected</c:if>>
                  ${fn:escapeXml(lr.lr_title)}
                </option>
              </c:forEach>
            </select>
          </span>
        </td>
      </tr>
      <%-- ASP 원본: "제·개정이력" (= ln_history) CKEditor --%>
      <tr>
        <th>제·개정이력</th>
        <td>
          <textarea name="ln_history" id="ln_history">${fn:escapeXml(info.ln_history != null ? info.ln_history : '')}</textarea>
        </td>
      </tr>
      <%-- ASP 원본: 부속서파일 1~10 (데모: file input만, 실제 업로드 처리는 생략) --%>
      <tr>
        <th>부속서파일</th>
        <td>
          <ul class="files-grid">
            <c:forEach begin="1" end="10" var="i">
              <li>
                <label>${i}.</label>
                <input type="file" name="lna_atta_${i}" id="lna_atta_${i}" style="font-size:12px">
              </li>
            </c:forEach>
          </ul>
        </td>
      </tr>
      <%-- ASP 원본: 사용여부 radio Y/N --%>
      <tr>
        <th><i class="req">*</i> 사용여부</th>
        <td class="radio-group">
          <label>
            <input type="radio" name="ln_is_use" value="Y"
              <c:if test="${empty info.ln_is_use || info.ln_is_use eq 'Y'}">checked</c:if>> 사용
          </label>
          <label>
            <input type="radio" name="ln_is_use" value="N"
              <c:if test="${info.ln_is_use eq 'N'}">checked</c:if>> 미사용
          </label>
        </td>
      </tr>
      <c:if test="${action eq 'MOD'}">
        <tr>
          <th>등록일</th>
          <td>${info.ln_reg_date} (${fn:escapeXml(info.ln_reg_user != null ? info.ln_reg_user : '')})</td>
        </tr>
        <tr>
          <th>수정일</th>
          <td>${info.ln_upd_date} (${fn:escapeXml(info.ln_upd_user != null ? info.ln_upd_user : '')})</td>
        </tr>
      </c:if>
    </table>

    <div class="btn-wrap">
      <button type="submit" class="btn btn-primary">저장</button>
      <c:if test="${action eq 'MOD'}">
        <button type="button" class="btn btn-danger"
          onclick="if(confirm('삭제하시겠습니까?')){
            document.getElementById('action-field').value='DEL';
            document.getElementById('frmInfo').submit();
          }">삭제</button>
      </c:if>
      <button type="button" class="btn btn-default"
        onclick="location.href='?mode=list&page=${page}&qKey=${fn:escapeXml(qKey)}&qWord=${fn:escapeXml(qWord)}&qLL=${qLL}&qLR=${qLR}'">목록</button>
    </div>
  </form>
</div>
<script>
/* ASP 원본: 규제법률 변경 → 규제사항 드롭다운 동적 갱신 (regulation_search.json.asp 패턴) */
function loadRegulation(llIdx) {
  var $lr = document.getElementById('lr_idx');
  while ($lr.options.length > 1) $lr.remove(1);
  if (!llIdx || llIdx === '0') return;
  fetch('/ajax/?type=regulation&qLL=' + llIdx)
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

/* ASP 원본: jfSubmit() 3가지 유효성 검증 */
function jfSubmit() {
  var notify = document.getElementById('ln_notify').value.trim();
  if (!notify) {
    alert('정부고시를 입력해주세요.');
    document.getElementById('ln_notify').focus();
    return false;
  }
  if (document.getElementById('ll_idx').value === '0') {
    alert('규제법률를 선택해주세요.');
    document.getElementById('ll_idx').focus();
    return false;
  }
  if (document.getElementById('lr_idx').value === '0') {
    alert('규제사항을 선택해주세요.');
    document.getElementById('lr_idx').focus();
    return false;
  }
  if (!document.querySelector('input[name="ln_is_use"]:checked')) {
    alert('사용여부를 선택해주세요.');
    return false;
  }
  return true;
}
</script>
</body>
</html>
