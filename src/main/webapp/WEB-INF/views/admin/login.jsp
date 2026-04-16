<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8"/>
    <title>管理员登录</title>
    <style>
        body{margin:0;background:#f5f7fb;font-family:Arial,sans-serif}
        .wrap{width:380px;margin:120px auto;background:#fff;border-radius:12px;padding:28px;box-shadow:0 8px 24px rgba(0,0,0,.08)}
        h2{margin:0 0 18px}
        .input{width:100%;box-sizing:border-box;padding:10px 12px;margin-bottom:12px;border:1px solid #d9dde7;border-radius:8px}
        .btn{width:100%;padding:10px;border:none;border-radius:8px;background:#1677ff;color:#fff;cursor:pointer}
        .msg{margin-top:10px;color:#e53935;min-height:20px}
    </style>
</head>
<body>
<div class="wrap">
    <h2>管理后台登录</h2>
    <input id="username" class="input" placeholder="账号"/>
    <input id="password" class="input" type="password" placeholder="密码"/>
    <button class="btn" onclick="doLogin()">登录</button>
    <div id="msg" class="msg"></div>
</div>

<script>
    var BASE = '<%=request.getContextPath()%>';

    function setCookie(name, value, maxAgeSeconds){
        document.cookie = name + '=' + encodeURIComponent(value) + '; Path=' + BASE + '; Max-Age=' + maxAgeSeconds;
    }

    function getCookie(name){
        var arr = document.cookie ? document.cookie.split('; ') : [];
        for(var i=0;i<arr.length;i++){
            var kv = arr[i].split('=');
            if(kv[0]===name) return decodeURIComponent(kv.slice(1).join('='));
        }
        return '';
    }

    function saveToken(token){
        localStorage.setItem('adminToken', token);
        setCookie('adminToken', token, 7 * 24 * 60 * 60);
    }

    function clearToken(){
        localStorage.removeItem('adminToken');
        document.cookie = 'adminToken=; Path=' + BASE + '; Max-Age=0';
    }

    async function verifyAndJumpIfLoggedIn(){
        var token = localStorage.getItem('adminToken') || getCookie('adminToken');
        if(!token) return;

        try{
            var res = await fetch(BASE + '/api/admin/profile', {
                method:'GET',
                headers:{ token: token }
            });
            var data = await res.json();
            if(data.code === 200){
                saveToken(token);
                window.location.assign(BASE + '/admin/index?t=' + encodeURIComponent(token));
            }else{
                clearToken();
            }
        }catch(e){
            // 网络异常时不自动跳，留在登录页
        }
    }

    async function doLogin(){
        var username = document.getElementById('username').value.trim();
        var password = document.getElementById('password').value.trim();
        var msg = document.getElementById('msg');
        msg.innerText = '';

        if(!username || !password){
            msg.innerText = '账号和密码不能为空';
            return;
        }

        try{
            var res = await fetch(BASE + '/api/admin/login', {
                method:'POST',
                headers:{'Content-Type':'application/json'},
                body: JSON.stringify({username:username, password:password})
            });
            var data = await res.json();
            if(data.code !== 200 || !data.data || !data.data.token){
                msg.innerText = data.msg || '登录失败';
                return;
            }

            saveToken(data.data.token);
            window.location.href = BASE + '/admin/index?t=' + encodeURIComponent(data.data.token);
        }catch(e){
            msg.innerText = '网络异常，请稍后重试';
        }
    }

    verifyAndJumpIfLoggedIn();
</script>
</body>
</html>
