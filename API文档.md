# MikuFans Cloud API 接口文档

> **项目**: mikufans-cloud — 视频分享平台微服务系统  
> **技术栈**: Spring Boot 3.3.11 + Spring Cloud 2023.0.3 + Nacos  
> **基路径**: `http://{host}:{port}`  
> **鉴权方式**: Cookie Token (`Authorization` header 或 Cookie 传递)

---

## 统一响应格式

所有接口统一返回 `ResponseVO<T>` 格式：

```json
{
  "status": "success | error",
  "code": 200,
  "info": "操作成功",
  "data": {}
}
```

| code | 说明 |
|------|------|
| 200  | 成功 |
| 404  | 资源不存在 |
| 500  | 服务器内部错误 |
| 503  | 远程服务不可用 |
| 600  | 参数错误 |
| 601  | 主键冲突 |

---

# 一、Web 模块 (mikufans-cloud-web)

> 面向 C 端用户的 Web 服务，提供账号、视频、用户主页、创作中心等功能。

---

## 1.1 AccountController — 账号管理

**路径前缀**: `/account`

### 1.1.1 获取图形验证码

```
GET /account/checkCode
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| 无   | —    | —    | —    |

**返回**:

```json
{
  "checkCode": "data:image/png;base64,...",
  "checkCodeKey": "mikufans:checkCode:uuid"
}
```

### 1.1.2 注册账号

```
POST /account/register
Content-Type: application/json
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| email | String | ✅ | 邮箱地址 |
| nickName | String | ✅ | 昵称 |
| password | String | ✅ | 密码 |
| checkCodeKey | String | ✅ | 验证码 Key（来自 checkCode 接口） |
| checkCode | String | ✅ | 验证码结果 |
| birthday | String | ❌ | 生日 `yyyy-MM-dd` |

### 1.1.3 登录账号

```
POST /account/login
Content-Type: application/json
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| email | String | ✅ | 邮箱 |
| password | String | ✅ | 密码 |
| checkCodeKey | String | ✅ | 验证码 Key |
| checkCode | String | ✅ | 验证码结果 |

**返回**: `TokenUserInfoDto` — 含 `token`、`userId`、`nickName`、`expireTime`。

### 1.1.4 自动登录（Token 续期）

```
GET /account/autoLogin
```

> 需携带 Cookie Token。Token 距离过期 < 1天时自动续期。

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| 无   | —    | —    | 通过 Cookie 传递 Token |

**返回**: `TokenUserInfoDto`（未登录返回 `null`）

### 1.1.5 退出登录

```
GET /account/logout
```

> 清除 Cookie 和 Redis 中的 Token。

### 1.1.6 获取用户硬币/粉丝/关注数

```
GET /account/getUserCountInfo
```

> 需登录。返回 `UserCountInfoDto`（coinCount、fansCount、focusCount 等）。

---

## 1.2 VideoController — 视频浏览

**路径前缀**: `/video`

### 1.2.1 获取首页推荐视频

```
GET /video/loadRecommendVideo
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| 无   | —    | —    | —    |

**返回**: `List<VideoInfo>` — 按 `create_time desc` 排序的推荐视频列表。

### 1.2.2 获取视频列表（分类筛选）

```
POST /video/loadVideoList
Content-Type: application/x-www-form-urlencoded
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| categoryId | Integer | ❌ | 子分类 ID |
| pCategoryId | Integer | ❌ | 父分类 ID |
| pageNo | Integer | ❌ | 页码（不传默认全部） |
| pageSize | Integer | ❌ | 每页数量 |

**返回**: `PaginationResultVO<VideoInfo>`

### 1.2.3 获取视频详情

```
GET /video/getVideoInfo
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| videoId | String | ✅ | 视频 ID |

**返回**: `VideoResultVO` — 包含视频详情 + 当前用户行为（点赞/投币/收藏状态）

### 1.2.4 获取视频分P列表

```
GET /video/getPVideoInfo
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| videoId | String | ✅ | 视频 ID |

**返回**: `List<VideoInfoFile>` — 按 `file_index asc` 排列的分P信息

### 1.2.5 搜索视频 (ES)

```
GET /video/search
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| keyword | String | ✅ | 搜索关键词 |
| orderType | Integer | ❌ | 排序类型（见 `SearchOrderTypeEnum`） |
| pageNo | Integer | ❌ | 页码（默认 30 条/页） |

