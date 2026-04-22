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
        .kv { margin-bottom:6px; font-size:14px; }
        .post-desc { white-space:pre-wrap; background:#f7f9fd; border:1px solid #eef1f6; border-radius:8px; padding:10px; margin-top:8px; }
        .img-grid { display:grid; grid-template-columns:repeat(auto-fill,minmax(180px,1fr)); gap:10px; margin-top:10px; }
        .img-grid img { width:100%; height:140px; object-fit:cover; border-radius:8px; border:1px solid #e5e9f2; background:#f8f9fb; }
        .modal-footer { margin-top:12px; text-align:right; }
        .input, textarea, select { width:100%; box-sizing:border-box; padding:8px 10px; border:1px solid #d9dde7; border-radius:6px; }
        textarea { min-height:180px; resize:vertical; }
        .full { width:100%; }
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
        <div class="row">
            <button id="tabPostList" class="btn tab-btn active" onclick="loadPostList()">待审核帖子</button>
            <button id="tabReportList" class="btn tab-btn" onclick="loadReportList()">待处理举报</button>
            <button id="tabAnnList" class="btn tab-btn" onclick="loadAnnList()">公告管理</button>
        </div>
        <div id="annBar" class="row" style="display:none;flex-wrap:wrap;gap:10px;">
            <button class="btn btn-primary" onclick="openAnnEditor()">新建公告（自动生效）</button>
            <button class="btn" onclick="loadAnnList()">刷新</button>
        </div>
        <div id="reportFilters" class="row" style="display:none;">
            <label>举报状态：
                <select id="reportStatusFilter" class="btn" onchange="loadReportList()">
                    <option value="pending" selected>待处理</option>
                    <option value="processed">已处理</option>
                    <option value="all">全部</option>
                </select>
            </label>
        </div>
        <div id="hint" class="muted"></div>
    </div>

    <div class="card" id="tableWrap"></div>
</div>

<div id="postDetailModalMask" class="modal-mask" onclick="closePostDetailModal(event)">
    <div class="modal" onclick="event.stopPropagation()">
        <div class="modal-title">帖子详情</div>
        <div id="postDetailContent"></div>
        <div class="modal-footer">
            <button class="btn" onclick="closePostDetailModal()">关闭</button>
        </div>
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

    function getQuery(name){
        var m = location.search.match(new RegExp('[?&]' + name + '=([^&]+)'));
        return m ? decodeURIComponent(m[1]) : '';
    }

    function getCookie(name){
        var arr = document.cookie ? document.cookie.split('; ') : [];
        for(var i=0;i<arr.length;i++){
            var kv = arr[i].split('=');
            if(kv[0] === name) return decodeURIComponent(kv.slice(1).join('='));
        }
        return '';
    }

    function setCookie(name, value, maxAge){
        document.cookie = name + '=' + encodeURIComponent(value) + '; Path=' + BASE + '; Max-Age=' + maxAge;
    }

    function clearToken(){
        localStorage.removeItem('adminToken');
        document.cookie = 'adminToken=; Path=' + BASE + '; Max-Age=0';
    }

    var token = getQuery('t') || localStorage.getItem('adminToken') || getCookie('adminToken');
    if(!token){
        window.location.replace(BASE + '/admin/login');
    }

    // 统一落盘，后续刷新不丢
    localStorage.setItem('adminToken', token);
    setCookie('adminToken', token, 7 * 24 * 60 * 60);

    // 清理 URL token，避免泄露
    if(location.search && location.search.indexOf('t=') >= 0){
        history.replaceState(null, '', BASE + '/admin/index');
    }

    async function api(path, method, body){
        var url = BASE + path + (path.indexOf('?') >= 0 ? '&' : '?') + 't=' + encodeURIComponent(token);
        var res = await fetch(url, {
            method: method || 'GET',
            headers: {
                'Content-Type': 'application/json',
                'token': token
            },
            body: body ? JSON.stringify(body) : undefined,
            credentials: 'include'
        });
        return res.json();
    }

    function esc(str){
        return (str === null || str === undefined) ? '' : String(str)
            .replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;')
            .replace(/"/g,'&quot;').replace(/'/g,'&#39;');
    }

    async function init(){
        try{
            var profile = await api('/api/admin/profile');
            if(profile.code !== 200){
                document.getElementById('adminName').innerText = '鉴权失败';
                document.getElementById('hint').innerHTML = '<span class="err">登录态无效，请重新登录</span>';
                return;
            }
            document.getElementById('adminName').innerText = profile.data.username || '管理员';
            restoreReportFilters();
            await loadPostList();
        }catch(e){
            document.getElementById('adminName').innerText = '鉴权失败';
            document.getElementById('hint').innerHTML = '<span class="err">请求异常，请刷新重试</span>';
        }
    }

    var postListCache = [];
    var reportProcessingMap = {};
    var reportGroupExpandedMap = {};
    var annListCache = [];

    function setActiveTab(tab){
        var postBtn = document.getElementById('tabPostList');
        var reportBtn = document.getElementById('tabReportList');
        var annBtn = document.getElementById('tabAnnList');
        if(!postBtn || !reportBtn || !annBtn) return;
        postBtn.classList.remove('active');
        reportBtn.classList.remove('active');
        annBtn.classList.remove('active');
        if(tab === 'post') postBtn.classList.add('active');
        else if(tab === 'report') reportBtn.classList.add('active');
        else if(tab === 'ann') annBtn.classList.add('active');
    }

    async function loadPostList(){
        setActiveTab('post');
        document.getElementById('reportFilters').style.display = 'none';
        document.getElementById('annBar').style.display = 'none';
        document.getElementById('tableWrap').innerHTML = '';
        document.getElementById('hint').innerText = '正在加载待审核帖子...';
        var data = await api('/api/admin/post/list?status=0&page=1&pageSize=50');
        if(data.code !== 200){
            document.getElementById('hint').innerHTML = '<span class="err">' + esc(data.msg || '加载失败') + '</span>';
            return;
        }
        postListCache = (data.data && data.data.list) || [];
        renderPostTable(postListCache);
        document.getElementById('hint').innerText = '共 ' + postListCache.length + ' 条待审核帖子';
    }

    var expandedPostIds = {};

    function renderPostTable(list){
        var html = '<table class="table"><thead><tr><th>ID</th><th>标题</th><th>分类</th><th>发布时间</th><th>内容</th><th>操作</th></tr></thead><tbody>';
        for(var i=0;i<list.length;i++){
            var it = list[i];
            var id = Number(it.id);
            var desc = it.description || '';
            var isExpanded = !!expandedPostIds[id];
            var shortDesc = desc.length > 40 ? (desc.substring(0, 40) + '...') : desc;
            var showText = isExpanded ? desc : shortDesc;
            var toggleText = isExpanded ? '收起' : '展开';

            html += '<tr>'
                + '<td>' + esc(it.id) + '</td>'
                + '<td>' + esc(it.title) + '</td>'
                + '<td>' + esc(it.category) + '</td>'
                + '<td>' + esc(it.createTime) + '</td>'
                + '<td>' + esc(showText) + ' <button class="btn" onclick="togglePostDesc(' + id + ')">' + toggleText + '</button></td>'
                + '<td>'
                + '<button class="btn btn-primary" onclick="approve(' + id + ')">通过</button> '
                + '<button class="btn" onclick="reject(' + id + ')">拒绝</button> '
                + '<button class="btn" onclick="delPost(' + id + ')">删除</button>'
                + '</td></tr>';
        }
        html += '</tbody></table>';
        document.getElementById('tableWrap').innerHTML = html;
    }

    function togglePostDesc(id){
        expandedPostIds[id] = !expandedPostIds[id];
        renderPostTable(postListCache || []);
    }

    async function approve(id){
        var data = await api('/api/admin/post/approve/' + id, 'POST');
        alert(data.msg || (data.code===200 ? '操作成功' : '操作失败'));
        loadPostList();
    }

    async function reject(id){
        var reason = prompt('请输入拒绝原因', '内容不规范');
        if(!reason) return;
        var data = await api('/api/admin/post/reject/' + id, 'POST', { reason: reason });
        alert(data.msg || (data.code===200 ? '操作成功' : '操作失败'));
        loadPostList();
    }

    async function delPost(id){
        if(!confirm('确认删除该帖子吗？')) return;
        var data = await api('/api/admin/post/delete/' + id, 'DELETE');
        alert(data.msg || (data.code===200 ? '删除成功' : '删除失败'));
        loadPostList();
    }

    function saveReportFilters(){
        try{
            var reportStatus = document.getElementById('reportStatusFilter').value;
            localStorage.setItem('adminReportFilters', JSON.stringify({ reportStatus: reportStatus }));
        }catch(e){}
    }

    function restoreReportFilters(){
        try{
            var text = localStorage.getItem('adminReportFilters');
            if(!text) return;
            var parsed = JSON.parse(text);
            if(parsed && parsed.reportStatus !== undefined){
                document.getElementById('reportStatusFilter').value = parsed.reportStatus;
            }
        }catch(e){}
    }

    function resetReportFilters(){
        document.getElementById('reportStatusFilter').value = 'pending';
        saveReportFilters();
        loadReportList();
    }

    async function loadReportList(){
        setActiveTab('report');
        document.getElementById('reportFilters').style.display = 'flex';
        document.getElementById('annBar').style.display = 'none';
        document.getElementById('tableWrap').innerHTML = '';
        saveReportFilters();
        document.getElementById('hint').innerText = '正在加载举报列表...';

        var reportStatusVal = document.getElementById('reportStatusFilter').value;
        var qs = ['page=1', 'pageSize=50'];
        if(reportStatusVal === 'pending'){
            qs.push('status=0');
        }

        var data = await api('/api/admin/post/reportList?' + qs.join('&'));
        if(data.code !== 200){
            document.getElementById('hint').innerHTML = '<span class="err">' + esc(data.msg || '加载失败') + '</span>';
            return;
        }
        var list = (data.data && data.data.list) || [];
        var groups = groupReportList(list);
        if(reportStatusVal === 'pending'){
            groups = groups.filter(function(group){ return group.pendingCount > 0; });
        }else if(reportStatusVal === 'processed'){
            groups = groups.filter(function(group){ return group.pendingCount === 0 && group.processedCount > 0; });
        }
        renderReportTable(groups);
        document.getElementById('hint').innerText = '共 ' + list.length + ' 条举报记录';
    }

    function postStatusText(status){
        if(status === 0) return '正常';
        if(status === 1) return '审核通过';
        if(status === 2) return '审核拒绝';
        if(status === 3) return '已下架';
        return '未知';
    }

    function reportHandleStatusText(status){
        if(status === 0) return '待处理';
        if(status === 1) return '举报成立';
        if(status === 2) return '举报驳回';
        return '未知(' + esc(status) + ')';
    }

    function reportGroupStatusText(group){
        if(group.pendingCount > 0 && group.processedCount > 0) return '全部';
        if(group.pendingCount > 0) return '待处理';
        if(group.processedCount > 0) return '已处理';
        return '全部';
    }

    function groupReportList(list){
        var map = {};
        for(var i=0;i<list.length;i++){
            var it = list[i] || {};
            var postId = String(it.postId || '-');
            if(!map[postId]){
                map[postId] = {
                    postId: it.postId,
                    postTitle: it.postTitle,
                    postDescription: it.postDescription,
                    postStatus: it.postStatus,
                    reports: [],
                    pendingCount: 0,
                    processedCount: 0
                };
            }
            map[postId].reports.push(it);
            if(Number(it.status) === 0){
                map[postId].pendingCount += 1;
            }else{
                map[postId].processedCount += 1;
            }
        }
        var groups = [];
        for(var k in map){
            if(Object.prototype.hasOwnProperty.call(map, k)){
                groups.push(map[k]);
            }
        }
        groups.sort(function(a, b){
            var at = a.reports && a.reports.length ? String(a.reports[0].createdAt || '') : '';
            var bt = b.reports && b.reports.length ? String(b.reports[0].createdAt || '') : '';
            return bt.localeCompare(at);
        });
        return groups;
    }

    function formatTime(val){
        if(val === null || val === undefined || val === '') return '-';
        var raw = String(val).trim();
        if(!raw) return '-';

        if(/^\d+$/.test(raw)){
            var num = Number(raw);
            if(raw.length <= 10){
                num = num * 1000;
            }
            if(!Number.isFinite(num)) return raw;
            var d = new Date(num);
            if(isNaN(d.getTime())) return raw;
            return d.getFullYear() + '-' + pad2(d.getMonth()+1) + '-' + pad2(d.getDate())
                + ' ' + pad2(d.getHours()) + ':' + pad2(d.getMinutes()) + ':' + pad2(d.getSeconds());
        }

        return raw;
    }

    async function loadAnnList(){
        setActiveTab('ann');
        document.getElementById('reportFilters').style.display = 'none';
        document.getElementById('annBar').style.display = 'flex';
        document.getElementById('tableWrap').innerHTML = '';
        document.getElementById('hint').innerText = '正在加载公告...';
        var data = await api('/api/admin/announcement/listAdmin?page=1&pageSize=50');
        if(data.code !== 200){
            document.getElementById('hint').innerHTML = '<span class="err">' + esc(data.msg || '加载失败') + '</span>';
            return;
        }
        annListCache = (data.data && data.data.list) || [];
        renderAnnTable(annListCache);
        document.getElementById('hint').innerText = '共 ' + annListCache.length + ' 条公告';
    }

    function renderAnnTable(list){
        if(!list || !list.length){
            document.getElementById('tableWrap').innerHTML = '<div class="muted">暂无公告</div>';
            return;
        }
        var html = '<table class="table"><thead><tr><th>ID</th><th>标题</th><th>状态</th><th>创建人</th><th>创建时间</th><th>操作</th></tr></thead><tbody>';
        for(var i=0;i<list.length;i++){
            var it = list[i] || {};
            var isActive = Number(it.status) === 1;
            var badge = isActive
                ? '<span style="background:#e6f4ea;color:#1a7f4b;padding:2px 10px;border-radius:99px;font-size:12px;font-weight:600;">生效中</span>'
                : '<span style="background:#f0f0f0;color:#888;padding:2px 10px;border-radius:99px;font-size:12px;">已过期</span>';
            var ops = '';
            if(!isActive){
                ops += '<button class="btn btn-primary" onclick="enableAnn(' + Number(it.id) + ')">启用</button> ';
            }
            ops += '<button class="btn" onclick="delAnn(' + Number(it.id) + ')">删除</button>';
            html += '<tr>'
                + '<td>' + esc(it.id) + '</td>'
                + '<td>' + esc(it.title) + '</td>'
                + '<td>' + badge + '</td>'
                + '<td>' + esc(it.createdByName || '-') + '</td>'
                + '<td>' + esc(formatTime(it.createdAt)) + '</td>'
                + '<td>' + ops + '</td></tr>';
        }
        html += '</tbody></table>';
        document.getElementById('tableWrap').innerHTML = html;
    }

    function openAnnEditor(){
        document.getElementById('annId').value = '';
        document.getElementById('annTitle').value = '';
        document.getElementById('annContent').value = '';
        document.getElementById('annEditorTitle').innerText = '新建公告';
        document.getElementById('annEditorMask').style.display = 'flex';
    }

    function closeAnnEditor(){
        document.getElementById('annEditorMask').style.display = 'none';
    }

    async function saveAnn(){
        var id = document.getElementById('annId').value;
        var payload = {
            title: document.getElementById('annTitle').value.trim(),
            content: document.getElementById('annContent').value.trim(),
            status: 1,
            isPinned: 1
        };
        if(!payload.title || !payload.content){ alert('请填写标题和内容'); return; }
        var path = id ? '/api/admin/announcement/update/' + id : '/api/admin/announcement/save';
        var data = await api(path, 'POST', payload);
        alert(data.msg || (data.code===200 ? '保存成功，已自动生效' : '保存失败'));
        closeAnnEditor();
        loadAnnList();
    }

    async function delAnn(id){
        if(!confirm('确认删除该公告吗？')) return;
        var data = await api('/api/admin/announcement/delete/' + id, 'DELETE');
        alert(data.msg || (data.code===200 ? '删除成功' : '删除失败'));
        loadAnnList();
    }

    async function enableAnn(id){
        if(!confirm('启用此公告后，其他公告将变为已过期状态，确认启用？')) return;
        var data = await api('/api/admin/announcement/enable/' + id, 'POST');
        alert(data.msg || (data.code===200 ? '已启用' : '操作失败'));
        loadAnnList();
    }

    function pad2(n){
        return n < 10 ? ('0' + n) : String(n);
    }

    function reasonTypeText(v){
        if(Number(v) === 1) return '违规内容';
        if(Number(v) === 2) return '广告骚扰';
        if(Number(v) === 3) return '色情低俗';
        if(Number(v) === 4) return '其他';
        return '-';
    }

    var reportGroupExpandedMap = {};

    function toggleReportGroup(groupKey){
        reportGroupExpandedMap[groupKey] = !reportGroupExpandedMap[groupKey];
        renderReportTable(reportLastGroups || []);
    }

    var reportLastGroups = [];

    function renderReportTable(groups){
        if(!groups || groups.length === 0){
            document.getElementById('tableWrap').innerHTML = '<div class="muted">暂无举报记录</div>';
            return;
        }

        reportLastGroups = groups || [];
        var html = '';
        for(var i=0;i<groups.length;i++){
            var group = groups[i] || {};
            var postId = Number(group.postId || 0);
            var groupKey = 'post_' + postId;
            var expanded = !!reportGroupExpandedMap[groupKey];
            var latestReport = group.reports && group.reports.length ? group.reports[0] : {};
            var postPreview = (group.postTitle || ('帖子#' + (group.postId || '-'))) + ' / ' + (group.postDescription ? String(group.postDescription).substring(0, 20) : '-');
            var actionHtml = '<button class="btn" onclick="viewPostDetailByReport(' + Number(latestReport.id || 0) + ')">查看帖子</button> ';
            if(group.pendingCount > 0){
                actionHtml += ' <button class="btn btn-primary" onclick="event.stopPropagation(); handleReportGroup(' + postId + ',\'违规，下架\')">违规，下架</button>';
                actionHtml += ' <button class="btn" onclick="event.stopPropagation(); handleReportGroup(' + postId + ',\'不违规，驳回\')">不违规，驳回</button>';
            }else{
                actionHtml += ' <span class="muted">已处理</span>';
            }

            html += '<div class="card" style="margin-bottom:12px;border:1px solid #eef1f6;">'
                + '<div class="row" style="justify-content:space-between;align-items:flex-start;cursor:pointer;" onclick="toggleReportGroup(\'' + groupKey + '\')">'
                + '<div>'
                + '<div style="font-weight:700;font-size:15px;">' + esc(postPreview) + '</div>'
                + '<div class="muted" style="margin-top:4px;">帖子ID：' + esc(group.postId) + ' ｜ 举报总数：' + esc((group.reports || []).length) + ' ｜ 待处理：' + esc(group.pendingCount) + ' ｜ 已处理：' + esc(group.processedCount) + '</div>'
                + '</div>'
                + '<div>' + actionHtml + '</div>'
                + '</div>';

            if(expanded){
                html += '<div style="margin-top:10px;">';
            html += '<table class="table"><thead><tr><th>ID</th><th>举报人</th><th>举报原因</th><th>补充描述</th><th>举报时间</th><th>状态</th><th>处理信息</th><th>操作</th></tr></thead><tbody>';
            for(var j=0;j<(group.reports || []).length;j++){
                var it = group.reports[j] || {};
                var reportId = Number(it.id);
                var status = Number(it.status);
                var handledInfo = '-';
                if(status !== 0){
                    var adminName = it.handledAdminName ? String(it.handledAdminName) : ('管理员#' + (it.handledBy || '-'));
                    handledInfo = adminName + ' / ' + formatTime(it.handledAt) + (it.adminRemark ? (' / 备注:' + it.adminRemark) : '');
                }
                html += '<tr>'
                    + '<td>' + esc(reportId) + '</td>'
                    + '<td>' + esc((it.reporterNickname || '用户') + '(' + (it.reporterId || '-') + ')') + '</td>'
                    + '<td>' + esc(reasonTypeText(it.reasonType)) + '</td>'
                    + '<td>' + esc(it.reasonDesc || '-') + '</td>'
                    + '<td>' + esc(formatTime(it.createdAt)) + '</td>'
                    + '<td>' + esc(reportHandleStatusText(status)) + '</td>'
                    + '<td>' + esc(handledInfo) + '</td>'
                    + '<td><button class="btn" onclick="viewPostDetailByReport(' + reportId + ')">查看帖子</button></td></tr>';
            }
            html += '</tbody></table></div>';
            }

            html += '</div>';
        }
        document.getElementById('tableWrap').innerHTML = html;
    }

    async function handleReportGroup(postId, action){
        var lockKey = 'post_' + postId + '_' + action;
        if(reportProcessingMap[lockKey]) return;
        var adminRemark = prompt('请输入处理备注（可空）', '') || '';
        reportProcessingMap[lockKey] = true;
        loadReportList();
        try{
            var data = await api('/api/admin/post/handleReportByPost/' + postId, 'POST', { action: action, adminRemark: adminRemark });
            alert(data.msg || (data.code===200 ? '处理成功' : '处理失败'));
        }finally{
            reportProcessingMap[lockKey] = false;
            loadReportList();
        }
    }

    async function restorePost(postId){
        var data = await api('/api/admin/post/restore/' + postId, 'POST');
        alert(data.msg || (data.code===200 ? '恢复成功' : '恢复失败'));
    }

    function normalizeImageList(imagesRaw){
        if(imagesRaw === null || imagesRaw === undefined) return [];
        var s = String(imagesRaw).trim();
        if(!s) return [];

        // 兼容 JSON 数组字符串
        if((s[0] === '[' && s[s.length - 1] === ']') || (s[0] === '"' && s[s.length - 1] === '"')){
            try{
                var parsed = JSON.parse(s);
                if(Array.isArray(parsed)){
                    return parsed.map(function(it){ return String(it || '').trim(); }).filter(function(it){ return !!it; });
                }
            }catch(e){}
        }

        // 兼容逗号/分号分隔
        return s.split(/[,;，；\n\r]+/)
            .map(function(it){ return String(it || '').trim(); })
            .filter(function(it){ return !!it; });
    }

    function openPostDetailModal(html){
        document.getElementById('postDetailContent').innerHTML = html;
        document.getElementById('postDetailModalMask').style.display = 'flex';
    }

    function closePostDetailModal(){
        document.getElementById('postDetailModalMask').style.display = 'none';
    }

    function renderExtraField(label, value){
        var show = (value === null || value === undefined || value === '') ? '-' : value;
        return '<div class="kv"><b>' + label + '：</b>' + esc(show) + '</div>';
    }

    function buildImageHtml(imgs){
        if(imgs.length === 0){
            return '<div class="kv"><b>图片：</b>-</div>';
        }
        return '<div class="kv"><b>图片（' + imgs.length + '）</b></div><div class="img-grid">'
            + imgs.map(function(url){
                var safeUrl = esc(url);
                return '<a href="' + safeUrl + '" target="_blank" rel="noopener noreferrer">'
                    + '<img src="' + safeUrl + '" alt="post-image"/>'
                    + '</a>';
            }).join('')
            + '</div>';
    }

    async function viewPostDetailByReport(reportId){
        if(!Number.isFinite(Number(reportId)) || Number(reportId) <= 0){
            alert('缺少有效举报ID，无法查看帖子详情');
            return;
        }
        var data = await api('/api/admin/post/detailByReport/' + Number(reportId));
        if(data.code !== 200 || !data.data){
            alert(data.msg || '加载帖子详情失败');
            return;
        }

        var p = data.data;
        var imgs = normalizeImageList(p.images);
        var imageHtml = buildImageHtml(imgs);

        var html = ''
            + '<div class="kv"><b>帖子ID：</b>' + esc(p.id || '-') + '</div>'
            + renderExtraField('标题', p.title)
            + renderExtraField('分类', p.category)
            + renderExtraField('状态', postStatusText(Number(p.status)))
            + renderExtraField('举报次数', p.reportCount)
            + renderExtraField('发布时间', formatTime(p.createTime))
            + renderExtraField('发布用户ID', p.userId)
            + renderExtraField('联系方式', p.contact)
            + renderExtraField('地点', p.location)
            + '<div class="kv"><b>正文：</b></div>'
            + '<div class="post-desc">' + esc(p.description || '-') + '</div>'
            + imageHtml;

        openPostDetailModal(html);
    }

    function logout(){
        clearToken();
        window.location.replace(BASE + '/admin/login');
    }

    init();
</script>
</body>
</html>
