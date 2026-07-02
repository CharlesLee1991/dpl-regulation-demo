<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<% String uri = request.getRequestURI(); %>
<!DOCTYPE html><html lang="ko"><head>
<meta charset="UTF-8"><meta http-equiv="X-UA-Compatible" content="IE=edge">
<title>위해정보DB — DPL 법규정보 관리시스템</title>
<link rel="stylesheet" href="/static/css/admin_common.css">
<link rel="stylesheet" href="/static/css/jquery-ui.min.css">
<script src="/static/js/jquery-1.11.1.min.js"></script>
<script src="/static/js/jquery-ui.min.js"></script>
<script src="/static/js/utils.js"></script>
<script src="/static/js/admin_common.js"></script>
<script>
function jfSave(act){
  if($("#rd_title").val()==""){alert("제목을 입력하세요.");$("#rd_title").focus();return;}
  $("#action").val(act);$("#frmInfo").submit();
}
function jfDelete(){ if(!confirm("삭제하시겠습니까?"))return; $("#action").val("DEL");$("#frmInfo").submit(); }
function jfList(){location.href="?mode=list&page=${page}&qKey=${fn:escapeXml(qKey)}&qWord=${fn:escapeXml(qWord)}";}
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
      <li><p class="<%= uri.contains("/riskdb_admin/") ? "on":"" %>"><a href="/riskdb_admin/?mode=list">위해정보DB</a></p></li>
      <li><p><a href="#">롯데스탠다드(품질기준서)</a></p></li>
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
      <li><p><a href="#">중분류 관리</a></p></li>
      <li><p><a href="#">소분류 관리</a></p></li>
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
    <h2>위해정보DB <c:choose><c:when test="${rdIdx>0}">수정</c:when><c:otherwise>등록</c:otherwise></c:choose></h2>
    <p class="navi"><span>안전정보DB 관리</span><span>위해정보DB</span></p>
  </div>
  <form id="frmInfo" name="frmInfo" method="post" action="?mode=proc">
    <input type="hidden" name="mode" value="proc">
    <input type="hidden" name="action" id="action" value="">
    <input type="hidden" name="rd_idx" value="${rdIdx}">
    <input type="hidden" name="page" value="${page}">
    <input type="hidden" name="qKey" value="${fn:escapeXml(qKey)}">
    <input type="hidden" name="qWord" value="${fn:escapeXml(qWord)}">
    <div class="table_form">
      <table><colgroup><col width="150"><col width="*"></colgroup><tbody>
        <tr><th>제목 <span class="req">*</span></th>
          <td><input type="text" name="rd_title" id="rd_title" class="inp _w_full" value="${fn:escapeXml(info.rd_title)}" maxlength="100"></td></tr>
        <tr><th>결함구분</th>
          <td><select name="rd_type">
            <option value="화학적결함" ${info.rd_type=='화학적결함'?'selected':''}>화학적결함</option>
            <option value="물리적결함" ${info.rd_type=='물리적결함'?'selected':''}>물리적결함</option>
            <option value="생물학적결함" ${info.rd_type=='생물학적결함'?'selected':''}>생물학적결함</option>
            <option value="전기적결함" ${info.rd_type=='전기적결함'?'selected':''}>전기적결함</option>
            <option value="환경적결함" ${info.rd_type=='환경적결함'?'selected':''}>환경적결함</option>
          </select></td></tr>
        <tr><th>위해요소</th>
          <td><input type="text" name="rd_factor" class="inp _w_full" value="${fn:escapeXml(info.rd_factor)}" maxlength="200"></td></tr>
        <tr><th>등급</th>
          <td><select name="rd_level">
            <option value="1" ${empty info || info.rd_level==1 ? 'selected':''}>관심</option>
            <option value="2" ${info.rd_level==2 ? 'selected':''}>주의</option>
            <option value="3" ${info.rd_level==3 ? 'selected':''}>경계</option>
            <option value="4" ${info.rd_level==4 ? 'selected':''}>심각</option>
          </select></td></tr>
        <tr><th>출처</th>
          <td><input type="text" name="rd_source" class="inp" value="${fn:escapeXml(info.rd_source)}" maxlength="100"></td></tr>
        <tr><th>URL</th>
          <td><input type="text" name="rd_link" class="inp _w_full" value="${fn:escapeXml(info.rd_link)}" maxlength="500"></td></tr>
        <tr><th>내용</th>
          <td><textarea name="rd_content" rows="12" class="inp _w_full">${fn:escapeXml(info.rd_content)}</textarea></td></tr>
        <tr><th>노출</th>
          <td>
            <label><input type="radio" name="rd_is_use" value="Y" ${empty info || info.rd_is_use=='Y' ? 'checked':''}> 노출</label>
            <label><input type="radio" name="rd_is_use" value="N" ${info.rd_is_use=='N' ? 'checked':''}> 미노출</label>
          </td></tr>
      </tbody></table>
    </div>
    <div class="btn_wrap">
      <c:choose>
        <c:when test="${rdIdx>0}">
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