**返回**: `PaginationResultVO<VideoInfo>`（来自 Elasticsearch）

### 1.2.6 获取推荐视频 (ES)

```
GET /video/getRecommendVideo
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| keyword | String | ✅ | 关键词 |
| videoId | String | ✅ | 当前视频 ID（排除自身） |

**返回**: `List<VideoInfo>` — 排除当前视频后的推荐列表（10条）

### 1.2.7 获取搜索热词 TOP10

```
GET /video/getHotKeyWord
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| 无   | —    | —    | —    |

**返回**: `List<String>` — 当天搜索热词

### 1.2.8 获取 24 小时热门视频

```
GET /video/getHotVideoList
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| pageNo | Integer | ❌ | 页码 |

**返回**: `PaginationResultVO<VideoInfo>` — 按播放量降序

---

## 1.3 UHomeController — 用户主页

**路径前缀**: `/uHome`

### 1.3.1 获取用户信息

```
GET /uHome/getUserInfo
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| userId | String | ✅ | 目标用户 ID |

**返回**: `UserInfoVO` — 含是否已关注状态

### 1.3.2 更新用户信息

```
POST /uHome/updateUserInfo
Content-Type: application/json
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| nickName | String | ❌ | 昵称 |
| avatar | String | ❌ | 头像路径 |
| sex | Integer | ❌ | 性别 |
| birthday | String | ❌ | 生日 |
| school | String | ❌ | 学校 |
| personIntroduction | String | ❌ | 个人简介 |
| noticeInfo | String | ❌ | 公告信息 |

> 需登录。

### 1.3.3 保存主题设置

```
PUT /uHome/saveTheme
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| theme | Integer | ✅ | 主题 ID |

> 需登录。

### 1.3.4 关注用户

```
POST /uHome/focusUser
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| focusUserId | String | ✅ | 要关注的用户 ID |

> 需登录。

### 1.3.5 取消关注用户

```
POST /uHome/cancelFocusUser
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| focusUserId | String | ✅ | 要取消关注的用户 ID |

> 需登录。

### 1.3.6 查询粉丝列表

```
GET /uHome/loadFansList
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| pageNo | Integer | ❌ | 页码 |

> 需登录。

**返回**: `PaginationResultVO<UserFocus>` — 按关注时间降序

### 1.3.7 查询关注列表

```
GET /uHome/loadFocusList
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| pageNo | Integer | ❌ | 页码 |

> 需登录。

**返回**: `PaginationResultVO<UserFocus>`

### 1.3.8 查询主页视频列表

```
GET /uHome/loadVideoList
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| userId | String | ✅ | 目标用户 ID |
| pageNo | Integer | ❌ | 页码 |
| type | Integer | ❌ | 不为空则 pageSize=10（封面展示） |
| orderType | Integer | ❌ | 排序：0=最新发布、1=最多播放、2=最多收藏 |
| VideoName | String | ❌ | 视频名称模糊搜索 |

**返回**: `PaginationResultVO<VideoInfo>`

---

## 1.4 UcenterVideoPostController — 创作中心·视频投稿

**路径前缀**: `/ucenter`

### 1.4.1 上传视频信息（投稿）

```
POST /ucenter/postVideo
Content-Type: application/json
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| videoId | String | ❌ | 视频 ID（新建时为空） |
| videoName | String | ✅ | 视频名称 |
| videoCover | String | ✅ | 封面路径 |
| pCategoryId | Integer | ✅ | 父分类 ID |
| categoryId | Integer | ✅ | 子分类 ID |
| postType | Integer | ✅ | 投稿类型（原创/转载） |
| tags | String | ❌ | 标签 |
| introduction | String | ❌ | 简介 |
| interaction | String | ❌ | 互动设置（如关闭评论） |
| uploadFileList | String | ✅ | 分P文件列表（JSON 数组字符串） |

> `uploadFileList` 需解析为 `List<VideoInfoFilePost>`。

### 1.4.2 查询投稿列表

```
GET /ucenter/loadVideoList
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| status | Integer | ❌ | 稿件状态：-1=审核中、3=已通过、4=未通过 |
| pageNo | Integer | ❌ | 页码 |
| pageSize | Integer | ❌ | 每页数量 |
| videoNameFuzzy | String | ❌ | 视频名模糊搜索 |

> 需登录。

### 1.4.3 获取各状态视频数量

```
GET /ucenter/getVideoStatusCount
```

> 需登录。返回 `VideoStatusCountInfoVo`（auditPassCount、auditFailCount、inProgress）。

### 1.4.4 根据视频 ID 获取视频详情（编辑用）

```
GET /ucenter/getVideoByVideoId
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| videoId | String | ✅ | 视频 ID |

