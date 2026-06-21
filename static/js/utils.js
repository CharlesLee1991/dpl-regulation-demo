///// Input	validate : START
// input
function chkInput(argTarget, argTxt){
	if (!$(argTarget).val()){
		if (argTxt)		alert(chkJosa(argTxt,"을") + " 입력해 주세요.");
		$(argTarget).focus();
		return false;
	}else{
		return true;
	}
}

// select (input과 동일. 메시지만 다름)
function chkInputSel(argTarget, argTxt){
	if (!$(argTarget).val()){
		if (argTxt)		alert(chkJosa(argTxt,"을") + " 선택해 주세요.");
		$(argTarget).focus();
		return false;
	}else{
		return true;
	}
}

// checkebox, radio
function chkInputChk(argTarget, argTxt, argMinCnt){
	if ($(argTarget + ":checked").length < argMinCnt){
		if (argTxt)		alert(chkJosa(argTxt,"을") + " 선택해 주세요.");
		$(argTarget+":eq(0)").focus();
		return false;
	}else{
		return true;
	}
}


// checkAll
var	global_chkall =	false;
function checkAllBtn(argTarget, argTxt){
	if (global_chkall) {
		global_chkall =	false; 
		$(argTxt).html("전체선택");
	} else {
		global_chkall =	true; 
		$(argTxt).html("전체해제");
	}

	checkAll(argTarget,global_chkall);
}

function checkAll(argTarget, argChk){
	//var	$checkboxes	= $(argTarget).find(':checkbox');
	var	$checkboxes	= $(argTarget);
	
	$checkboxes.prop("checked",	argChk);
}

function cntChecked(argTarget){
	var	chkCnt = $(argTarget).find(':checkbox:checked').length;
	
	return chkCnt;
}

function getCheckedVal(argTarget){
	var	chkVal;
	var	items =	new	Array;
	
	$("input[name='" + argTarget + "']:checkbox:checked").each(function(){
		items.push($(this).val());
	});	
	
	chkVal = items.join(',');
	
	return chkVal;
}

function getCheckboxAllVal(argTarget){
	var	chkVal;
	var	items =	new	Array;
	
	$("input[name='" + argTarget + "']:checkbox").each(function(){
		items.push($(this).val());
	});	
	
	chkVal = items.join(',');
	
	return chkVal;
}

function chkPassword(str, len){
	// 영문+숫자+특수문자
	//var pRegex = new RegExp("^(((?=.*[a-z])(?=.*[0-9])(?=.*[!@#\$%\^&\*]))|((?=.*[A-Z])(?=.*[0-9])(?=.*[!@#\$%\^&\*])))(?=.{"+len+",})");
	// 영문+숫자
	var pRegex = new RegExp("^(((?=.*[a-z])(?=.*[0-9]))|((?=.*[A-Z])(?=.*[0-9])))(?=.{"+len+",})");
	return pRegex.test(str);
}

function isEmail (emailStr)	{ // E-Mail	체크
	var	emailPat=/^(.+)@(.+)$/;
	var	specialChars="\\(\\)<>@,;:\\\\\\\"\\.\\[\\]";
	var	validChars="\[^\\s"	+ specialChars + "\]";
	var	firstChars=validChars;
	var	quotedUser="(\"[^\"]*\")";
	var	ipDomainPat=/^\[(\d{1,3})\.(\d{1,3})\.(\d{1,3})\.(\d{1,3})\]$/;
	var	atom="(" + firstChars +	validChars + "*" + ")";
	var	word="(" + atom	+ "|" +	quotedUser + ")";
	var	userPat=new	RegExp("^" + word +	"(\\." + word +	")*$");
	var	domainPat=new RegExp("^" + atom	+ "(\\." + atom	+")*$");
	
	
	var	matchArray=emailStr.match(emailPat);
	if (matchArray==null) {	
		//alert("@,.")
		return false;
	} 
	var	user=matchArray[1];
	var	domain=matchArray[2];
	
	if (user.match(userPat)==null) { 
		//alert("id") 
		return false;
	} 
	
	var	IPArray=domain.match(ipDomainPat) ;
	if (IPArray!=null) { 
		for	(var i=1;i<=4;i++) { 
			if (IPArray[i]>255)	{ 
				//alert("IP") 
				return false;
			} 
		} 
		return true;
	} 
	
	var	domainArray=domain.match(domainPat);
	
	if (domainArray==null) { 
		//alert("domain") 
		return false;
	} 
	var	atomPat=new	RegExp(atom,"g");
	var	domArr=domain.match(atomPat);
	var	len=domArr.length;
	
	if (domArr[domArr.length-1].length<2 ||	
		domArr[domArr.length-1].length>3) {	
		//alert("country") 
		return false;
	} 
	
	if (domArr[domArr.length-1].length==2 && len<3)	{ 
		var	errStr="This address ends in two characters, which is a	country";
		errStr+=" code.	Country	codes must be preceded by ";
		errStr+="a hostname	and	category (like com,	co,	pub, pu, etc.)";
		//alert(errStr)
		return false;
	} 
	
	if (domArr[domArr.length-1].length==3 && len<2)	{
		//var errStr="hostname"	
		//alert(errStr)
		return false;
	} 
	return true; 
}

