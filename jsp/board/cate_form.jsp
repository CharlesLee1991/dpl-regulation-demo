<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<% String uri = request.getRequestURI(); %>
<!DOCTYPE html><html lang="ko"><head>
<meta charset="UTF-8"><meta http-equiv="X-UA-Compatible" content="IE=edge">
<title>${cateTitle} — DPL 법규정보 관리시스템</title>
<link rel="stylesheet" href="/static/css/admin_common.css">
<link rel="stylesheet" href="/static/css/jquery-ui.min.css">
<script src="/static/js/jquery-1.11.1.min.js"></script>
<script src="/static/js/jquery-ui.min.js"></script>
<script src="/static/js/utils.js"></script>
<script src="/static/js/admin_common.js"></script>
<script>
function jfSave(act){
  if($("#lc_category").val()==""){alert("카테고리명을 입력하세요.");$("#lc_category").focus();return;}
  $("#action").val(act);$("#frmInfo").submit();
}
function jfDelete(){ if(!confirm("삭제하시겠습니까?"))return; $("#action").val("DEL");$("#frmInfo").submit(); }
function jfList(){location.href="?mode=list&depth=${depth}&page=${page}&qWord=${fn:escapeXml(qWord)}";}
</script>
</head><body>
<div id="header">
  <div class="logo"><a href="/"><img src="/static/img_admin/common/logo.png" alt="롯데중앙연구소 안전센터"></a></div>
  <div class="side_menu"><div class="side_link my_info">
    <a href="#none"><strong>관리자</strong>DPL</a>
    <div class="my_info_view"><p><strong>관리자</strong><span>DPL 관리자</span></p>
      <div class="btn_c"><a href="#">개인정보 수정</a><a href="#">로그아웃</a></div></div>
  </div></div>
  <div class="gnb">
    <strong class="menu_1depth_01 menu_1depth on"><a href="/legal/?mode=list">법규정보DB 관리</a></strong>
    <div class="menu_2depth_01 menu_2depth"><ul>
      <li><p class="<%= uri.contains("/legal/") ? "on":"" %>"><a href="/legal/?mode=list">규제법률</a></p></li>
      <li><p class="<%= uri.contains("/regulation/") ? "on":"" %>"><a href="/regulation/?mode=list">규제사항</a></p></li>
      <li><p class="<%= uri.contains("/notify/") ? "on":"" %>"><a href="/notify/?mode=list">고시(부속서)</a></p></li>
      <li><p class="<%= uri.contains("/safety/") ? "on":"" %>"><a href="/safety/?mode=list">안전요건</a></p></li>
      <li><p><a href="#">표시사항</a></p></li>
      <li><p class="<%= uri.contains("items_def") ? "on":"" %>"><a href="/items_def/?mode=list">품목정보</a></p></li>
      <li><p class="<%= uri.contains("items_detail") ? "on":"" %>"><a href="/items_detail/?mode=list">세부품목정보</a></p></li>
      <li><p><a href="#">온라인표시사항</a></p></li>
      <li><p><a href="#">법규 제·개정 사항</a></p></li>
    </ul></div>
    <strong class="menu_1depth_02 menu_1depth"><a href="#">안전정보DB 관리</a></strong>
    <div class="menu_2depth_02 menu_2depth"><ul>
      <li><p><a href="/news_admin/?mode=list">제품안전 뉴스</a></p></li>
      <li><p><a href="/riskdb_admin/?mode=list">위해정보DB</a></p></li>
      <li><p><a href="/standard_admin/?mode=list">롯데스탠다드(품질기준서)</a></p></li>
    </ul></div>
    <strong class="menu_1depth_03 menu_1depth"><a href="#">셀프러닝 관리</a></strong>
    <div class="menu_2depth_03 menu_2depth"><ul>
      <li><p><a href="/shortclass/?mode=list">숏클래스</a></p></li>
      <li><p><a href="/board/?mode=list&code=6">유용한 정보</a></p></li>
      <li><p><a href="/board/?mode=list&code=8">동영상 정보</a></p></li>
      <li><p><a href="/board/?mode=list&code=10">안전센터정보</a></p></li>
    </ul></div>
    <strong class="menu_1depth_04 menu_1depth"><a href="#">카테고리 관리</a></strong>
    <div class="menu_2depth_04 menu_2depth"><ul>
      <li><p class="<%= uri.contains("/cate_admin/") && !"2".equals(request.getParameter("depth")) ? "on":"" %>"><a href="/cate_admin/?mode=list&depth=1">중분류 관리</a></p></li>
      <li><p class="<%= "2".equals(request.getParameter("depth")) ? "on":"" %>"><a href="/cate_admin/?mode=list&depth=2">소분류 관리</a></p></li>
    </ul></div>
    <strong class="menu_1depth_05 menu_1depth"><a href="#">배너 관리</a></strong>
    <div class="menu_2depth_05 menu_2depth"><ul>
      <li><p><a href="#">메인상단띠배너</a></p></li>
    </ul></div>
  </div>
</div>
<div id="container">
<div id="contents">
  <div class="title title_navi">
    <h2>${cateTitle} <c:choose><c:when test="${lcIdx>0}">수정</c:when><c:otherwise>등록</c:otherwise></c:choose></h2>
    <p class="navi"><span>카테고리 관리</span><span>${cateTitle}</span></p>
  </div>
  <form id="frmInfo" name="frmInfo" method="post" action="?mode=proc">
    <input type="hidden" name="mode" value="proc">
    <input type="hidden" name="depth" value="${depth}">
    <input type="hidden" name="action" id="action" value="">
    <input type="hidden" name="lc_idx" value="${lcIdx}">
    <input type="hidden" name="page" value="${page}">
    <input type="hidden" name="qWord" value="${fn:escapeXml(qWord)}">
    <div class="table_form">
      <table><colgroup><col width="150"><col width="*"></colgroup><tbody>
        <c:if test="${depth==2}">
          <tr><th>상위(중분류) <span class="req">*</span></th>
            <td><select name="lc_parent_idx">
              <option value="0">선택</option>
              <c:forEach var="pp" items="${parentList}">
                <option value="${pp.lc_idx}" ${info.lc_parent_idx==pp.lc_idx?'selected':''}>${fn:escapeXml(pp.lc_category)}</option>
              </c:forEach>
            </select></td></tr>
        </c:if>
        <tr><th>카테고리명 <span class="req">*</span></th>
          <td><input type="text" name="lc_category" id="lc_category" class="inp" value="${fn:escapeXml(info.lc_category)}" maxlength="15"></td></tr>
        <tr><th>노출</th>
          <td>
            <label><input type="radio" name="lc_is_use" value="Y" ${empty info || info.lc_is_use=='Y' ? 'checked':''}> 노출</label>
            <label><input type="radio" name="lc_is_use" value="N" ${info.lc_is_use=='N' ? 'checked':''}> 미노출</label>
          </td></tr>
      </tbody></table>
    </div>
    <div class="btn_wrap">
      <c:choose>
        <c:when test="${lcIdx>0}">
          <button type="button" class="btn btn_style_01" onclick="jfSave('MOD');">수정</button>
          <button type="button" class="btn btn_style_03" onclick="jfDelete();">삭제</button>
        </c:when>
        <c:otherwise>
          <button type="button" class="btn btn_style_01" onclick="jfSave('ADD');">등록</button>
        </c:otherwise>
      </c:choose>
      <button type="button" class="btn btn_style_02" onclick="jfList();">목록</button>
    </div>
  </form>
</div>
</div>
</body></html>