> 校验视频归属权。返回 `VideoPostEditInfoVO`（含分P信息）。

### 1.4.5 保存视频互动设置

```
POST /ucenter/saveVideoInteraction
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| videoId | String | ✅ | 视频 ID |
| interaction | String | ❌ | 互动信息（如 `"0"` 关闭评论） |

> 需登录。

### 1.4.6 删除视频稿件

```
DELETE /ucenter/deleteVideo
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| videoId | String | ✅ | 视频 ID |

> 需登录，校验视频归属权。

---

## 1.5 UcenterController — 创作中心

**路径前缀**: `/ucenter`

### 1.5.1 加载个人所有视频

```
GET /ucenter/loadAllVideo
```

> 需登录。返回 `List<VideoInfo>` — 按创建时间降序。

---

## 1.6 UcenterStatisticsController — 创作中心·数据统计

**路径前缀**: `/statistics`

### 1.6.1 获取实时统计数据

```
GET /statistics/getActualTimeStatisticsInfo
```

> 需登录。返回昨天数据 + 总计数据（播放量/评论数/弹幕数/收藏数/硬币数）。

### 1.6.2 获取近 7 天统计趋势

```
GET /statistics/getWeekStatisticsInfo
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| dateType | Integer | ✅ | 数据类型（播放/评论/弹幕/收藏/硬币） |

> 需登录。返回 `List<StatisticsInfo>`。

---

## 1.7 UHomeVideoUserSeriesController — 视频合集

**路径前缀**: `/uHome/series`

### 1.7.1 获取合集列表（封面）

```
GET /uHome/series/loadVideoSeries
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| userId | String | ✅ | 用户 ID |

**返回**: `List<UserVideoSeries>` — 每个合集的封面视频

### 1.7.2 创建/编辑合集

```
POST /uHome/series/saveVideoSeries
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| seriesId | Integer | ❌ | 合集 ID（编辑时传入） |
| seriesName | String | ✅ | 合集名称 |
| description | String | ❌ | 描述（最长 200 字） |
| videoIds | String | ❌ | 视频 ID 列表，逗号分隔 |

> 需登录。

### 1.7.3 获取我的视频列表（用于添加合集）

```
GET /uHome/series/loadAllVideo
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| seriesId | Integer | ❌ | 合集 ID（排除已存在合集内的视频） |

> 需登录。

### 1.7.4 查看合集详情

```
GET /uHome/series/loadVideoSeriesDetail
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| seriesId | Integer | ✅ | 合集 ID |

> 需登录。返回 `VideoSeriesVO`。

### 1.7.5 向合集添加视频

```
POST /uHome/series/saveSeriesVideo
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| seriesId | Integer | ✅ | 合集 ID |
| videoIds | String | ✅ | 视频 ID 列表，逗号分隔 |

> 需登录。

### 1.7.6 从合集删除视频

```
DELETE /uHome/series/deleteSeriesInVideo
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| seriesId | Integer | ✅ | 合集 ID |
| videoId | String | ✅ | 要删除的视频 ID |

> 需登录。

### 1.7.7 更新合集排序

```
POST /uHome/series/updateSeriesSort
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| seriesIds | String | ✅ | 合集 ID 列表，逗号分隔（按前端排序） |

> 需登录。

### 1.7.8 获取个人主页合集列表

```
GET /uHome/series/loadUserVideoSeries
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| userId | String | ✅ | 用户 ID |

**返回**: `List<UserVideoSeries>` — 按排序字段升序

---

## 1.8 VideoPlayHistoryController — 播放历史

**路径前缀**: `/history`

### 1.8.1 加载播放历史

```
GET /history/loadHistoryVideo
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| pageNo | Integer | ❌ | 页码 |

> 需登录。返回 `PaginationResultVO<VideoPlayHistory>`。

### 1.8.2 清空播放历史

```
DELETE /history/cleanHistoryVideo
```

> 需登录。

### 1.8.3 删除单条播放历史