///// Input	validate : END

function chkJosa(txt, josa)
{
	var code = txt.charCodeAt(txt.length-1) - 44032;
	var cho = 19, jung = 21, jong=28;
	var i1, i2, code1, code2;

	// 원본 문구가 없을때는 빈 문자열 반환
	if (txt.length == 0) return '';

	// 한글이 아닐때
	if (code < 0 || code > 11171) return txt;

	if (code % 28 == 0) return txt + chkJosa.get(josa, false);
	else return txt + chkJosa.get(josa, true);
}

chkJosa.get = function (josa, jong) {
	// jong : true면 받침있음, false면 받침없음

	if (josa == '을' || josa == '를') return (jong?'을':'를');
	if (josa == '이' || josa == '가') return (jong?'이':'가');
	if (josa == '은' || josa == '는') return (jong?'은':'는');
	if (josa == '와' || josa == '과') return (jong?'와':'과');

	// 알 수 없는 조사
	return '**';
}

/* file download */
function DownLoad(spath,fname,idx){
	var browser = chkBrowser();
	var fname = encodeURIComponent(fname);
	var ref = encodeURIComponent(location.href);
	var downUrl = "/common/dev/Download.php?spath=" + spath + "&file=" + fname + "&idx=" + idx + "&ref=" + ref;
	if (browser == "IPHONE"){
		pop = window.open("","","");
		pop.location.href=downUrl;
	/*
	}else if (browser == "ANDROID"){
		window.location.href = "/fileupload/" + spath + "/" + fname;
	*/
	}else{
		window.location.href=downUrl;
	}

}

/* input use numeric : onkeydown */
function OnlyNumber(e, decimal) {
	var key;
	var keychar;

	if (window.event) {
		key = window.event.keyCode;
	} else if (e) {
		key = e.which;
	} else {
		return true;
	}
	
	keychar = String.fromCharCode(key);

	if ((key == null) || (key == 0) || (key == 8) || (key == 9) || (key == 13) || (key == 27) || (key >= 96 && key <= 105)) {
		return true;
	} else if ((("0123456789").indexOf(keychar) > -1)) {
		return true;
	} else if (decimal && (keychar == ".")) {
		return true;
	} else
		return false;
}

function rtnError(msg,url){
	if (msg){
		alert(msg);
	}
	
	if (url=="BACK"){
		history.back();
	}else{
		location.href=url;
	}
}

/* 브라우져 체크 */
function chkBrowser(){
	var browser;
	if(navigator.userAgent.indexOf("MSIE") != -1){
		browser = "IE";
	}else if(navigator.userAgent.indexOf("Chrome") != -1){
		browser = "CHROME";
	}else if(navigator.userAgent.indexOf("Safari") != -1){
		browser = "SAFARI";
	}else if(navigator.userAgent.indexOf('iPhone') != -1 || navigator.userAgent.indexOf('iPad') != -1){
		browser = "IPHONE";
	}else if(navigator.userAgent.indexOf('Android') != -1 || navigator.userAgent.indexOf('Android') != -1){
		browser = "ANDROID";
	}else{
		browser = "OTHER";
	}
	
	return browser;
	
}

/* 파일 확장자 체크*/
function chkAllowFileExts(file,exts) {
	
	if (!file || !exts) {
		return true;
	}else{
		var file_ext = file.slice(file.indexOf(".") + 1).toLowerCase();
		if (exts.indexOf(file_ext) < 0)
			return false;
		else
			return true;
	}
}

