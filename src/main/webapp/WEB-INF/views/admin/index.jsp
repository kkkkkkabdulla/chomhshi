<%@ page contentType="text/html;charset=UTF-8" language="java" isELIgnored="true" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8"/>
    <title>管理后台</title>
    <style>
        body { margin:0; background:#f5f7fb; font-family:Arial,sans-serif; }
        .top { background:#1677ff; color:#fff; padding:12px 18px; display:flex; justify-content:space-between; align-items:center; }
        .main { padding:18px; }
        .card { background:#fff; border-radius:10px; padding:14px; box-shadow:0 6px 20px rgba(0,0,0,.06); margin-bottom:16px; }
        .row { display:flex; gap:10px; align-items:center; margin-bottom:10px; }
        .btn { padding:6px 10px; border:1px solid #d9dde7; background:#fff; border-radius:6px; cursor:pointer; }
        .btn-primary { background:#1677ff; color:#fff; border-color:#1677ff; }
        .tab-btn { padding:8px 16px; font-weight:600; border-radius:8px; transition:all .15s ease; }
        .tab-btn.active { background:#1677ff; color:#fff; border-color:#1677ff; box-shadow:0 6px 14px rgba(22,119,255,.22); }
        .tab-btn:not(.active):hover { background:#f0f5ff; border-color:#b7d1ff; color:#0b61d9; }
        .table { width:100%; border-collapse:collapse; }
        .table th,.table td { border-bottom:1px solid #eef1f6; padding:8px; text-align:left; font-size:14px; }
        .muted { color:#8b93a7; }
        .err { color:#e53935; }
        .modal-mask { position:fixed; inset:0; background:rgba(0,0,0,.45); display:none; align-items:center; justify-content:center; z-index:9999; }
        .modal { width:min(920px,92vw); max-height:88vh; overflow:auto; background:#fff; border-radius:10px; padding:16px; box-shadow:0 10px 30px rgba(0,0,0,.2); }
        .modal-title { font-size:16px; font-weight:bold; margin-bottom:8px; }
        .input, textarea, select { width:100%; box-sizing:border-box; padding:8px 10px; border:1px solid #d9dde7; border-radius:6px; }
        textarea { min-height:180px; resize:vertical; }
    </style>
</head>
<body>
<div class="top">
    <div>校园平台 · 管理后台</div>
    <div>
        <span id="adminName" style="margin-right:12px;"></span>
        <button class="btn" onclick="logout()">退出</button>
    </div>
</div>

<div class="main">
    <div class="card">
        <div class="row" style="flex-wrap:wrap;">
            <button id="tabPostList" class="btn tab-btn active" onclick="loadPostList()">待审核帖子</button>
            <button id="tabReportList" class="btn tab-btn" onclick="loadReportList()">待处理举报</button>
            <button id="tabAnnList" class="btn tab-btn" onclick="loadAnnList()">公告管理</button>
            <button id="tabUserList" class="btn tab-btn" onclick="loadUserList()">用户管理</button>
        </div>
        <div id="annBar" class="row" style="display:none;flex-wrap:wrap;gap:10px;">
            <button class="btn btn-primary" onclick="openAnnEditor()">新建公告（自动生效）</button>
            <button class="btn" onclick="loadAnnList()">刷新</button>
        </div>
        <div id="reportFilters" class="row" style="display:none;flex-wrap:wrap;gap:10px;">
            <label>举报状态：
                <select id="reportStatusFilter" class="btn" onchange="loadReportList()">
                    <option value="pending" selected>待处理</option>
                    <option value="processed">已处理</option>
                    <option value="all">全部</option>
                </select>
            </label>
        </div>
        <div id="userFilters" class="row" style="display:none;flex-wrap:wrap;gap:10px;">
            <input id="userKeyword" class="input" style="max-width:280px;" placeholder="搜索昵称 / 手机号 / 用户ID" />
            <select id="userStatusFilter" class="btn" style="max-width:180px;">
                <option value="">全部状态</option>
                <option value="1">正常</option>
                <option value="0">封禁</option>
            </select>
            <button class="btn btn-primary" onclick="loadUserList()">查询</button>
            <button class="btn" onclick="resetUserFilters()">重置</button>
        </div>
        <div id="hint" class="muted"></div>
    </div>

    <div class="card" id="tableWrap"></div>
</div>

<div id="postDetailModalMask" class="modal-mask" onclick="closePostDetailModal(event)">
    <div class="modal" onclick="event.stopPropagation()">
        <div class="modal-title">帖子详情</div>
        <div id="postDetailContent"></div>
        <div class="modal-footer"><button class="btn" onclick="closePostDetailModal()">关闭</button></div>
    </div>
</div>

<div id="userDetailModalMask" class="modal-mask" onclick="closeUserDetailModal(event)">
    <div class="modal" onclick="event.stopPropagation()">
        <div class="modal-title">用户详情</div>
        <div id="userDetailContent"></div>
        <div class="modal-footer"><button class="btn" onclick="closeUserDetailModal()">关闭</button></div>
    </div>
</div>
<div id="userActionModalMask" class="modal-mask" onclick="closeUserActionModal(event)">
    <div class="modal" style="width:min(620px,92vw);" onclick="event.stopPropagation()">
        <div class="modal-title" id="userActionTitle">用户操作</div>
        <input type="hidden" id="userActionId" />
        <input type="hidden" id="userActionType" />
        <div class="row" style="display:block;">
            <label>原因</label>
            <input id="userActionReason" class="input" maxlength="200" placeholder="请输入原因" />
        </div>
        <div class="row" style="display:block;">
            <label>备注</label>
            <textarea id="userActionRemark" maxlength="500" placeholder="请输入备注（可选）"></textarea>
        </div>
        <div class="modal-footer">
            <button class="btn" onclick="closeUserActionModal()">取消</button>
            <button class="btn btn-primary" onclick="submitUserAction()">确认</button>
        </div>
    </div>
</div>
<div id="postPreviewModalMask" class="modal-mask" onclick="closePostPreviewModal(event)">
    <div class="modal" onclick="event.stopPropagation()">
        <div class="modal-title">帖子详情预览</div>
        <div id="postPreviewContent"></div>
        <div class="modal-footer"><button class="btn" onclick="closePostPreviewModal()">关闭</button></div>
    </div>
</div>

<div id="annEditorMask" class="modal-mask" onclick="closeAnnEditor(event)">
    <div class="modal" style="width:min(760px,92vw);" onclick="event.stopPropagation()">
        <div class="modal-title" id="annEditorTitle">新建公告</div>
        <input type="hidden" id="annId" />
        <div class="row" style="display:block;">
            <label>标题</label>
            <input id="annTitle" class="input" maxlength="200" placeholder="请输入公告标题" />
        </div>
        <div class="row" style="display:block;">
            <label>内容</label>
            <textarea id="annContent" maxlength="5000" placeholder="请输入公告内容"></textarea>
        </div>
        <div class="modal-footer">
            <button class="btn" onclick="closeAnnEditor()">取消</button>
            <button class="btn btn-primary" onclick="saveAnn()">保存并生效</button>
        </div>
    </div>
</div>

<script>
    var BASE = '<%=request.getContextPath()%>';
    function getQuery(name){ var m = location.search.match(new RegExp('[?&]' + name + '=([^&]+)')); return m ? decodeURIComponent(m[1]) : ''; }
    function getCookie(name){ var arr = document.cookie ? document.cookie.split('; ') : []; for(var i=0;i<arr.length;i++){ var kv = arr[i].split('='); if(kv[0] === name) return decodeURIComponent(kv.slice(1).join('=')); } return ''; }
    function setCookie(name, value, maxAge){ document.cookie = name + '=' + encodeURIComponent(value) + '; Path=' + BASE + '; Max-Age=' + maxAge; }
    function clearToken(){ localStorage.removeItem('adminToken'); document.cookie = 'adminToken=; Path=' + BASE + '; Max-Age=0'; }
    var token = getQuery('t') || localStorage.getItem('adminToken') || getCookie('adminToken'); if(!token){ window.location.replace(BASE + '/admin/login'); }
    localStorage.setItem('adminToken', token); setCookie('adminToken', token, 7 * 24 * 60 * 60); if(location.search && location.search.indexOf('t=') >= 0){ history.replaceState(null, '', BASE + '/admin/index'); }
    async function api(path, method, body){ var url = BASE + path + (path.indexOf('?') >= 0 ? '&' : '?') + 't=' + encodeURIComponent(token); var res = await fetch(url, { method: method || 'GET', headers: { 'Content-Type': 'application/json', 'token': token }, body: body ? JSON.stringify(body) : undefined, credentials: 'include' }); return res.json(); }
    function esc(str){ return (str === null || str === undefined) ? '' : String(str).replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;').replace(/"/g,'&quot;').replace(/'/g,'&#39;'); }
    function formatTime(val){ if(val === null || val === undefined || val === '') return '-'; var raw = String(val).trim(); if(!raw) return '-'; if(/^\d+$/.test(raw)){ var num = Number(raw); if(raw.length <= 10) num *= 1000; var d = new Date(num); if(isNaN(d.getTime())) return raw; return d.getFullYear() + '-' + pad2(d.getMonth()+1) + '-' + pad2(d.getDate()) + ' ' + pad2(d.getHours()) + ':' + pad2(d.getMinutes()); } return raw; }
    function pad2(n){ return n < 10 ? ('0' + n) : String(n); }
    function setActiveTab(tab){ ['tabPostList','tabReportList','tabAnnList','tabUserList'].forEach(function(id){ var el = document.getElementById(id); if(el) el.classList.remove('active'); }); if(tab === 'post') document.getElementById('tabPostList').classList.add('active'); else if(tab === 'report') document.getElementById('tabReportList').classList.add('active'); else if(tab === 'ann') document.getElementById('tabAnnList').classList.add('active'); else if(tab === 'user') document.getElementById('tabUserList').classList.add('active'); }
    async function init(){ try{ var profile = await api('/api/admin/profile'); if(profile.code !== 200){ document.getElementById('adminName').innerText = '鉴权失败'; document.getElementById('hint').innerHTML = '<span class="err">登录态无效，请重新登录</span>'; return; } document.getElementById('adminName').innerText = profile.data.username || '管理员'; restoreReportFilters(); await loadPostList(); }catch(e){ document.getElementById('adminName').innerText = '鉴权失败'; document.getElementById('hint').innerHTML = '<span class="err">请求异常，请刷新重试</span>'; } }
    var postListCache = []; var reportProcessingMap = {}; var reportGroupExpandedMap = {}; var annListCache = []; var userListCache = []; var reportLastGroups = [];
    async function loadPostList(){ setActiveTab('post'); document.getElementById('reportFilters').style.display = 'none'; document.getElementById('userFilters').style.display = 'none'; document.getElementById('annBar').style.display = 'none'; document.getElementById('tableWrap').innerHTML = ''; document.getElementById('hint').innerText = '正在加载待审核帖子...'; var data = await api('/api/admin/post/list?status=0&page=1&pageSize=50'); if(data.code !== 200){ document.getElementById('hint').innerHTML = '<span class="err">' + esc(data.msg || '加载失败') + '</span>'; return; } postListCache = (data.data && data.data.list) || []; renderPostTable(postListCache); document.getElementById('hint').innerText = '共 ' + postListCache.length + ' 条待审核帖子'; }
    function renderPostTable(list){ if(!list || !list.length){ document.getElementById('tableWrap').innerHTML = '<div class="muted">暂无待审核帖子</div>'; return; } var html = '<table class="table"><thead><tr><th>ID</th><th>标题</th><th>分类</th><th>发布时间</th><th>内容</th><th>操作</th></tr></thead><tbody>'; for(var i=0;i<list.length;i++){ var it = list[i]; var id = Number(it.id); var desc = it.description || ''; var shortDesc = desc.length > 40 ? (desc.substring(0, 40) + '...') : desc; html += '<tr><td>' + esc(it.id) + '</td><td>' + esc(it.title) + '</td><td>' + esc(it.category) + '</td><td>' + esc(it.createTime) + '</td><td>' + esc(shortDesc) + '</td><td><button class="btn btn-primary" onclick="approve(' + id + ')">通过</button> <button class="btn" onclick="reject(' + id + ')">拒绝</button> <button class="btn" onclick="delPost(' + id + ')">删除</button></td></tr>'; } html += '</tbody></table>'; document.getElementById('tableWrap').innerHTML = html; }
    async function loadReportList(){ setActiveTab('report'); document.getElementById('reportFilters').style.display = 'flex'; document.getElementById('userFilters').style.display = 'none'; document.getElementById('annBar').style.display = 'none'; document.getElementById('tableWrap').innerHTML = ''; saveReportFilters(); document.getElementById('hint').innerText = '正在加载举报列表...'; var reportStatusVal = document.getElementById('reportStatusFilter').value; var qs = ['page=1','pageSize=50']; if(reportStatusVal === 'pending'){ qs.push('status=0'); } var data = await api('/api/admin/post/reportList?' + qs.join('&')); if(data.code !== 200){ document.getElementById('hint').innerHTML = '<span class="err">' + esc(data.msg || '加载失败') + '</span>'; return; } var list = (data.data && data.data.list) || []; var groups = groupReportList(list); if(reportStatusVal === 'pending'){ groups = groups.filter(function(group){ return group.pendingCount > 0; }); }else if(reportStatusVal === 'processed'){ groups = groups.filter(function(group){ return group.pendingCount === 0 && group.processedCount > 0; }); } reportLastGroups = groups; renderReportTable(groups); document.getElementById('hint').innerText = '共 ' + list.length + ' 条举报记录'; }
    function saveReportFilters(){ try{ localStorage.setItem('adminReportFilters', JSON.stringify({ reportStatus: document.getElementById('reportStatusFilter').value })); }catch(e){} }
    function restoreReportFilters(){ try{ var text = localStorage.getItem('adminReportFilters'); if(!text) return; var parsed = JSON.parse(text); if(parsed && parsed.reportStatus !== undefined){ document.getElementById('reportStatusFilter').value = parsed.reportStatus; } }catch(e){} }
    function resetReportFilters(){ document.getElementById('reportStatusFilter').value = 'pending'; saveReportFilters(); loadReportList(); }
    function groupReportList(list){ var map = {}; for(var i=0;i<list.length;i++){ var it = list[i] || {}; var postId = String(it.postId || '-'); if(!map[postId]){ map[postId] = { postId: it.postId, postTitle: it.postTitle, postDescription: it.postDescription, postStatus: it.postStatus, reports: [], pendingCount: 0, processedCount: 0 }; } map[postId].reports.push(it); if(Number(it.status) === 0){ map[postId].pendingCount += 1; }else{ map[postId].processedCount += 1; } } var groups = []; for(var k in map){ if(Object.prototype.hasOwnProperty.call(map, k)) groups.push(map[k]); } groups.sort(function(a, b){ var at = a.reports && a.reports.length ? String(a.reports[0].createdAt || '') : ''; var bt = b.reports && b.reports.length ? String(b.reports[0].createdAt || '') : ''; return bt.localeCompare(at); }); return groups; }
    function renderReportTable(groups){ if(!groups || !groups.length){ document.getElementById('tableWrap').innerHTML = '<div class="muted">暂无举报记录</div>'; return; } var html = ''; for(var i=0;i<groups.length;i++){ var group = groups[i] || {}; var postId = Number(group.postId || 0); var latestReport = group.reports && group.reports.length ? group.reports[0] : {}; var postPreview = (group.postTitle || ('帖子#' + (group.postId || '-'))) + ' / ' + (group.postDescription ? String(group.postDescription).substring(0, 20) : '-'); var actionHtml = '<button class="btn" onclick="viewPostDetailByReport(' + Number(latestReport.id || 0) + ')">查看帖子</button> '; if(group.pendingCount > 0){ actionHtml += ' <button class="btn btn-primary" onclick="handleReportGroup(' + postId + ',\'违规，下架\')">违规，下架</button>'; actionHtml += ' <button class="btn" onclick="handleReportGroup(' + postId + ',\'不违规，驳回\')">不违规，驳回</button>'; }else{ actionHtml += ' <span class="muted">已处理</span>'; } html += '<div class="card" style="margin-bottom:12px;border:1px solid #eef1f6;"><div class="row" style="justify-content:space-between;align-items:flex-start;"><div><div style="font-weight:700;font-size:15px;">' + esc(postPreview) + '</div><div class="muted" style="margin-top:4px;">帖子ID：' + esc(group.postId) + ' ｜ 举报总数：' + esc((group.reports || []).length) + ' ｜ 待处理：' + esc(group.pendingCount) + ' ｜ 已处理：' + esc(group.processedCount) + '</div></div><div>' + actionHtml + '</div></div><div style="margin-top:10px;"><table class="table"><thead><tr><th>ID</th><th>举报人</th><th>举报原因</th><th>补充描述</th><th>举报时间</th><th>状态</th><th>处理信息</th><th>操作</th></tr></thead><tbody>'; for(var j=0;j<(group.reports || []).length;j++){ var it = group.reports[j] || {}; var reportId = Number(it.id); var status = Number(it.status); var handledInfo = '-'; if(status !== 0){ var adminName = it.handledAdminName ? String(it.handledAdminName) : ('管理员#' + (it.handledBy || '-')); handledInfo = adminName + ' / ' + formatTime(it.handledAt) + (it.adminRemark ? (' / 备注:' + it.adminRemark) : ''); } html += '<tr><td>' + esc(reportId) + '</td><td>' + esc((it.reporterNickname || '用户') + '(' + (it.reporterId || '-') + ')') + '</td><td>' + esc(reasonTypeText(it.reasonType)) + '</td><td>' + esc(it.reasonDesc || '-') + '</td><td>' + esc(formatTime(it.createdAt)) + '</td><td>' + esc(reportHandleStatusText(status)) + '</td><td>' + esc(handledInfo) + '</td><td><button class="btn" onclick="viewPostDetailByReport(' + reportId + ')">查看帖子</button></td></tr>'; } html += '</tbody></table></div></div>'; } document.getElementById('tableWrap').innerHTML = html; }
    function reportHandleStatusText(status){ if(status === 0) return '待处理'; if(status === 1) return '举报成立'; if(status === 2) return '举报驳回'; return '未知'; }
    function reasonTypeText(v){ if(Number(v) === 1) return '违规内容'; if(Number(v) === 2) return '广告骚扰'; if(Number(v) === 3) return '色情低俗'; if(Number(v) === 4) return '其他'; return '-'; }
    async function handleReportGroup(postId, action){ var adminRemark = prompt('请输入处理备注（可空）', '') || ''; var data = await api('/api/admin/post/handleReportByPost/' + postId, 'POST', { action: action, adminRemark: adminRemark }); alert(data.msg || (data.code===200 ? '处理成功' : '处理失败')); loadReportList(); }
    async function loadAnnList(){ setActiveTab('ann'); document.getElementById('reportFilters').style.display = 'none'; document.getElementById('userFilters').style.display = 'none'; document.getElementById('annBar').style.display = 'flex'; document.getElementById('tableWrap').innerHTML = ''; document.getElementById('hint').innerText = '正在加载公告...'; var data = await api('/api/admin/announcement/listAdmin?page=1&pageSize=50'); if(data.code !== 200){ document.getElementById('hint').innerHTML = '<span class="err">' + esc(data.msg || '加载失败') + '</span>'; return; } annListCache = (data.data && data.data.list) || []; renderAnnTable(annListCache); document.getElementById('hint').innerText = '共 ' + annListCache.length + ' 条公告'; }
    function renderAnnTable(list){ if(!list || !list.length){ document.getElementById('tableWrap').innerHTML = '<div class="muted">暂无公告</div>'; return; } var html = '<table class="table"><thead><tr><th>ID</th><th>标题</th><th>状态</th><th>创建时间</th><th>操作</th></tr></thead><tbody>'; for(var i=0;i<list.length;i++){ var it = list[i] || {}; var isActive = Number(it.status) === 1; var badge = isActive ? '<span style="background:#e6f4ea;color:#1a7f4b;padding:2px 10px;border-radius:99px;font-size:12px;font-weight:600;">生效中</span>' : '<span style="background:#f0f0f0;color:#888;padding:2px 10px;border-radius:99px;font-size:12px;">已过期</span>'; var ops = ''; if(!isActive){ ops += '<button class="btn btn-primary" onclick="enableAnn(' + Number(it.id) + ')">启用</button> '; } ops += '<button class="btn" onclick="delAnn(' + Number(it.id) + ')">删除</button>'; html += '<tr><td>' + esc(it.id) + '</td><td>' + esc(it.title) + '</td><td>' + badge + '</td><td>' + esc(formatTime(it.createdAt)) + '</td><td>' + ops + '</td></tr>'; } html += '</tbody></table>'; document.getElementById('tableWrap').innerHTML = html; }
    function openAnnEditor(){ document.getElementById('annId').value=''; document.getElementById('annTitle').value=''; document.getElementById('annContent').value=''; document.getElementById('annEditorTitle').innerText='新建公告'; document.getElementById('annEditorMask').style.display='flex'; }
    function closeAnnEditor(){ document.getElementById('annEditorMask').style.display='none'; }
    async function saveAnn(){ var id = document.getElementById('annId').value; var payload = { title: document.getElementById('annTitle').value.trim(), content: document.getElementById('annContent').value.trim(), status: 1, isPinned: 1 }; if(!payload.title || !payload.content){ alert('请填写标题和内容'); return; } var path = id ? '/api/admin/announcement/update/' + id : '/api/admin/announcement/save'; var data = await api(path, 'POST', payload); alert(data.msg || (data.code===200 ? '保存成功，已自动生效' : '保存失败')); closeAnnEditor(); loadAnnList(); }
    async function delAnn(id){ if(!confirm('确认删除该公告吗？')) return; var data = await api('/api/admin/announcement/delete/' + id, 'DELETE'); alert(data.msg || (data.code===200 ? '删除成功' : '删除失败')); loadAnnList(); }
    async function enableAnn(id){ if(!confirm('启用此公告后，其他公告将变为已过期状态，确认启用？')) return; var data = await api('/api/admin/announcement/enable/' + id, 'POST'); alert(data.msg || (data.code===200 ? '已启用' : '操作失败')); loadAnnList(); }
    function resetUserFilters(){ document.getElementById('userKeyword').value=''; document.getElementById('userStatusFilter').value=''; loadUserList(); }
    async function loadUserList(){ setActiveTab('user'); document.getElementById('reportFilters').style.display='none'; document.getElementById('annBar').style.display='none'; document.getElementById('userFilters').style.display='flex'; document.getElementById('tableWrap').innerHTML=''; document.getElementById('hint').innerText='正在加载用户...'; var keyword = document.getElementById('userKeyword').value.trim(); var status = document.getElementById('userStatusFilter').value; var qs = ['page=1','pageSize=50']; if(keyword) qs.push('keyword=' + encodeURIComponent(keyword)); if(status !== '') qs.push('status=' + encodeURIComponent(status)); var data = await api('/api/admin/user/list?' + qs.join('&')); if(data.code !== 200){ document.getElementById('hint').innerHTML = '<span class="err">' + esc(data.msg || '加载失败') + '</span>'; return; } userListCache = (data.data && data.data.list) || []; renderUserTable(userListCache); document.getElementById('hint').innerText = '共 ' + userListCache.length + ' 个用户'; }
    function renderUserTable(list){ if(!list || !list.length){ document.getElementById('tableWrap').innerHTML = '<div class="muted">暂无用户</div>'; return; } var html = '<table class="table"><thead><tr><th>ID</th><th>昵称</th><th>手机号</th><th>注册时间</th><th>最近活跃</th><th>状态</th><th>操作</th></tr></thead><tbody>'; for(var i=0;i<list.length;i++){ var it = list[i] || {}; var status = Number(it.status) === 0 ? '封禁' : '正常'; var statusBadge = '<span style="padding:2px 10px;border-radius:99px;background:' + (status === '正常' ? '#f0f5ff' : '#fff1f0') + ';color:' + (status === '正常' ? '#1677ff' : '#cf1322') + ';font-size:12px;">' + status + '</span>'; html += '<tr><td>' + esc(it.id) + '</td><td>' + esc(it.nickname || '-') + '</td><td>' + esc(it.phone || '-') + '</td><td>' + esc(formatDateOnly(it.createTime)) + '</td><td>' + esc(formatTime(it.lastActiveTime)) + '</td><td>' + statusBadge + '</td><td><button class="btn btn-primary" onclick="openUserDetail(' + Number(it.id) + ')">详情</button> <button class="btn" onclick="toggleUserStatus(' + Number(it.id) + ',' + (it.status != null ? Number(it.status) : 1) + ')">' + (Number(it.status) === 0 ? '解封' : '封禁') + '</button></td></tr>'; } html += '</tbody></table>'; document.getElementById('tableWrap').innerHTML = html; }
    function formatDateOnly(val){ if(val === null || val === undefined || val === '') return '-'; var raw = String(val).trim(); if(!raw) return '-'; if(/^\d+$/.test(raw)){ var num = Number(raw); if(raw.length <= 10) num *= 1000; var d = new Date(num); if(isNaN(d.getTime())) return raw; return d.getFullYear() + '-' + pad2(d.getMonth()+1) + '-' + pad2(d.getDate()); } return raw.split(' ')[0]; }
    async function openUserDetail(id){ var data = await api('/api/admin/user/' + id); if(data.code !== 200){ alert(data.msg || '加载失败'); return; } var d = data.data || {}; var html = '<div class="card" style="box-shadow:none;border:1px solid #eef1f6;margin-bottom:12px;"><div class="kv"><b>昵称：</b>' + esc(d.nickname || '-') + '</div><div class="kv"><b>手机号：</b>' + esc(d.phone || '-') + '</div><div class="kv"><b>注册日期：</b>' + esc(formatDateOnly(d.createTime)) + '</div><div class="kv"><b>最近活跃：</b>' + esc(formatTime(d.lastActiveTime)) + '</div><div class="kv"><b>状态：</b>' + (Number(d.status) === 0 ? '封禁' : '正常') + '</div></div>' + '<div class="card" style="box-shadow:none;border:1px solid #eef1f6;margin-bottom:12px;"><div class="modal-title" style="margin-bottom:10px;">发帖</div>' + renderUserPostList(d.posts || []) + '</div>' + '<div class="card" style="box-shadow:none;border:1px solid #eef1f6;margin-bottom:12px;"><div class="modal-title" style="margin-bottom:10px;">评论</div>' + renderUserCommentList(d.comments || []) + '</div>' + '<div class="card" style="box-shadow:none;border:1px solid #eef1f6;margin-bottom:12px;"><div class="modal-title" style="margin-bottom:10px;">收藏</div>' + renderUserCollectList(d.collects || []) + '</div>'; document.getElementById('userDetailContent').innerHTML = html; document.getElementById('userDetailModalMask').style.display='flex'; }
    function renderUserPostList(list){ if(!list || !list.length) return '<div class="muted">暂无发帖</div>'; var html = '<table class="table"><thead><tr><th>ID</th><th>标题</th><th>分类</th><th>状态</th><th>操作</th></tr></thead><tbody>'; for(var i=0;i<list.length;i++){ var it = list[i] || {}; html += '<tr><td>' + esc(it.id) + '</td><td>' + esc(it.title) + '</td><td>' + esc(it.category || '-') + '</td><td>' + esc(it.status) + '</td><td><button class="btn btn-primary" onclick="previewPost(' + Number(it.id) + ')">预览</button></td></tr>'; } return html + '</tbody></table>'; }
    function renderUserCommentList(list){ if(!list || !list.length) return '<div class="muted">暂无评论</div>'; var html = '<table class="table"><thead><tr><th>ID</th><th>帖子ID</th><th>内容</th><th>时间</th><th>操作</th></tr></thead><tbody>'; for(var i=0;i<list.length;i++){ var it = list[i] || {}; var pid = Number(it.postId || 0); html += '<tr><td>' + esc(it.id) + '</td><td>' + esc(it.postId) + '</td><td>' + esc(it.content) + '</td><td>' + esc(formatTime(it.createTime)) + '</td><td>' + (pid > 0 ? '<button class="btn btn-primary" onclick="previewCommentPost(' + pid + ')">看帖子</button>' : '<span class="muted">无关联帖子</span>') + '</td></tr>'; } return html + '</tbody></table>'; }
    function renderUserCollectList(list){ if(!list || !list.length) return '<div class="muted">暂无收藏</div>'; var html = '<table class="table"><thead><tr><th>ID</th><th>标题</th><th>分类</th><th>状态</th><th>操作</th></tr></thead><tbody>'; for(var i=0;i<list.length;i++){ var it = list[i] || {}; html += '<tr><td>' + esc(it.id) + '</td><td>' + esc(it.title) + '</td><td>' + esc(it.category || '-') + '</td><td>' + esc(it.status) + '</td><td><button class="btn" onclick="previewPost(' + Number(it.id) + ')">预览</button></td></tr>'; } return html + '</tbody></table>'; }
    async function previewPost(id){ var data = await api('/api/admin/post/detail/' + id); if(data.code !== 200){ alert(data.msg || '加载失败'); return; } var it = data.data || {}; var imgs = parseImages(it.images); var imageHtml = imgs.length ? '<div class="row" style="gap:10px;flex-wrap:wrap;">' + imgs.map(function(src){ return '<img src="' + esc(src) + '" style="width:120px;height:120px;object-fit:cover;border-radius:10px;border:1px solid #e5e9f2;" />'; }).join('') + '</div>' : '<div class="muted">暂无图片</div>'; var html = '<div class="kv"><b>标题：</b>' + esc(it.title || '-') + '</div><div class="kv"><b>分类：</b>' + esc(it.category || '-') + '</div><div class="kv"><b>状态：</b>' + esc(it.status) + '</div><div class="kv"><b>内容：</b></div><div style="white-space:pre-wrap;line-height:1.7;">' + esc(it.description || '-') + '</div><div style="margin-top:12px;"><b>图片：</b></div>' + imageHtml; document.getElementById('postPreviewContent').innerHTML = html; document.getElementById('postPreviewModalMask').style.display='flex'; }
    async function previewCommentPost(postId){ await previewPost(postId); }
    function closeUserDetailModal(){ document.getElementById('userDetailModalMask').style.display='none'; }
    function closePostPreviewModal(){ document.getElementById('postPreviewModalMask').style.display='none'; }
    function closePostDetailModal(){ document.getElementById('postDetailModalMask').style.display='none'; }
    async function approve(id){ var data = await api('/api/admin/post/approve/' + id, 'POST'); alert(data.msg || (data.code===200 ? '操作成功' : '操作失败')); loadPostList(); }
    async function reject(id){ var reason = prompt('请输入拒绝原因', '内容不规范'); if(!reason) return; var data = await api('/api/admin/post/reject/' + id, 'POST', { reason: reason }); alert(data.msg || (data.code===200 ? '操作成功' : '操作失败')); loadPostList(); }
    async function delPost(id){ if(!confirm('确认删除该帖子吗？')) return; var data = await api('/api/admin/post/delete/' + id, 'DELETE'); alert(data.msg || (data.code===200 ? '删除成功' : '删除失败')); loadPostList(); }
    function parseImages(images){ if(!images) return []; if(Array.isArray(images)) return images; var text = String(images).trim(); if(!text) return []; try{ var arr = JSON.parse(text); return Array.isArray(arr) ? arr : []; }catch(e){ return text.split(',').map(function(s){ return s.trim(); }).filter(Boolean); } }
    async function toggleUserStatus(id, currentStatus){ var target = Number(currentStatus) === 0 ? 1 : 0; var reason = target === 0 ? prompt('请输入封禁原因', '违规操作') : prompt('请输入解封备注', '已处理'); if(reason === null) return; var data = await api('/api/admin/user/status/' + id, 'POST', { status: target, reason: reason, remark: reason }); alert(data.msg || (data.code===200 ? '操作成功' : '操作失败')); loadUserList(); }
    function viewPostDetailByReport(reportId){ alert('请在帖子管理里查看该帖子详情'); }
    function logout(){ clearToken(); window.location.replace(BASE + '/admin/login'); }
    init();
</script>
</body>
</html>