```
DELETE /history/deleteHistoryVideo
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| videoId | String | ✅ | 视频 ID |

> 需登录。

---

## 1.9 CategoryController — 分类（Web 端）

**路径前缀**: `/category`

### 1.9.1 加载全部分类

```
GET /category/loadAllCategory
```

> 通过 Feign 调用 Admin 服务。返回 `List<CategoryInfo>`（树形结构）。

---

## 1.10 SysSettingController — 系统设置（Web 端）

**路径前缀**: `/sysSetting`

### 1.10.1 获取系统设置

```
GET /sysSetting/getSysSetting
```

**返回**: `SysSettingDto`（注册奖励、视频大小限制等配置）

---

## 1.11 EmailController — 邮件服务

**路径前缀**: `/email`

### 1.11.1 发送邮件

```
POST /email/send
Content-Type: application/json
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| emailAccount | String | ✅ | 发件人邮箱账号 |
| emailPassword | String | ✅ | 授权码 |
| sendUserName | String | ✅ | 发件人名称 |
| receiveEmailAccount | String | ✅ | 收件人邮箱 |
| emailTitle | String | ✅ | 邮件标题 |
| content | String | ✅ | 邮件内容 |

---

# 二、Interact 模块 (mikufans-cloud-interact)

> 互动服务：弹幕、评论、用户行为、消息、在线人数。

---

## 2.1 UserActionController — 用户行为

**路径前缀**: `/userAction`

### 2.1.1 用户操作（点赞/收藏/投币）

```
POST /userAction/doAction
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| videoId | String | ✅ | 视频 ID |
| actionType | Integer | ✅ | 行为类型：1=视频点赞、2=视频投币、3=视频收藏、4=评论点赞、5=评论点踩 |
| actionCount | Integer | ❌ | 投币数量（1-2，默认1） |
| commentId | Integer | ❌ | 评论 ID（评论点赞/点踩时传入） |

> 需登录。通过 `@RecordUserMessage` 自动记录消息。

---

## 2.2 VideoDanmuController — 弹幕

**路径前缀**: `/danmu`

### 2.2.1 发布弹幕

```
POST /danmu/postDanMu
Content-Type: application/json
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| videoId | String | ✅ | 视频 ID |
| fileId | String | ✅ | 分P文件 ID |
| text | String | ✅ | 弹幕文本 |
| mode | Integer | ❌ | 弹幕模式（滚动/顶部/底部） |
| color | String | ❌ | 弹幕颜色 |
| time | Integer | ❌ | 弹幕出现时间（秒） |

> 需登录。

### 2.2.2 加载弹幕列表

```
GET /danmu/loadDanMu
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| videoId | String | ✅ | 视频 ID |
| fileId | String | ✅ | 分P文件 ID |

> 若视频关闭了弹幕（interaction 包含 "1"），返回空数组。

---

## 2.3 VideoCommentController — 评论

**路径前缀**: `/comment`

### 2.3.1 发布评论

```
POST /comment/postComment
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| videoId | String | ✅ | 视频 ID |
| content | String | ✅ | 评论内容（最长 500 字） |
| replyCommentId | Integer | ❌ | 回复的评论 ID |
| imgPath | String | ❌ | 图片路径（最长 50 字符） |

> 需登录。

### 2.3.2 加载评论列表

```
GET /comment/loadComment
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| videoId | String | ✅ | 视频 ID |
| orderType | Integer | ❌ | 0=最热（按点赞降序）、其他=最新（按评论ID降序） |
| PageNo | Integer | ❌ | 页码（15 条/页） |

> 若视频关闭评论（interaction 包含 "0"），返回空数组。首屏自动追加置顶评论。

### 2.3.3 置顶评论

```
GET /comment/topComment
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| commentId | Integer | ✅ | 评论 ID |

> 需登录（仅 UP 主可操作）。

### 2.3.4 取消置顶评论

```
GET /comment/cancelTopComment
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| commentId | Integer | ✅ | 评论 ID |

> 需登录（仅 UP 主可操作）。

### 2.3.5 删除评论

```
DELETE /comment/userDelComment
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| commentId | Integer | ✅ | 评论 ID |

> 需登录（评论作者或 UP 主可删除）。

---

## 2.4 UserMessageController — 用户消息

**路径前缀**: `/message`

### 2.4.1 获取未读消息数量

```
GET /message/getNoReadCount
```

> 需登录。返回未读消息总数。

### 2.4.2 获取未读消息数量（按类型分组）

```
GET /message/getNoReadCountGroup
```

