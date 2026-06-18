<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>규제사항 ${action eq 'ADD' ? '등록' : '수정'}</title>
<style>
  body { font-family: 'Malgun Gothic', sans-serif; font-size: 13px; }
  .wrap { max-width: 900px; margin: 20px auto; padding: 0 15px; }
  h2 { font-size: 18px; margin-bottom: 15px; }
  .tbl-form { width: 100%; border-collapse: collapse; margin-bottom: 15px; }
  .tbl-form th { background: #f0f4ff; width: 18%; padding: 10px;
                  border: 1px solid #ddd; text-align: left; font-weight: bold; vertical-align: top; }
  .tbl-form td { padding: 8px 12px; border: 1px solid #ddd; }
  .tbl-form input[type=text], .tbl-form select, .tbl-form textarea {
    width: 98%; padding: 6px 8px; border: 1px solid #ccc; border-radius: 3px;
    font-size: 13px; font-family: inherit; box-sizing: border-box; }
  .tbl-form textarea { height: 100px; resize: vertical; }
  .req { color: #e53935; }
  .radio-group label { margin-right: 15px; cursor: pointer; }
  .btn-wrap { text-align: center; margin-top: 10px; }
  .btn { padding: 7px 20px; border: none; border-radius: 3px; cursor: pointer; font-size: 13px; margin: 0 4px; }
  .btn-primary { background: #3d6fd4; color: #fff; }
  .btn-danger  { background: #c62828; color: #fff; }
  .btn-default { background: #777; color: #fff; }
</style>
</head>
<body>
<div class="wrap">
  <h2>규제사항 ${action eq 'ADD' ? '등록' : '수정'}</h2>

  <form id="frmInfo" method="post" action="?mode=proc" onsubmit="return jfSubmit()">
    <input type="hidden" name="mode"    value="proc">
    <input type="hidden" name="action"  value="${action}">
    <input type="hidden" name="lr_idx"  value="${lrIdx}">
    <input type="hidden" name="page"    value="${page}">
    <input type="hidden" name="qKey"    value="${fn:escapeXml(qKey)}">
    <input type="hidden" name="qWord"   value="${fn:escapeXml(qWord)}">
    <input type="hidden" name="qLL"     value="${qLL}">

    <table class="tbl-form">
      <tr>
        <th>규제사항명 <span class="req">*</span></th>
        <td>
          <input type="text" id="lr_title" name="lr_title"
                 value="${fn:escapeXml(info.lr_title)}" placeholder="규제사항명 입력">
        </td>
      </tr>
      <tr>
        <th>규제법률 <span class="req">*</span></th>
        <td>
          <select id="ll_idx" name="ll_idx">
            <option value="0">-- 선택 --</option>
            <c:forEach var="ll" items="${legalList}">
              <option value="${ll.ll_idx}"
                <c:if test="${ll.ll_idx == info.ll_idx}">selected</c:if>>
                ${fn:escapeXml(ll.ll_title)}
              </option>
            </c:forEach>
          </select>
        </td>
      </tr>
      <tr>
        <th>적용조건</th>
        <td>
          <textarea name="lr_condition">${fn:escapeXml(info.lr_condition)}</textarea>
        </td>
      </tr>
      <tr>
        <th>인증 가이드</th>
        <td>
          <textarea name="lr_certify_guide">${fn:escapeXml(info.lr_certify_guide)}</textarea>
        </td>
      </tr>
      <tr>
        <th>벌칙/과태료</th>
        <td>
          <textarea name="lr_penalty">${fn:escapeXml(info.lr_penalty)}</textarea>
        </td>
      </tr>
      <tr>
        <th>사용여부 <span class="req">*</span></th>
        <td class="radio-group">
          <label>
            <input type="radio" name="lr_is_use" value="Y"
              <c:if test="${empty info.lr_is_use || info.lr_is_use eq 'Y'}">checked</c:if>> 사용
          </label>
          <label>
            <input type="radio" name="lr_is_use" value="N"
              <c:if test="${info.lr_is_use eq 'N'}">checked</c:if>> 미사용
          </label>
        </td>
      </tr>
      <c:if test="${action eq 'MOD'}">
        <tr>
          <th>등록일</th>
          <td>${info.lr_reg_date} (${info.lr_reg_user})</td>
        </tr>
        <tr>
          <th>수정일</th>
          <td>${info.lr_upd_date} (${info.lr_upd_user})</td>
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
        onclick="location.href='?mode=list&page=${page}&qKey=${fn:escapeXml(qKey)}&qWord=${fn:escapeXml(qWord)}'">목록</button>
    </div>
    <input type="hidden" id="action-field" name="action" value="${action}">
  </form>
</div>
<script>
function jfSubmit() {
  var title = document.getElementById('lr_title').value.trim();
  if (!title) { alert('규제사항명을 입력해주세요.'); document.getElementById('lr_title').focus(); return false; }
  var ll = document.getElementById('ll_idx').value;
  if (!ll || ll === '0') { alert('규제법률을 선택해주세요.'); return false; }
  var isUse = document.querySelector('input[name="lr_is_use"]:checked');
  if (!isUse) { alert('사용여부를 선택해주세요.'); return false; }
  return true;
}
</script>
</body>
</html>
