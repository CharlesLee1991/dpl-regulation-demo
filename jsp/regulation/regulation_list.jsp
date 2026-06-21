<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>규제사항 목록</title>
<style>
  body { font-family: 'Malgun Gothic', sans-serif; font-size: 13px; }
  .wrap { max-width: 1100px; margin: 20px auto; padding: 0 15px; }
  h2 { font-size: 18px; margin-bottom: 15px; }
  /* 검색 폼 */
  .search-bar { background: #f5f5f5; padding: 10px 15px; border-radius: 4px;
                display: flex; gap: 8px; align-items: center; margin-bottom: 15px; flex-wrap: wrap; }
  .search-bar select, .search-bar input[type=text] {
    padding: 5px 8px; border: 1px solid #ccc; border-radius: 3px; font-size: 13px; }
  .btn { padding: 5px 14px; border: none; border-radius: 3px; cursor: pointer; font-size: 13px; }
  .btn-primary { background: #3d6fd4; color: #fff; }
  .btn-default { background: #aaa; color: #fff; }
  .btn-success { background: #2e7d32; color: #fff; }
  /* 테이블 */
  .tbl { width: 100%; border-collapse: collapse; margin-bottom: 10px; }
  .tbl th { background: #3d6fd4; color: #fff; padding: 8px; font-weight: normal;
             border: 1px solid #2a56b0; text-align: center; }
  .tbl td { padding: 7px 10px; border: 1px solid #ddd; vertical-align: middle; }
  .tbl tr:hover td { background: #f0f4ff; }
  .tbl td.center { text-align: center; }
  .tbl td a { color: #1a3d8a; text-decoration: none; }
  .tbl td a:hover { text-decoration: underline; }
  /* 페이징 */
  .paging { text-align: center; margin-top: 10px; }
  .paging a { display: inline-block; padding: 4px 10px; margin: 0 2px;
               border: 1px solid #ccc; border-radius: 3px; text-decoration: none; color: #333; }
  .paging a.cur { background: #3d6fd4; color: #fff; border-color: #3d6fd4; }
  .total-bar { text-align: right; font-size: 12px; color: #666; margin-bottom: 4px; }
</style>
</head>
<body>
<div class="wrap">
  <h2>규제사항 관리</h2>

  <%-- 검색 폼 (ASP 원본: qLL 규제법률 + qKey/qWord) --%>
  <form id="frmSearch" method="get" action="">
    <input type="hidden" name="mode" value="list">
    <div class="search-bar">
      <%-- 규제법률 드롭다운 (ASP: SB_ArrayToSelectBox arListLegal) --%>
      <select name="qLL">
        <option value="0">-- 규제법률 전체 --</option>
        <c:forEach var="ll" items="${legalList}">
          <option value="${ll.ll_idx}"
            <c:if test="${ll.ll_idx == qLL}">selected</c:if>>
            ${fn:escapeXml(ll.ll_title)}
          </option>
        </c:forEach>
      </select>
      <%-- 검색어 --%>
      <select name="qKey">
        <option value="" <c:if test="${qKey==''}">selected</c:if>>전체</option>
        <option value="TITLE" <c:if test="${qKey=='TITLE'}">selected</c:if>>관리제도</option>
      </select>
      <input type="text" name="qWord" value="${fn:escapeXml(qWord)}" placeholder="검색어" style="width:200px">
      <button type="submit" class="btn btn-primary">검색</button>
      <button type="button" class="btn btn-default"
        onclick="document.querySelectorAll('#frmSearch input[type=text]').forEach(e=>e.value='');
                 document.querySelectorAll('#frmSearch select').forEach(e=>e.selectedIndex=0);
                 this.form.submit();">초기화</button>
      <div style="flex:1"></div>
      <button type="button" class="btn btn-success"
        onclick="location.href='?mode=form'">신규등록</button>
    </div>
  </form>

  <%-- 목록 --%>
  <div class="total-bar">전체 <strong>${total}</strong>건</div>
  <%-- 컬럼: ASP 원본 = No / 관리제도(LR_TITLE) / 규제법률(LL_TITLE) / 노출(LR_IS_USE) / 등록일 --%>
  <table class="tbl">
    <thead>
      <tr>
        <th style="width:60px">No.</th>
        <th>관리제도</th>
        <th style="width:160px">규제법률</th>
        <th style="width:60px">노출</th>
        <th style="width:130px">등록일</th>
      </tr>
    </thead>
    <tbody>
      <c:choose>
        <c:when test="${empty list}">
          <tr><td colspan="5" class="center" style="padding:30px;color:#999">검색된 내용이 없습니다.</td></tr>
        </c:when>
        <c:otherwise>
          <c:forEach var="row" items="${list}" varStatus="st">
            <tr>
              <td class="center">${total - (page-1)*listSize - st.index}</td>
              <td>
                <a href="?mode=form&lr_idx=${row.lr_idx}&page=${page}&qKey=${fn:escapeXml(qKey)}&qWord=${fn:escapeXml(qWord)}&qLL=${qLL}">
                  ${fn:escapeXml(row.lr_title)}
                </a>
              </td>
              <td class="center">${fn:escapeXml(row.ll_title)}</td>
              <td class="center">${row.lr_is_use eq 'Y' ? '사용' : '미사용'}</td>
              <td class="center">${row.lr_reg_date}</td>
            </tr>
          </c:forEach>
        </c:otherwise>
      </c:choose>
    </tbody>
  </table>

  <%-- 페이징 --%>
  <div class="paging">
    <c:if test="${page > 1}">
      <a href="?mode=list&page=1&qKey=${fn:escapeXml(qKey)}&qWord=${fn:escapeXml(qWord)}&qLL=${qLL}">◀◀</a>
      <a href="?mode=list&page=${page-1}&qKey=${fn:escapeXml(qKey)}&qWord=${fn:escapeXml(qWord)}&qLL=${qLL}">◀</a>
    </c:if>
    <c:forEach begin="1" end="${pageCnt}" var="p">
      <c:if test="${p >= page-4 && p <= page+4}">
        <a href="?mode=list&page=${p}&qKey=${fn:escapeXml(qKey)}&qWord=${fn:escapeXml(qWord)}&qLL=${qLL}"
           class="${p==page ? 'cur' : ''}">${p}</a>
      </c:if>
    </c:forEach>
    <c:if test="${page < pageCnt}">
      <a href="?mode=list&page=${page+1}&qKey=${fn:escapeXml(qKey)}&qWord=${fn:escapeXml(qWord)}&qLL=${qLL}">▶</a>
      <a href="?mode=list&page=${pageCnt}&qKey=${fn:escapeXml(qKey)}&qWord=${fn:escapeXml(qWord)}&qLL=${qLL}">▶▶</a>
    </c:if>
  </div>
</div>
</body>
</html>