> 需登录。返回 `List<UserMessageCountDto>`。

### 2.4.3 全部已读

```
PUT /message/readAllMessage
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| messageType | Integer | ✅ | 消息类型 |

> 需登录。将指定类型的消息全部标记已读。

### 2.4.4 获取消息列表

```
GET /message/loadMessageList
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| messageType | Integer | ✅ | 消息类型 |
| pageNo | Integer | ✅ | 页码 |

> 需登录。返回 `PaginationResultVO<UserMessage>`。

### 2.4.5 删除消息

```
DELETE /message/delMessage
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| messageId | Integer | ✅ | 消息 ID |

> 需登录。

---

## 2.5 OnlineController — 在线人数

**路径前缀**: `/online`

### 2.5.1 上报视频在线播放

```
GET /online/reportVideoPlayOneLine
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| deviceId | String | ✅ | 设备唯一标识（浏览器指纹） |
| fileId | String | ✅ | 视频文件 ID |

**返回**: 当前在线观看人数。

---

## 2.6 UHomeController (Interact) — 用户收藏列表

**路径前缀**: `/uHome`

### 2.6.1 查询收藏列表

```
GET /uHome/loadUserCollectList
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| userId | String | ✅ | 用户 ID |
| pageNo | Integer | ❌ | 页码 |

**返回**: `PaginationResultVO<UserAction>`

---

## 2.7 UcenterInteractionController — 创作中心·互动管理

**路径前缀**: `/ucenter`

### 2.7.1 查询评论（创作中心）

```
GET /ucenter/loadComment
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| pageNo | Integer | ❌ | 页码 |
| videoId | String | ❌ | 按视频筛选 |

> 需登录。返回 `PaginationResultVO<VideoComment>`。

### 2.7.2 删除评论

```
DELETE /ucenter/deleteComment
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| commentId | Integer | ✅ | 评论 ID |

> 需登录。

### 2.7.3 查询弹幕（创作中心）

```
GET /ucenter/loadDanMu
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| pageNo | Integer | ❌ | 页码 |
| videoId | String | ❌ | 按视频筛选 |

> 需登录。返回 `PaginationResultVO<VideoDanmu>`。

### 2.7.4 删除弹幕

```
DELETE /ucenter/deleteDanMu
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| danMuId | Integer | ✅ | 弹幕 ID |

> 需登录。

---

# 三、Admin 模块 (mikufans-cloud-admin)

> 管理员后台服务，使用独立 Token (`TOKEN_ADMIN`)。

---

## 3.1 AccountController — 管理员登录

**路径前缀**: `/account`

### 3.1.1 获取验证码

```
GET /account/checkCode
```

同 Web 端。

### 3.1.2 管理员登录

```
POST /account/login
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| account | String | ✅ | 管理员账号 |
| password | String | ✅ | 密码（MD5 加密） |
| checkCodeKey | String | ✅ | 验证码 Key |
| checkCode | String | ✅ | 验证码结果 |

**返回**: 管理员 Token 字符串。

### 3.1.3 退出登录

```
GET /account/logout
```

---

## 3.2 CategoryController — 分类管理

**路径前缀**: `/category`

### 3.2.1 查询分类列表

```
POST /category/loadCategory
Content-Type: application/json
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| pCategoryId | Integer | ❌ | 父分类 ID |
| categoryCode | String | ❌ | 分类编码 |

**返回**: `List<CategoryInfo>` — 树形结构。

### 3.2.2 新增/修改分类

```
POST /category/saveCategory
Content-Type: application/json
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| pCategoryId | Integer | ✅ | 父分类 ID（0=一级分类） |
| categoryId | Integer | ❌ | 编辑时传入 |
| categoryCode | String | ✅ | 分类编码 |
| categoryName | String | ✅ | 分类名称 |
| icon | String | ❌ | 图标 |
| background | String | ❌ | 背景图 |

### 3.2.3 删除分类

```
DELETE /category/delCategory
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| categoryId | Integer | ✅ | 分类 ID |

### 3.2.4 分类排序

```
POST /category/sortCategory
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| pCategoryId | Integer | ✅ | 父分类 ID |
| categoryIds | String | ✅ | 排序后的分类 ID 列表，逗号分隔 |

---

## 3.3 FileController — 文件管理

**路径前缀**: `/file`

### 3.3.1 上传图片