/* input에 글자수 제한*/
function limitCharacters(textid, limit, limitid) {
	// 잆력 값 저장
	var text = $('#'+textid).val();
	var rtn = false;
	
	// 입력값 길이 저장
	var textlength = text.length;
	if(textlength > limit) {
		// 제한 글자 길이만큼 값 재 저장
		$('#'+textid).val(text.substr(0,limit));
		textlength = $('#'+textid).val().length;
		rtn =  false;
	} else {
		rtn =  true;
	}
	$('#' + limitid).html( textlength + ' /  '+limit+ '자');
	return rtn;
}

/* Cookie Setting */
function setCookie( name, value, expiredays )
{
	var todayDate = new Date();
	todayDate.setDate( todayDate.getDate() + expiredays);
	document.cookie = name + "=" + escape( value ) + "; path=/; expires=" + todayDate.toGMTString() + ";"
}

function getCookie( name ) {
	var nameOfCookie = name + "=";
	var x = 0;
	while ( x <= document.cookie.length )
	{
			var y = (x+nameOfCookie.length);
			if ( document.cookie.substring( x, y ) == nameOfCookie ) {
					if ( (endOfCookie=document.cookie.indexOf( ";", y )) == -1 )
							endOfCookie = document.cookie.length;
					return unescape( document.cookie.substring( y, endOfCookie ) );
			}
			x = document.cookie.indexOf( " ", x ) + 1;
			if ( x == 0 )
					break;
	}
	return "";
}

function replaceAll(str,searchStr,replaceStr){
	if (str){
		while (str.indexOf(searchStr) != -1) {
			str = str.replace(searchStr, replaceStr);
		}
	}
	return str;
}

//콤마(,) 제거	##################################################
function stripComma(str) {
	var	re = /,/g;
	return str.replace(re, "");
}

// 숫자	3자리수마다	콤마(,)	찍기 ##################################################
function formatComma(num, pos) {
	if (!pos) pos =	0;	//소숫점 이하 자리수
	var	re = /(-?\d+)(\d{3}[,.])/;

	var	strNum = stripComma(num.toString());
	var	arrNum = strNum.split(".");

	arrNum[0] += ".";

	while (re.test(arrNum[0])) {
		arrNum[0] =	arrNum[0].replace(re, "$1,$2");
	}

	if (arrNum.length >	1) {
		if (arrNum[1].length > pos)	{
			arrNum[1] =	arrNum[1].substr(0,	pos);
		}
		return arrNum.join("");
	}
	else {
		return arrNum[0].split(".")[0];
	}
}

//반올림 ##################################################
//num:	대상 숫자, pos:	대상 자리수
function setRound(num, pos)	{
	if(!pos) pos = 0;
	return Math.round(num *	Math.pow(10, pos)) / Math.pow(10, pos);
}

// 날짜 관련 함수 ##################################################
function date_add (y,mm,dd,flag,add){
	var rtn = y+"-"+mm+"-"+dd;
	
	flag = flag.toUpperCase();
	
	if (is_date(rtn)){
		var date_b = new Date(mm + "-" + dd + "-" + y);
		var date_e = new Date();
		
		if (flag=="D")
			date_e.setDate(date_b.getDate()+add);
		else if (flag=="M")
			date_e.setMonth(date_b.getMonth()+add);
		else if (flag=="Y")
			date_e.setYear(date_b.getYear()+add);
		else
			date_e.setDate(date_b.getDate());

		var dtY = date_e.getFullYear();
		var dtM = date_e.getMonth() + 1; if (dtM<10)	dtM = "0"+ dtM;
		var dtD = date_e.getDate(); if (dtD<10)	dtD = "0"+ dtD;
		
		rtn = dtY + "-" + dtM + "-" + dtD;
	}
	
	return rtn;	
}

