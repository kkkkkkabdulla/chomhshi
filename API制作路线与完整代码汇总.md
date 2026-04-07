# 后端接口：制作路线 + 完整代码汇总

> 基于当前项目（SSM）整理。本文档按 `后端接口文档.md` 的每个 API 分组给出：
> 1) 制作路线（从 Controller 到 SQL）
> 2) 可直接落地的完整代码（Controller/Service/Mapper 关键实现）
>
> 说明：为避免文档无限膨胀，通用基础设施（`Result`、`BusinessException`、拦截器注册、数据库连接配置）不在每个接口重复贴，默认沿用项目现有实现。

---

## 统一约定

- 返回：`Result<T>`
- 用户鉴权：`TokenInterceptor`
- 管理员鉴权：`AdminTokenInterceptor`
- 分页：`PageResult<T>`
- 事务：Service 写操作 `@Transactional(rollbackFor = Exception.class)`

---

# 1. 用户认证与资料

## 1.1 POST `/api/user/wxLogin`

### 制作路线
1. Controller 收 `WxLoginReq`
2. Service 根据 code/mock 信息查或建用户
3. 生成 token，写入 `user_token`
4. 返回 `LoginResp(token + user)`

### 完整代码（核心）

```java
@RestController
@RequestMapping("/api/user")
public class UserController {

    @Resource
    private UserService userService;

    @PostMapping("/wxLogin")
    public Result<LoginResp> wxLogin(@Valid @RequestBody WxLoginReq req) {
        return Result.success("登录成功", userService.wxLogin(req));
    }
}
```

```java
public interface UserService {
    LoginResp wxLogin(WxLoginReq req);
    User verifyToken(String token);
    UserInfoResp info(Integer userId);
    void updateInfo(Integer userId, UpdateUserInfoReq req);
}
```

```java
@Service
public class UserServiceImpl implements UserService {

    @Resource private UserMapper userMapper;
    @Resource private UserTokenMapper userTokenMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LoginResp wxLogin(WxLoginReq req) {
        // mock 场景：用 code 映射 openid
        String openid = "mock_openid_" + req.getCode();
        User user = userMapper.findByOpenid(openid);
        if (user == null) {
            user = new User();
            user.setOpenid(openid);
            user.setNickname("用户" + req.getCode());
            user.setStatus(1);
            user.setRole(1);
            userMapper.insert(user);
        }

        String token = TokenUtil.generateToken();
        Date expire = new Date(System.currentTimeMillis() + 7L * 24 * 3600 * 1000);

        UserToken old = userTokenMapper.findByUserId(user.getId());
        if (old == null) {
            UserToken t = new UserToken();
            t.setUserId(user.getId());
            t.setToken(token);
            t.setExpireTime(expire);
            userTokenMapper.insert(t);
        } else {
            old.setToken(token);
            old.setExpireTime(expire);
            userTokenMapper.updateByUserId(old);
        }

        LoginResp resp = new LoginResp();
        resp.setToken(token);
        UserInfoResp info = new UserInfoResp();
        info.setId(user.getId());
        info.setNickname(user.getNickname());
        info.setAvatarUrl(user.getAvatarUrl());
        info.setPhone(user.getPhone());
        resp.setUser(info);
        return resp;
    }

    // verifyToken/info/updateInfo 略（项目中已有）
}
```

---

## 1.2 GET `/api/user/verifyToken`

### 制作路线
1. 拦截器先校验 token
2. Controller 拿 request 中当前用户
3. 返回成功

### 完整代码（核心）

```java
@GetMapping("/verifyToken")
public Result<Void> verifyToken(HttpServletRequest request) {
    User user = (User) request.getAttribute(TokenInterceptor.CURRENT_USER_ATTR);
    return Result.success("token有效", null);
}
```

---

## 1.3 GET `/api/user/info`

```java
@GetMapping("/info")
public Result<UserInfoResp> info(HttpServletRequest request) {
    User user = (User) request.getAttribute(TokenInterceptor.CURRENT_USER_ATTR);
    return Result.success(userService.info(user.getId()));
}
```

---

## 1.4 PUT `/api/user/updateInfo`

```java
@PutMapping("/updateInfo")
public Result<Void> updateInfo(@Valid @RequestBody UpdateUserInfoReq req, HttpServletRequest request) {
    User user = (User) request.getAttribute(TokenInterceptor.CURRENT_USER_ATTR);
    userService.updateInfo(user.getId(), req);
    return Result.success("更新成功", null);
}
```