```
POST /file/uploadImage
Content-Type: multipart/form-data
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| file | MultipartFile | ✅ | 图片文件 |
| cover | Boolean | ✅ | 是否生成缩略图 |

**返回**: 图片访问路径。

### 3.3.2 获取资源文件

```
GET /file/getResource
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| sourceName | String | ✅ | 资源路径名称 |

> 通过 Feign 从 Resource 服务获取文件流，写入 HttpServletResponse。

### 3.3.3 获取视频资源（m3u8 索引）

```
GET /file/getVideoResource/{fileId}
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| fileId | String | ✅ | 文件 ID（路径参数） |

### 3.3.4 获取视频 TS 片段

```
GET /file/getVideoResourceTs/{fileId}/{ts}
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| fileId | String | ✅ | 文件 ID |
| ts | String | ✅ | TS 文件名 |

---

## 3.4 SysSettingController — 系统设置管理

**路径前缀**: `/sysSetting`

### 3.4.1 获取系统设置

```
GET /sysSetting/getSysSetting
```

### 3.4.2 更新系统设置

```
PUT /sysSetting/updateSysSetting
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| registerCoinCount | Integer | ❌ | 注册赠送硬币数 |
| videoSize | Integer | ❌ | 视频文件大小限制（MB） |
| commentOpen | Boolean | ❌ | 是否默认开启评论 |
| danmuOpen | Boolean | ❌ | 是否默认开启弹幕 |

> 设置保存在 Redis 中。

---

## 3.5 VideoInfoController — 视频审核管理

**路径前缀**: `/video`

### 3.5.1 查询投稿列表

```
POST /video/loadVideoList
Content-Type: application/json
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| status | Integer | ❌ | 按状态筛选 |
| videoNameFuzzy | String | ❌ | 视频名模糊搜索 |
| pageNo | Integer | ❌ | 页码 |

**返回**: `PaginationResultVO<VideoInfoPost>`

### 3.5.2 审核视频

```
POST /video/auditVideo
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| videoId | String | ✅ | 视频 ID |
| status | Integer | ✅ | 审核结果：3=通过、4=不通过 |
| reason | String | ❌ | 审核不通过原因 |

> 不通过时自动发送系统消息给用户。

### 3.5.3 推荐视频

```
POST /video/recommendVideo
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| videoId | String | ✅ | 视频 ID |

### 3.5.4 删除视频

```
DELETE /video/deleteVideo
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| videoId | String | ✅ | 视频 ID |

### 3.5.5 获取视频分P列表

```
GET /video/loadPVideoList
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| videoId | String | ✅ | 视频 ID |

**返回**: `List<VideoInfoFilePost>`

---

## 3.6 UcenterAdminStatisticsController — 管理员统计

**路径前缀**: `/statistics`

### 3.6.1 获取实时统计（全局）

```
GET /statistics/getActualTimeStatisticsInfo
```

**返回**: 昨天全站数据 + 总计数据（注册用户数替代粉丝数）。

### 3.6.2 获取近 7 天统计（全局）

```
GET /statistics/getWeekStatisticsInfo
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| dateType | Integer | ✅ | 数据类型 |

> 当 dateType 为粉丝类型时，返回全站注册用户数趋势。

---

## 3.7 InteractionController — 管理员互动管理

**路径前缀**: `/interaction`

### 3.7.1 查询评论

```
GET /interaction/loadComment
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| pageNo | Integer | ❌ | 页码 |
| videoNameFuzzy | String | ❌ | 按视频名模糊搜索 |
| pageSize | Integer | ❌ | 每页数量 |

### 3.7.2 删除评论

```
DELETE /interaction/delComment
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| commentId | Integer | ✅ | 评论 ID |

### 3.7.3 查询弹幕

```
GET /interaction/loadDanMu
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| pageNo | Integer | ❌ | 页码 |
| videoNameFuzzy | String | ❌ | 按视频名模糊搜索 |

### 3.7.4 删除弹幕

```
DELETE /interaction/delDanMu
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| danMuId | Integer | ✅ | 弹幕 ID |

---

# 四、Resource 模块 (mikufans-cloud-resource)

> 文件存储服务：图片上传、视频分片上传/合并、文件流读取。

---

## 4.1 FileController — 文件资源

**无需路径前缀**

### 4.1.1 获取静态资源

```
GET /getResource
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| sourceName | String | ✅ | 资源路径名 |

