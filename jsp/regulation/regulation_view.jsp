<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>규제사항 상세</title>
<style>
  body { font-family: 'Malgun Gothic', sans-serif; font-size: 13px; }
  .wrap { max-width: 900px; margin: 20px auto; padding: 0 15px; }
  h2 { font-size: 18px; margin-bottom: 15px; }
  .tbl-view { width: 100%; border-collapse: collapse; margin-bottom: 15px; }
  .tbl-view th { background: #f0f4ff; width: 18%; padding: 10px;
                  border: 1px solid #ddd; text-align: left; font-weight: bold; }
  .tbl-view td { padding: 10px 12px; border: 1px solid #ddd; white-space: pre-wrap; }
  .btn-wrap { text-align: center; }
  .btn { padding: 7px 20px; border: none; border-radius: 3px; cursor: pointer;
         font-size: 13px; margin: 0 4px; }
  .btn-primary { background: #3d6fd4; color: #fff; }
  .btn-default { background: #777; color: #fff; }
</style>
</head>
<body>
<div class="wrap">
  <h2>규제사항 상세</h2>
  <c:choose>
    <c:when test="${empty info}">
      <p style="color:#999">데이터를 찾을 수 없습니다.</p>
    </c:when>
    <c:otherwise>
      <%-- ASP 원본 regulation_form.asp 라벨 직역 (읽기 모드) --%>
      <table class="tbl-view">
        <tr><th>관리제도</th><td>${fn:escapeXml(info.lr_title)}</td></tr>
        <tr><th>규제법률</th><td>${fn:escapeXml(info.ll_title)}</td></tr>
        <tr><th>사전이행요건</th><td>${fn:escapeXml(info.lr_condition)}</td></tr>
        <tr><th>인증표시방법</th><td>${fn:escapeXml(info.lr_certify_guide)}</td></tr>
        <tr><th>벌칙</th><td>${fn:escapeXml(info.lr_penalty)}</td></tr>
        <tr><th>사용여부</th><td>${info.lr_is_use eq 'Y' ? '사용' : '미사용'}</td></tr>
        <tr><th>등록일</th><td>${info.lr_reg_date} (${fn:escapeXml(info.lr_reg_user)})</td></tr>
        <tr><th>수정일</th><td>${info.lr_upd_date} (${fn:escapeXml(info.lr_upd_user)})</td></tr>
      </table>
    </c:otherwise>
  </c:choose>
  <div class="btn-wrap">
    <button class="btn btn-primary"
      onclick="location.href='?mode=form&lr_idx=${lrIdx}&page=${page}&qKey=${fn:escapeXml(qKey)}&qWord=${fn:escapeXml(qWord)}&qLL=${qLL}'">수정</button>
    <button class="btn btn-default"
      onclick="location.href='?mode=list&page=${page}&qKey=${fn:escapeXml(qKey)}&qWord=${fn:escapeXml(qWord)}&qLL=${qLL}'">목록</button>
  </div>
</div>
</body>
</html>