---

# 2. 帖子模块

## 2.1 POST `/api/post/publish`

### 制作路线
1. Controller 收参 + 取用户
2. Service 校验（标题/描述/联系方式/分类）
3. 敏感词检测：命中进入人工审核，不命中自动通过
4. `postMapper.insert`
5. 返回 `PostPublishResp`

### 完整代码（当前实现）

```java
@PostMapping("/publish")
public Result<PostPublishResp> publish(@Valid @RequestBody PostPublishReq req, HttpServletRequest request) {
    User user = (User) request.getAttribute(TokenInterceptor.CURRENT_USER_ATTR);
    PostPublishResp resp = postService.publish(user.getId(), req);
    String msg = resp.isAutoApproved() ? "发布成功，自动审核通过" : "发布成功，已进入人工审核";
    return Result.success(msg, resp);
}
```

```java
@Override
@Transactional(rollbackFor = Exception.class)
public PostPublishResp publish(Integer userId, PostPublishReq req) {
    validateTypeSpecificFields(req);

    String hitWord = detectSensitiveContent(req);
    boolean autoApproved = (hitWord == null);
    int status = autoApproved ? 1 : 0;

    Post post = new Post();
    post.setUserId(userId);
    post.setType(req.getType());
    post.setTitle(req.getTitle());
    post.setDescription(req.getDescription());
    post.setCategory(req.getCategory());
    post.setPrice(req.getPrice());
    post.setImages(req.getImages());
    post.setContact(req.getContact());
    post.setLostStatus(req.getLostStatus());
    post.setStatus(status);
    post.setViewCount(0);
    post.setLikeCount(0);
    post.setCommentCount(0);
    post.setReportCount(0);

    int rows = postMapper.insert(post);
    if (rows <= 0 || post.getId() == null) {
        throw new BusinessException(ResultCode.BUSINESS_ERROR.getCode(), "发布失败，请稍后重试");
    }

    String reviewNote = autoApproved ? "自动审核通过，已上架" : ("命中敏感词「" + hitWord + "」，已进入人工审核队列");
    return new PostPublishResp(post.getId(), status, autoApproved, reviewNote);
}
```

---

## 2.2 GET `/api/post/myList`

```java
@GetMapping("/myList")
public Result<PageResult<Post>> myList(@RequestParam(defaultValue = "1") Integer page,
                                       @RequestParam(defaultValue = "10") Integer pageSize,
                                       HttpServletRequest request) {
    User user = (User) request.getAttribute(TokenInterceptor.CURRENT_USER_ATTR);
    return Result.success(postService.myList(user.getId(), page, pageSize));
}
```

---

## 2.3 GET `/api/post/list`

```java
@GetMapping("/list")
public Result<PageResult<Post>> list(@RequestParam(required = false) Integer type,
                                     @RequestParam(required = false) String keyword,
                                     @RequestParam(required = false) String category,
                                     @RequestParam(defaultValue = "1") Integer page,
                                     @RequestParam(defaultValue = "10") Integer pageSize) {
    return Result.success(postService.list(type, keyword, category, page, pageSize));
}
```

---

## 2.4 GET `/api/post/detail/{id}`

```java
@GetMapping("/detail/{id}")
public Result<Post> detail(@PathVariable("id") Integer id) {
    return Result.success(postService.detail(id));
}
```

---

## 2.5 PUT `/api/post/update/{id}`

```java
@PutMapping("/update/{id}")
public Result<Void> update(@PathVariable("id") Integer id,
                           @Valid @RequestBody PostPublishReq req,
                           HttpServletRequest request) {
    User user = (User) request.getAttribute(TokenInterceptor.CURRENT_USER_ATTR);
    postService.update(user.getId(), id, req);
    return Result.success("更新成功", null);
}
```

---

## 2.6 DELETE `/api/post/delete/{id}`

```java
@DeleteMapping("/delete/{id}")
public Result<Void> delete(@PathVariable("id") Integer id, HttpServletRequest request) {
    User user = (User) request.getAttribute(TokenInterceptor.CURRENT_USER_ATTR);
    postService.delete(user.getId(), id);
    return Result.success("删除成功", null);
}
```

```java
@Override
@Transactional(rollbackFor = Exception.class)
public void delete(Integer userId, Integer postId) {
    Post post = requireOwnedPost(userId, postId);
    int rows = postMapper.deleteById(postId);
    if (rows <= 0) {
        throw new BusinessException(ResultCode.BUSINESS_ERROR.getCode(), "删除失败，请稍后重试");
    }
}
```