function is_date(dateStr) {
	
	//var datePat =	/^(\d{1,2})(\/|-)(\d{1,2})(\/|-)(\d{4})$/;
	var	datePat	= /^(\d{1,4})(\-)(\d{1,2})(\-)(\d{2})$/;
	var	matchArray = dateStr.match(datePat); //	is the format ok?
	
	if (matchArray == null)	{
		//alert("Please	enter date as either mm/dd/yyyy	or mm-dd-yyyy.");
		return false;
	}
	
	year = matchArray[1]; // p@rse date	into variables
	month =	matchArray[3];
	day	= matchArray[5];

	if (month <	1 || month > 12) { // check	month range
		//alert("Month must	be between 1 and 12.");
		return false;
	}
	
	if (day	< 1	|| day > 31) {
		//alert("Day must be between 1 and 31.");
		return false;
	}
	
	if ((month==4 || month==6 || month==9 || month==11)	&& day==31)	{
		//alert("Month "+month+" doesn`t have 31 days!")
		return false;
	}
	
	if (month == 2)	{ // check for february	29th
		var	isleap = (year % 4 == 0	&& (year % 100 != 0	|| year	% 400 == 0));
		if (day	> 29 ||	(day==29 &&	!isleap)) {
			//alert("February "	+ year + " doesn`t have	" +	day	+ "	days!");
			return false;
		}
	}
	return true; //	date is	valid
}
function is_date2(y,m,d) {
	var strDt;
	m = String(m);
	d = String(d);
	
	if (m.length==1)
		m = "0"+m;	
	if (d.length==1)
		d = "0"+d;	
	
	return is_date(y+"-"+m+"-"+d);
}

//Popup Open / Close ##################################################
function popOpen(idx,ptype){
	if (ptype=="L"){
		$.ajax({
			url: "/popup/popup.php",
			type: "get",
			data: {"idx" : idx},
			dataType: "html",
			success: function(data){
				$(window.document.body).append(data);
			}
		})
	} else {
		window.open("/popup/popup.php?idx="+idx, "popup_"+idx, "width=50, height=50, left=0, top=0, toolbar=no, menubar=no, scrollbars=no");
		console.log(idx);
	}
}
function popClose(flag,idx,ptype){
	if (flag=="today"){
		var utc = new Date().toJSON().slice(0,10);
		setCookie("popup_"+idx, utc, 1);
	}
	if (ptype=="L") {
		$("#popup_"+idx).hide();
	}else{
		self.close();
	}
}
//VOD Player ##################################################
function setVodPlayer(url,thumb,w,h){
	var rtn;
/*
	rtn = "<video width='"+w+"' height='"+h+"' poster='"+thumb+"' controls='controls'>"
		+ "<source src='" + url + "' />"
		+ "<object width='"+w+"' height='"+h+"' classid='clsid:02BF25D5-8C17-4B23-BC80-D3488ABDDC6B'><!"
		+ "[endif]--><!--[if !IE]><!-->"
		+ "<object width='"+w+"' height='"+h+"'  data='" + url + "'><!--<![endif]-->"
		+ "<param name='src' value='" + url + "' />"
		+ "<param name='autoplay' value='false' />"
		+ "<param name='showlogo' value='false' />"
		+ "<object width='"+w+"' height='"+h+"' type='application/x-shockwave-flash'"
		+ "	data='__FLASH__.swf?image="+thumb+"&amp;file=" + url + "'>"
		+ "	<param name='movie' value='__FLASH__.swf?image="+thumb+"&amp;file=" + url + "' />"
		+ "	<img src='"+thumb+"' width='"+w+"' height='"+h+"' />"
		+ "	<p>"
		+ "		<strong>No video playback capabilities detected.</strong>"
		+ "		Why not try to download the file instead?<br />"
		+ "		<a href='" + url + "'>download (Windows / Mac)</a> |"
		+ "	</p>"
		+ "</object><!--[if gt IE 6]><!-->"
		+ "</object><!--<![endif]-->"
		+ "</video>";	
*/
	var browser = chkBrowser();
	
	if(browser == "IE"){ // Explorer일때만 실행
		rtn = "<OBJECT ID='MediaPlayer' Name='MediaPlayer' classid='clsid:22D6F312-B0F6-11D0-94AB-0080C74C7E95' codebase='http://activex.microsoft.com/activex/controls/mplayer/en/nsmp2inf.cab#Version=5,1,52,701' standby='Loading Microsoft Windows Media Player components...' type='application/x-oleobject ' bgcolor='DarkBlue' width='"+w+"' Height='"+h+"' VIEWASTEXT>"
		+ "<param name='Filename' value='"+url+"'>"
		+ "<PARAM NAME='AutoStart' VALUE='1'>"
		+ "<PARAM NAME='AutoSize' VALUE='0'>"
		+ "<PARAM NAME='Volume' VALUE='50'>"
		+ "<PARAM NAME='ShowCaptioning' VALUE='0'>"
		+ "<PARAM NAME='TransparentAtStart' value='0'>"
		+ "<EMBED type='application/x-mplayer2' pluginspage='http://www.microsoft.com/Windows/Downloads/Contents/Products/MediaPlayer/' id='MediaPlayer' Name='MediaPlayer' AutoSize='0' autostart='1'  filename='"+url+"'>"
		+ "</EMBED>"
		+ "</OBJECT>";
	/*
	}else if(browser == "CHROME"){
		rtn = "<OBJECT ID='MediaPlayer' Name='MediaPlayer' style='display:block' type='application/x-mplayer2' data='' width='"+w+"px;' Height='"+h+"px;' >"
		+ "<param name='src' value='"+url+"'>"
		+ "<PARAM NAME='controller' value='true'>"
		+ "<PARAM NAME='autostart' value='true'>"
		+ "</OBJECT>";
	*/
	}else if(browser == "IPHONE" || browser == "ANDROID" || browser == "SAFARI" || browser == "CHROME"){
		//alert("영상을 재생할 수 없습니다.");
		rtn = "<div style='display:block;text-align:center; width:100%; height:360px;'>"
		+ "<div style='line-height:20px;'>영상을 재생할 수 없습니다. 아래 링크에서 다운로드 받아 재생하세요.<br>"
		+ "<a href='"+url+"'><strong>다운로드</strong></a>"
		+ "</p></div>";
	}else{
		rtn = "<EMBED src='"+url+"' AutoSize='0' AutoStart='1' width='"+w+"px;' height='"+h+"px;' type='application/x-mplayer2' pluginspage='http://microsoft.com/windows/mediaplayer/en/download/' id='MediaPlayer' name='MediaPlayer' displaysize='4' >"
		+ "</EMBED>";
	} 
	return rtn;
}