> 设置 `Cache-Control: max-age=2592000` 缓存一个月。直接写入 HttpServletResponse。

### 4.1.2 视频预上传

```
POST /preUploadVideo
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| fileName | String | ✅ | 文件名 |
| chunks | Integer | ✅ | 总分片数 |

> 需登录。返回 `uploadId`。

### 4.1.3 视频分片上传

```
POST /uploadVideo
Content-Type: multipart/form-data
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| chunkFile | MultipartFile | ✅ | 分片文件 |
| uploadId | String | ✅ | 上传 ID |
| chunkIndex | Integer | ✅ | 分片索引（从 0 开始） |

> 需登录。校验分片大小 ≤ 系统设置限制、分片顺序合法性。

### 4.1.4 取消上传

```
DELETE /delUploadVideo
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| uploadId | String | ✅ | 上传 ID |

> 需登录。清除 Redis 记录和临时文件目录。

### 4.1.5 上传图片

```
POST /uploadImage
Content-Type: multipart/form-data
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| file | MultipartFile | ✅ | 图片文件 |
| createIsThumbnail | Boolean | ✅ | 是否生成缩略图 |

**返回**: 图片访问相对路径（如 `cover/2025/01/01/xxx.png`）。

### 4.1.6 获取视频 m3u8 索引文件

```
GET /getVideoResource/{fileId}/
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| fileId | String | ✅ | 视频文件 ID（路径参数） |

> 直接返回 `index.m3u8` 文件流，同时记录播放信息到 Redis。

### 4.1.7 获取视频 TS 片段

```
GET /getVideoResource/{fileId}/{ts}
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| fileId | String | ✅ | 视频文件 ID |
| ts | String | ✅ | TS 片段文件名 |

---

# 五、内部 API（Feign Provider）

> 微服务间通过 Feign 调用的内部接口，路径前缀为 `/innerApi`。

---

## 5.1 CategoryApi — 分类内部接口

**路径前缀**: `/innerApi`

| 方法 | 路径 | 说明 | 调用方 |
|------|------|------|--------|
| GET | `/loadAllCategory` | 获取全部分类列表 | Web→Admin |

---

## 5.2 InteractApi — 互动内部接口

**路径前缀**: `/innerApi/interact`

| 方法 | 路径 | 参数 | 说明 | 调用方 |
|------|------|------|------|--------|
| GET | `/admin/loadComment` | pageNo, videoNameFuzzy, pageSize | 管理端查询评论 | Admin→Interact |
| DELETE | `/admin/delComment` | commentId | 管理端删除评论 | Admin→Interact |
| GET | `/admin/loadDanMu` | pageNo, videoNameFuzzy | 管理端查询弹幕 | Admin→Interact |
| DELETE | `/admin/delDanMu` | danMuId | 管理端删除弹幕 | Admin→Interact |
| DELETE | `/delCommentByVideoId` | videoId | 根据视频ID删评论 | Admin→Interact |
| DELETE | `/delDanMuByVideoId` | videoId | 根据视频ID删弹幕 | Admin→Interact |
| POST | `/userAction/getUserActionList` | JSON: UserActionQuery | 查询用户行为列表 | Web→Interact |

---

## 5.3 ResourceApi — 资源内部接口

**路径前缀**: `/innerApi/admin/file`

| 方法 | 路径 | 参数 | 说明 | 调用方 |
|------|------|------|------|--------|
| POST | `/uploadImage` | file, createThumbnail | 上传图片 | Admin→Resource |
| GET | `/getResource` | sourceName | 获取文件流 | Admin→Resource |
| GET | `/getVideoResource/{fileId}` | fileId | 获取视频 m3u8 | Admin→Resource |
| GET | `/getVideoResourceTs/{fileId}/{ts}` | fileId, ts | 获取视频 TS 片段 | Admin→Resource |

---

## 5.4 StatisticsApi — 统计内部接口

**路径前缀**: `/innerApi/statistics/admin`

| 方法 | 路径 | 参数 | 说明 | 调用方 |
|------|------|------|------|--------|
| GET | `/getActualTimeStatisticsInfo` | — | 全局实时统计 | Admin→Web |
| GET | `/getWeekStatisticsInfo` | dateType | 全局近7天趋势 | Admin→Web |

---

## 5.5 UserInfoApi — 用户信息内部接口

**路径前缀**: `/innerApi/user`

