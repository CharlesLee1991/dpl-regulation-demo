<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html><html><head><meta charset="UTF-8"><title>세부품목정보 ${action eq 'ADD' ? '등록' : '수정'}</title>
<style>
  body{font-family:'Malgun Gothic',sans-serif;font-size:13px}
  .wrap{max-width:960px;margin:20px auto;padding:0 15px}
  h2{font-size:18px;margin-bottom:15px}
  .tbl-form{width:100%;border-collapse:collapse;margin-bottom:15px}
  .tbl-form th{background:#f0f4ff;width:18%;padding:10px;border:1px solid #ddd;text-align:left;font-weight:bold;vertical-align:top}
  .tbl-form td{padding:8px 12px;border:1px solid #ddd}
  .tbl-form input[type=text],.tbl-form select{padding:6px 8px;border:1px solid #ccc;border-radius:3px;font-size:13px;font-family:inherit;box-sizing:border-box}
  .tbl-form input[type=text]{width:98%}
  .req{color:#e53935}.radio-group label{margin-right:15px;cursor:pointer}
  .btn-wrap{text-align:center;margin-top:10px}
  .btn{padding:7px 20px;border:none;border-radius:3px;cursor:pointer;font-size:13px;margin:0 4px}
  .btn-primary{background:#3d6fd4;color:#fff}.btn-danger{background:#c62828;color:#fff}.btn-default{background:#777;color:#fff}
</style>
</head><body>
<div class="wrap">
  <h2>세부품목정보 (일반품목명) ${action eq 'ADD' ? '등록' : '수정'}</h2>
  <form id="frmInfo" method="post" action="?mode=proc" onsubmit="return jfSubmit()">
    <input type="hidden" name="mode" value="proc">
    <input type="hidden" name="action" id="action-field" value="${action}">
    <input type="hidden" name="ld_idx" value="${ldIdx}">
    <input type="hidden" name="page" value="${page}">
    <input type="hidden" name="qLL" value="${qLL}">
    <input type="hidden" name="qLR" value="${qLR}">
    <input type="hidden" name="qLN" value="${qLN}">
    <table class="tbl-form">
      <tr>
        <th><i class="req">*</i> 일반 품목명</th>
        <td><input type="text" id="ld_item_name" name="ld_item_name" value="${fn:escapeXml(info.ld_item_name != null ? info.ld_item_name : '')}" maxlength="100"></td>
      </tr>
      <%-- ASP 원본: 규제법률/사항/고시 3단계 + 법정품목명 4단계 --%>
      <tr>
        <th><i class="req">*</i> 규제법률/사항/고시</th>
        <td>
          <select id="ll_idx" name="ll_idx" onchange="loadLR(this.value)">
            <option value="0">-- 선택 --</option>
            <c:forEach var="ll" items="${legalList}">
              <option value="${ll.ll_idx}" <c:if test="${ll.ll_idx==info.ll_idx}">selected</c:if>>${fn:escapeXml(ll.ll_title)}</option>
            </c:forEach>
          </select>
          <select id="lr_idx" name="lr_idx" onchange="loadLN(this.value)">
            <option value="0">-- 선택 --</option>
            <c:forEach var="lr" items="${regulationList}">
              <option value="${lr.lr_idx}" <c:if test="${lr.lr_idx==info.lr_idx}">selected</c:if>>${fn:escapeXml(lr.lr_title)}</option>
            </c:forEach>
          </select>
          <select id="ln_idx" name="ln_idx" onchange="loadLI(this.value)">
            <option value="0">-- 선택 --</option>
            <c:forEach var="ln" items="${notifyList}">
              <option value="${ln.ln_idx}" <c:if test="${ln.ln_idx==info.ln_idx}">selected</c:if>>${fn:escapeXml(ln.ln_title != null ? ln.ln_title : '')}</option>
            </c:forEach>
          </select>
        </td>
      </tr>
      <%-- 4단계: 법정품목명 (ln_idx 선택 후 AJAX) --%>
      <tr>
        <th><i class="req">*</i> 법정품목명</th>
        <td>
          <select id="li_idx" name="li_idx">
            <option value="0">-- 선택 --</option>
            <c:forEach var="li" items="${itemsList}">
              <option value="${li.li_idx}" <c:if test="${li.li_idx==info.li_idx}">selected</c:if>>${fn:escapeXml(li.li_legal_name != null ? li.li_legal_name : '')}</option>
            </c:forEach>
          </select>
        </td>
      </tr>
      <tr>
        <th>사용연령(체중)</th>
        <td><input type="text" name="ld_use_age" value="${fn:escapeXml(info.ld_use_age != null ? info.ld_use_age : '')}" maxlength="100"></td>
      </tr>
      <tr>
        <th>재질(형태)</th>
        <td><input type="text" name="ld_material" value="${fn:escapeXml(info.ld_material != null ? info.ld_material : '')}" maxlength="100"></td>
      </tr>
      <tr>
        <th><i class="req">*</i> 사용여부</th>
        <td class="radio-group">
          <label><input type="radio" name="ld_is_use" value="Y" <c:if test="${empty info.ld_is_use || info.ld_is_use eq 'Y'}">checked</c:if>> 사용</label>
          <label><input type="radio" name="ld_is_use" value="N" <c:if test="${info.ld_is_use eq 'N'}">checked</c:if>> 미사용</label>
        </td>
      </tr>
      <c:if test="${action eq 'MOD'}">
        <tr><th>등록일</th><td>${info.ld_reg_date}</td></tr>
      </c:if>
    </table>
    <div class="btn-wrap">
      <button type="submit" class="btn btn-primary">저장</button>
      <c:if test="${action eq 'MOD'}">
        <button type="button" class="btn btn-danger"
          onclick="if(confirm('삭제?')){document.getElementById('action-field').value='DEL';document.getElementById('frmInfo').submit();}">삭제</button>
      </c:if>
      <button type="button" class="btn btn-default"
        onclick="location.href='?mode=list&page=${page}&qLL=${qLL}&qLR=${qLR}&qLN=${qLN}'">목록</button>
    </div>
  </form>
</div>
<script>
function loadLR(llIdx){
  var $lr=document.getElementById('lr_idx'),$ln=document.getElementById('ln_idx'),$li=document.getElementById('li_idx');
  while($lr.options.length>1)$lr.remove(1); while($ln.options.length>1)$ln.remove(1); while($li.options.length>1)$li.remove(1);
  if(!llIdx||llIdx==='0')return;
  fetch('/ajax/?type=regulation&qLL='+llIdx).then(r=>r.json()).then(d=>{
    (d.rows||[]).forEach(row=>{var o=document.createElement('option');o.value=row.lr_idx;o.text=row.lr_title;$lr.add(o);});
  }).catch(()=>{});
}
function loadLN(lrIdx){
  var $ln=document.getElementById('ln_idx'),$li=document.getElementById('li_idx');
  while($ln.options.length>1)$ln.remove(1); while($li.options.length>1)$li.remove(1);
  if(!lrIdx||lrIdx==='0')return;
  fetch('/ajax/?type=notify&qLR='+lrIdx).then(r=>r.json()).then(d=>{
    (d.rows||[]).forEach(row=>{var o=document.createElement('option');o.value=row.ln_idx;o.text=row.ln_title||row.ln_notify;$ln.add(o);});
  }).catch(()=>{});
}
function loadLI(lnIdx){
  var $li=document.getElementById('li_idx'); while($li.options.length>1)$li.remove(1);
  if(!lnIdx||lnIdx==='0')return;
  fetch('/ajax/?type=items&qLN='+lnIdx).then(r=>r.json()).then(d=>{
    (d.rows||[]).forEach(row=>{var o=document.createElement('option');o.value=row.li_idx;o.text=row.li_legal_name;$li.add(o);});
  }).catch(()=>{});
}
function jfSubmit(){
  if(!document.getElementById('ld_item_name').value.trim()){alert('일반 품목명을 입력해주세요');return false;}
  if(document.getElementById('ll_idx').value==='0'){alert('규제법률를 선택해주세요');return false;}
  if(document.getElementById('lr_idx').value==='0'){alert('규제사항을 선택해주세요');return false;}
  if(document.getElementById('ln_idx').value==='0'){alert('고시를 선택해주세요');return false;}
  if(document.getElementById('li_idx').value==='0'){alert('법정 품목명을 선택해주세요');return false;}
  if(!document.querySelector('input[name="ld_is_use"]:checked')){alert('사용여부를 선택해주세요');return false;}
  return true;
}
</script>
</body></html>