function stopVod(){
	var browser = chkBrowser();
	var objPlayer = document.getElementById("MediaPlayer");
	
	if(browser == "IE"){ // Explorer일때만 실행
		objPlayer.Stop();
	/*
	}else if(browser == "CHROME"){
		objPlayer.Stop();
	*/
	}else if(browser == "IPHONE" || browser == "ANDROID" || browser == "SAFARI" || browser == "CHROME"){

	}else{
		objPlayer.Stop();
	} 
}


// sns share
/*
	<!-- sns share : tag //-->
	<meta property="og:title" content="사이트명" />
	<meta property="og:description" content="노출 컨텐츠" />
	<meta property="og:image" content="썸네일 URL" />
	<!-- sns share : tag //-->
*/
$(function(){
	// facebook share button
	$("a[data-action='share']").click(function(){
		var s_ch = $(this).attr('data-share');
		var s_url = "";
		var opts = "";
		var rtn_url = window.location.href;
		/*
		if (window.location.pathname){
			rtn_url = window.location.protocol + "//" + window.location.host ;
		}else{
			rtn_url = window.location.protocol + "//" + window.location.host + window.location.pathname;
		}
		*/
		
		if (s_ch=="fb"){
			s_url = "https://www.facebook.com/sharer/sharer.php?u="+encodeURIComponent(rtn_url + location.search.replace(/&/g, '&amp;'));
			opts = "toolbar=0,status=0,width=626,height=476";
		} else if (s_ch=="tw"){
			s_url = "http://twitter.com/share?url="+rtn_url;
			//s_url = "http://twitter.com/share?url="+rtn_url+"&text="+encodeURI(msg);
			opts = "toolbar=0,status=0,width=626,height=450";
		} else if (s_ch=="kkos"){
			s_url = "https://story.kakao.com/s/share?url="+encodeURIComponent(rtn_url);
			opts = "menubar=no,toolbar=no,resizable=yes,scrollbars=yes,height=600,width=600";
		} else if (s_ch=="gp"){
			s_url = "https://plus.google.com/share?url=" + encodeURIComponent(rtn_url);
			opts = "menubar=no,toolbar=no,resizable=yes,scrollbars=yes,height=600,width=600";
		}
		
		if (s_url)	
			window.open(s_url,s_ch+"_share",opts);
			
		return false;
	});
});