<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>규제법률 ${action eq 'ADD' ? '등록' : '수정'}</title>
<style>
  body { font-family: 'Malgun Gothic', sans-serif; font-size: 13px; }
  .wrap { max-width: 900px; margin: 20px auto; padding: 0 15px; }
  h2 { font-size: 18px; margin-bottom: 15px; }
  .tbl-form { width: 100%; border-collapse: collapse; margin-bottom: 15px; }
  .tbl-form th { background: #f0f4ff; width: 18%; padding: 10px;
                  border: 1px solid #ddd; text-align: left; font-weight: bold; vertical-align: middle; }
  .tbl-form td { padding: 8px 12px; border: 1px solid #ddd; }
  .tbl-form input[type=text] {
    width: 98%; padding: 6px 8px; border: 1px solid #ccc; border-radius: 3px;
    font-size: 13px; font-family: inherit; box-sizing: border-box; }
  .req { color: #e53935; }
  .radio-group label { margin-right: 15px; cursor: pointer; }
  .btn-wrap { text-align: center; margin-top: 10px; }
  .btn { padding: 7px 20px; border: none; border-radius: 3px; cursor: pointer; font-size: 13px; margin: 0 4px; }
  .btn-primary { background: #3d6fd4; color: #fff; }
  .btn-danger  { background: #c62828; color: #fff; }
  .btn-default { background: #777; color: #fff; }
  .nav-link { margin-bottom: 10px; }
  .nav-link a { color: #3d6fd4; font-size: 12px; text-decoration: none; }
  .nav-link a:hover { text-decoration: underline; }
</style>
</head>
<body>
<div class="wrap">
  <h2>규제법률 ${action eq 'ADD' ? '등록' : '수정'}</h2>
  <div class="nav-link">▶ <a href="${pageContext.request.contextPath}/legal/?mode=list&page=${page}&qKey=${fn:escapeXml(qKey)}&qWord=${fn:escapeXml(qWord)}">규제법률 목록</a></div>

  <%-- ASP 원본: action=legal_proc.asp, fields: action/ll_idx + 검색파라미터 --%>
  <form id="frmInfo" method="post" action="?mode=proc" onsubmit="return jfSubmit()">
    <input type="hidden" name="mode"    value="proc">
    <input type="hidden" name="action"  value="${action}">
    <input type="hidden" name="ll_idx"  value="${llIdx}">
    <input type="hidden" name="page"    value="${page}">
    <input type="hidden" name="qKey"    value="${fn:escapeXml(qKey)}">
    <input type="hidden" name="qWord"   value="${fn:escapeXml(qWord)}">

    <table class="tbl-form">
      <%-- ASP 원본 라벨: "법규명" (= LL_TITLE) --%>
      <tr>
        <th><i class="req">*</i> 법규명</th>
        <td>
          <input type="text" id="ll_title" name="ll_title"
                 value="${fn:escapeXml(info.ll_title)}" placeholder="법규명 입력" maxlength="200">
        </td>
      </tr>
      <%-- ASP 원본 라벨: "관리부처" (= LL_DEPT) --%>
      <tr>
        <th><i class="req">*</i> 관리부처</th>
        <td>
          <input type="text" id="ll_dept" name="ll_dept"
                 value="${fn:escapeXml(info.ll_dept)}" placeholder="관리부처 입력" maxlength="100">
        </td>
      </tr>
      <%-- ASP 원본: radio Y/N --%>
      <tr>
        <th><i class="req">*</i> 사용여부</th>
        <td class="radio-group">
          <label>
            <input type="radio" name="ll_is_use" value="Y"
              <c:if test="${empty info.ll_is_use || info.ll_is_use eq 'Y'}">checked</c:if>> 사용
          </label>
          <label>
            <input type="radio" name="ll_is_use" value="N"
              <c:if test="${info.ll_is_use eq 'N'}">checked</c:if>> 미사용
          </label>
        </td>
      </tr>
      <c:if test="${action eq 'MOD'}">
        <tr>
          <th>등록일</th>
          <td>${info.ll_reg_date} (${fn:escapeXml(info.ll_reg_user)})</td>
        </tr>
      </c:if>
    </table>

    <div class="btn-wrap">
      <%-- ASP 원본: 저장=jfSubmit()→legal_proc.asp / 삭제=DEL / 목록=jfList() --%>
      <button type="submit" class="btn btn-primary">저장</button>
      <c:if test="${action eq 'MOD'}">
        <button type="button" class="btn btn-danger"
          onclick="if(confirm('삭제하시겠습니까?')){
            document.getElementById('action-field').value='DEL';
            document.getElementById('frmInfo').submit();
          }">삭제</button>
      </c:if>
      <button type="button" class="btn btn-default"
        onclick="location.href='?mode=list&page=${page}&qKey=${fn:escapeXml(qKey)}&qWord=${fn:escapeXml(qWord)}'">목록</button>
    </div>
    <input type="hidden" id="action-field" name="action" value="${action}">
  </form>
</div>
<script>
/* ASP 원본: jfSubmit() — 법규명/관리부처/사용여부 3가지 필수 검증 */
function jfSubmit() {
  var title = document.getElementById('ll_title').value.trim();
  if (!title) { alert('법규명을 입력해주세요.'); document.getElementById('ll_title').focus(); return false; }
  var dept  = document.getElementById('ll_dept').value.trim();
  if (!dept)  { alert('관리부처를 입력해주세요.'); document.getElementById('ll_dept').focus(); return false; }
  var isUse = document.querySelector('input[name="ll_is_use"]:checked');
  if (!isUse) { alert('사용여부를 선택해주세요.'); return false; }
  return true;
}
</script>
</body>
</html>