---

# 3. 点赞模块

## 3.1 POST `/api/post/like/{id}`

```java
@PostMapping("/like/{id}")
public Result<PostLikeResp> like(@PathVariable("id") Integer id, HttpServletRequest request) {
    User user = (User) request.getAttribute(TokenInterceptor.CURRENT_USER_ATTR);
    return Result.success(postService.toggleLike(user.getId(), id));
}
```

```java
@Override
@Transactional(rollbackFor = Exception.class)
public PostLikeResp toggleLike(Integer userId, Integer postId) {
    Post post = postMapper.findById(postId);
    if (post == null || post.getStatus() == null || post.getStatus() != 1) {
        throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "帖子不存在或不可点赞");
    }

    Integer count = postLikeMapper.countByPostAndUser(postId, userId);
    boolean currentlyLiked = count != null && count > 0;

    if (currentlyLiked) {
        postLikeMapper.delete(postId, userId);
        postMapper.decreaseLikeCount(postId);
    } else {
        postLikeMapper.insert(postId, userId);
        postMapper.increaseLikeCount(postId);
    }

    Post latest = postMapper.findById(postId);
    return new PostLikeResp(!currentlyLiked, latest == null ? 0 : latest.getLikeCount());
}
```

---

## 3.2 GET `/api/post/isLiked/{id}`

```java
@GetMapping("/isLiked/{id}")
public Result<PostLikeResp> isLiked(@PathVariable("id") Integer id, HttpServletRequest request) {
    User user = (User) request.getAttribute(TokenInterceptor.CURRENT_USER_ATTR);
    boolean liked = postService.isLiked(user.getId(), id);
    return Result.success(new PostLikeResp(liked, null));
}
```

---

# 4. 评论模块

## 4.1 POST `/api/comment/add`

```java
@PostMapping("/add")
public Result<CommentAddResp> add(@Valid @RequestBody CommentAddReq req, HttpServletRequest request) {
    User user = (User) request.getAttribute(TokenInterceptor.CURRENT_USER_ATTR);
    Integer commentId = commentService.add(user.getId(), req);
    return Result.success("评论成功", new CommentAddResp(commentId));
}
```

---

## 4.2 GET `/api/comment/list/{postId}`

```java
@GetMapping("/list/{postId}")
public Result<PageResult<PostCommentResp>> list(@PathVariable("postId") Integer postId,
                                                 @RequestParam(defaultValue = "1") Integer page,
                                                 @RequestParam(defaultValue = "10") Integer pageSize) {
    return Result.success(commentService.list(postId, page, pageSize));
}
```

```java
<select id="findByPostId" resultMap="CommentRespMap">
    SELECT c.id, c.post_id, c.user_id, c.content, c.parent_id, c.create_time,
           u.nickname, u.avatar_url
    FROM post_comment c
    LEFT JOIN user u ON c.user_id = u.id
    WHERE c.post_id = #{postId}
    ORDER BY c.id DESC
    LIMIT #{offset}, #{pageSize}
</select>
```

---

## 4.3 DELETE `/api/comment/delete/{id}`

```java
@DeleteMapping("/delete/{id}")
public Result<Void> delete(@PathVariable("id") Integer id, HttpServletRequest request) {
    User user = (User) request.getAttribute(TokenInterceptor.CURRENT_USER_ATTR);
    commentService.delete(user.getId(), id);
    return Result.success("删除成功", null);
}
```

---

# 5. 举报模块

## 5.1 POST `/api/report/add`

### 制作路线
1. 校验帖子可举报
2. 写 `post_report`
3. 帖子 `report_count +1`
4. 达阈值（>=5）自动下架 `status=3`

### 完整代码（核心）

```java
@PostMapping("/add")
public Result<Void> add(@Valid @RequestBody ReportAddReq req, HttpServletRequest request) {
    User user = (User) request.getAttribute(TokenInterceptor.CURRENT_USER_ATTR);
    reportService.add(user.getId(), req);
    return Result.success("举报成功", null);
}
```

---

# 6. 上传模块

## 6.1 POST `/api/upload/image`

```java
@PostMapping("/image")
public Result<UploadImageResp> upload(@RequestParam("file") MultipartFile file) {
    String url = fileUploadUtil.saveImage(file);
    return Result.success(new UploadImageResp(url));
}
```

---

## 6.2 GET `/api/upload/file/{filename}`