| 方法 | 路径 | 参数 | 说明 | 调用方 |
|------|------|------|------|--------|
| GET | `/updateCoinCount` | userId, count | 更新用户硬币数 | Interact→Web |
| GET | `/selectByUserId` | userId | 查询用户信息 | Interact→Web |

---

## 5.6 VideoInfoApi — 视频内部接口

**路径前缀**: `/innerApi/video`

| 方法 | 路径 | 参数 | 说明 | 调用方 |
|------|------|------|------|--------|
| GET | `/getVideoInfoFileByFileId` | fileId | 查询视频文件信息 | Resource→Web |
| GET | `/getVideoSelectByVideoId` | videoId | 查询视频信息 | Interact/Resource→Web |
| PUT | `/updateCountInfo` | videoId, fileId, changeCount | 更新视频播放量 | Interact→Web |
| GET | `/getVideoPostSelectByVideoId` | videoId | 查询投稿视频信息 | — |
| PUT | `/updateDocCount` | videoId, searchOrderTypeEnum, changeCount | 更新 ES 文档计数 | Interact→Web |
| POST | `/admin/loadVideoList` | JSON: VideoInfoPostQuery | 管理端查询投稿列表 | Admin→Web |
| GET | `/admin/auditVideo` | videoId, status, reason | 审核视频 | Admin→Web |
| POST | `/admin/recommendVideo` | videoId | 推荐视频 | Admin→Web |
| DELETE | `/admin/deleteVideo` | videoId | 删除视频 | Admin→Web |
| GET | `/admin/loadPVideoList` | videoId | 获取视频分P列表 | Admin→Web |
| POST | `/admin/getVideoCount` | JSON: VideoInfoQuery | 获取视频数量 | Admin→Web |
| POST | `/transferVideoInfoFile` | videoId, uploadId, userId, JSON body | 转码后更新文件信息 | Resource→Web |

---

# 六、全局异常处理

所有模块共享 `AGlobalExceptionHandlerController`（`@RestControllerAdvice`），统一拦截以下异常：

| 异常类型 | HTTP 场景 | 返回 code |
|----------|-----------|-----------|
| `BusinessException` | 业务异常 | 自定义 code |
| `BindException` / `MethodArgumentTypeMismatchException` | 参数校验失败 | 600 |
| `DuplicateKeyException` | 主键冲突 | 601 |
| `ConstraintViolationException` | 约束校验失败 | 600 |
| `NoHandlerFoundException` | 404 资源不存在 | 404 |
| `FeignException.ServiceUnavailable` | 远程服务不可用 | 503 |
| 其他 `Exception` | 未知异常 | 500 |

---

# 七、关键枚举值

## UserActionTypeEnum

| type | 说明 |
|------|------|
| 1 | VIDEO_LIKE — 视频点赞 |
| 2 | VIDEO_COIN — 视频投币 |
| 3 | VIDEO_COLLECT — 视频收藏 |
| 4 | COMMENT_LIKE — 评论点赞 |
| 5 | COMMENT_HATE — 评论点踩 |

## VideoStatusEnum

| status | 说明 |
|--------|------|
| 0 | 转码中 |
| 1 | 转码成功 |
| 2 | 待审核 |
| 3 | 审核通过 |
| 4 | 审核不通过 |

## MessageTypeEnum

| type | 说明 |
|------|------|
| LIKE | 点赞消息 |
| COMMENT | 评论消息 |
| SYS | 系统消息 |

## VideoOrderTypeEnum

| type | 说明 |
|------|------|
| 0 | CREATE_TIME — 最新发布 |
| 1 | PLAY_COUNT — 最多播放 |
| 2 | COLLECT_COUNT — 最多收藏 |

## CommentTopTypeEnum

| type | 说明 |
|------|------|
| 0 | NO_TOP — 非置顶 |
| 1 | TOP — 已置顶 |

## StatisticsTypeEnum

| type | 说明 |
|------|------|
| 0 | PLAY — 播放量 |
| 1 | COMMENT — 评论数 |
| 2 | DANMU — 弹幕数 |
| 3 | COLLECT — 收藏数 |
| 4 | COIN — 硬币数 |
| 5 | FANS — 粉丝数（管理端渲染为注册用户数） |

---

> 📅 文档生成时间：2026-07-17  
> 📝 基于源码分析自动生成，共覆盖 29 个 Controller + 6 个 Feign Provider，**100+ 个 API 接口**