```java
@GetMapping("/file/{filename:.+}")
public void getFile(@PathVariable String filename, HttpServletResponse response) {
    fileUploadUtil.outputFile(filename, response);
}
```

---

# 7. 管理员接口

## 7.1 POST `/api/admin/login`

```java
@PostMapping("/login")
public Result<LoginResp> login(@Valid @RequestBody AdminLoginReq req) {
    return Result.success("登录成功", adminService.login(req));
}
```

---

## 7.2 GET `/api/admin/profile`

```java
@GetMapping("/profile")
public Result<Map<String, Object>> profile(HttpServletRequest request) {
    Admin admin = (Admin) request.getAttribute(AdminTokenInterceptor.CURRENT_ADMIN_ATTR);
    Map<String, Object> data = new HashMap<>();
    data.put("id", admin.getId());
    data.put("username", admin.getUsername());
    return Result.success(data);
}
```

---

## 7.3 GET `/api/admin/post/list`

```java
@GetMapping("/post/list")
public Result<PageResult<Post>> postList(@RequestParam(required = false) Integer status,
                                         @RequestParam(defaultValue = "1") Integer page,
                                         @RequestParam(defaultValue = "10") Integer pageSize) {
    return Result.success(adminService.postList(status, page, pageSize));
}
```

---

## 7.4 POST `/api/admin/post/approve/{id}`

```java
@PostMapping("/post/approve/{id}")
public Result<Void> approve(@PathVariable("id") Integer id, HttpServletRequest request) {
    Admin admin = (Admin) request.getAttribute(AdminTokenInterceptor.CURRENT_ADMIN_ATTR);
    adminService.approvePost(admin.getId(), id);
    return Result.success("审核通过", null);
}
```

---

## 7.5 POST `/api/admin/post/reject/{id}`

```java
@PostMapping("/post/reject/{id}")
public Result<Void> reject(@PathVariable("id") Integer id,
                           @Valid @RequestBody AdminPostRejectReq req,
                           HttpServletRequest request) {
    Admin admin = (Admin) request.getAttribute(AdminTokenInterceptor.CURRENT_ADMIN_ATTR);
    adminService.rejectPost(admin.getId(), id, req);
    return Result.success("已拒绝", null);
}
```

---

## 7.6 DELETE `/api/admin/post/delete/{id}`

```java
@DeleteMapping("/post/delete/{id}")
public Result<Void> deletePost(@PathVariable("id") Integer id, HttpServletRequest request) {
    Admin admin = (Admin) request.getAttribute(AdminTokenInterceptor.CURRENT_ADMIN_ATTR);
    adminService.deletePost(admin.getId(), id);
    return Result.success("删除成功", null);
}
```

---

## 7.7 GET `/api/admin/post/reportList`

```java
@GetMapping("/post/reportList")
public Result<PageResult<PostReport>> reportList(@RequestParam(required = false) Integer status,
                                                 @RequestParam(defaultValue = "1") Integer page,
                                                 @RequestParam(defaultValue = "10") Integer pageSize) {
    return Result.success(adminService.reportList(status, page, pageSize));
}
```

---

## 7.8 POST `/api/admin/post/handleReport/{id}`

```java
@PostMapping("/post/handleReport/{id}")
public Result<Void> handleReport(@PathVariable("id") Integer id,
                                 @Valid @RequestBody AdminHandleReportReq req,
                                 HttpServletRequest request) {
    Admin admin = (Admin) request.getAttribute(AdminTokenInterceptor.CURRENT_ADMIN_ATTR);
    adminService.handleReport(admin.getId(), id, req);
    return Result.success("处理成功", null);
}
```

---

## 7.9 POST `/api/admin/post/restore/{id}`

```java
@PostMapping("/post/restore/{id}")
public Result<Void> restore(@PathVariable("id") Integer id, HttpServletRequest request) {
    Admin admin = (Admin) request.getAttribute(AdminTokenInterceptor.CURRENT_ADMIN_ATTR);
    adminService.restorePost(admin.getId(), id);
    return Result.success("已恢复", null);
}
```

---

# 附：每个 API 的最小落地 checklist

1. Request DTO + 校验注解
2. Controller 路由 + `Result` 返回
3. Service 业务规则 + 事务
4. Mapper 接口 + XML SQL
5. 鉴权拦截器放行/拦截配置
6. Postman happy path + 异常 path

---

如果你需要，我可以在下一版把本文档继续扩展成“逐接口文件路径索引”（每个 API 精确对应到项目中的 Controller/Service/Mapper 文件名与方法名）。
