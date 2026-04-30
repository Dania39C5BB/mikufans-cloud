package com.linyajin.mikufans.component;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.Result;
import co.elastic.clients.elasticsearch._types.Script;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.*;
import co.elastic.clients.elasticsearch.core.search.Highlight;
import co.elastic.clients.elasticsearch.core.search.HighlightField;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.indices.CreateIndexRequest;
import co.elastic.clients.elasticsearch.indices.CreateIndexResponse;
import co.elastic.clients.elasticsearch.indices.ExistsRequest;
import co.elastic.clients.elasticsearch.indices.GetIndexRequest;
import co.elastic.clients.json.JsonData;
import com.linyajin.mikufans.config.AppConfig;
import com.linyajin.mikufans.dto.VideoInfoEsDto;
import com.linyajin.mikufans.entity.enums.PageSize;
import com.linyajin.mikufans.entity.enums.SearchOrderTypeEnum;
import com.linyajin.mikufans.entity.po.UserInfo;
import com.linyajin.mikufans.entity.po.VideoInfo;
import com.linyajin.mikufans.entity.query.SimplePage;
import com.linyajin.mikufans.entity.query.UserInfoQuery;
import com.linyajin.mikufans.entity.vo.PaginationResultVO;
import com.linyajin.mikufans.exception.BusinessException;
import com.linyajin.mikufans.mappers.UserInfoMapper;
import com.linyajin.mikufans.utils.CopyTools;
import com.linyajin.mikufans.utils.StringTools;
import jakarta.annotation.Resource;
//import org.elasticsearch.client.RestHighLevelClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.elasticsearch.core.query.ScriptType;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;


@Component("esSearchComponent")
@Slf4j
public class EsSearchComponent {

    @Resource
    private AppConfig appConfig;

    @Resource
    private ElasticsearchClient elasticsearchClient;

    @Resource
    private UserInfoMapper<UserInfo, UserInfoQuery> userInfoMapper;

    private Boolean isExitsIndex() throws IOException {
        ExistsRequest request = new ExistsRequest.Builder()
                .index(appConfig.getEsIndexVideoName())  // 指定要检查的索引名
                .build();
        return elasticsearchClient.indices().exists(request).value();
    }

    public void createIndex() {
        try {
            //检查索引是否存在，如果存在则不创建
            if(isExitsIndex()) {
                return;
            }
            String settingsJson = """
                {
                  "analysis": {
                    "analyzer": {
                      "comma": {
                        "type": "pattern",
                        "pattern": ","
                      }
                    }
                  }
                }
                """;
            String mappingJson = """
                 {
                    "properties": {
                        "videoId":{
                           "type": "text",
                           "index": false
                        },
                        "userId": {
                           "type": "text",
                           "index": false
                        },
                        "videoCover": {
                           "type": "text",
                           "index": false
                        },
                        "videoName": {
                           "type": "text",
                           "analyzer": "ik_max_word"
                        },
                        "tags": {
                           "type": "text",
                           "analyzer": "comma"
                        },
                        "playCount": {
                           "type": "integer",
                           "index": false
                        },
                        "danmuCount": {
                           "type": "integer",
                           "index": false
                        },
                        "collectCount": {
                           "type": "integer",
                           "index": false
                        },
                        "createTime": {
                           "type": "date",
                           "format":"yyyy-MM-dd HH:mm:ss||epoch_millis",
                           "index": false
                        }
                    }
                 }
                 """;
            //建索引（使用Builder模式） 创建索引并配置分析器
            CreateIndexResponse response = elasticsearchClient.indices().create(c -> c
                    .index(appConfig.getEsIndexVideoName())
                    // 可以添加mapping和settings配置
                    .settings(s -> s.withJson(new StringReader(settingsJson)))
                    .mappings(m -> m.withJson(new StringReader(mappingJson)))
            );
            boolean acknowledged = response.acknowledged();
            if (!acknowledged && response.shardsAcknowledged()) {
                throw new BusinessException("初始化es失败");
            }
        } catch (Exception e) {
            log.error("初始化es失败", e);
            throw new BusinessException("初始化es失败");
        }
    }

    //插入es文档（新版写法）
    public void saveDoc(VideoInfo videoInfo) {

       try {

           if (docExists(videoInfo.getVideoId())) {
                updateDoc(videoInfo);
           } else {
               log.info("插入es文档:{}" , videoInfo.getCreateTime());
               VideoInfoEsDto videoInfoEsDto = CopyTools.copy(videoInfo, VideoInfoEsDto.class);
               videoInfoEsDto.setCollectCount(0);
               videoInfoEsDto.setDanmuCount(0);
               videoInfoEsDto.setPlayCount(0);

               // 新版写法 (ElasticsearchClient)
               IndexResponse response = elasticsearchClient.index(i -> i
                       .index(appConfig.getEsIndexVideoName())
                       .id(videoInfo.getVideoId())
                       .document(videoInfoEsDto)  // 自动序列化对象为JSON
               );
           }
       } catch (IOException e) {
           log.error("保存es失败", e);
           throw new BusinessException("保存es失败", e);
       }

    }

    //查询es文档是否存在
    private Boolean docExists(String videoId) throws IOException {
        GetResponse<VideoInfoEsDto> response = elasticsearchClient.get(g -> g
                        .index(appConfig.getEsIndexVideoName())
                        .id(videoId),
                VideoInfoEsDto.class  // 指定返回的文档类型
        );
        // 2. 判断文档是否存在
        log.info("查询es结果：{} :{}", response,response.found());
        return response.found();
    }

    //更新es文档
    private void updateDoc(VideoInfo videoInfo) {
        try {
            videoInfo.setLastUpdateTime(null);
            videoInfo.setCreateTime(null);

            //利用反射 保存到es的时候 把非空字段过滤掉 生成一个标准的字符串
            Map<String , Object> dataMap = new HashMap<>();
            //获取 videoInfo里面 所有字段的名字
            Field[] fields = videoInfo.getClass().getDeclaredFields();
            //把所有的属性进行循环 把非空字段过滤掉 生成一个标准的字符串
            for (Field field : fields) {
                log.info("字段名：{}", field.getName());
                //upperCaseFirstLetter 把字段名首字母大写 比如 videoName 转成 VideoName
                //获取完整的就是 VideoInfo.getVideoName()
                String methodName = "get" + StringTools.upperCaseFirstLetter(field.getName());

                //获取方法
                Method method = videoInfo.getClass().getMethod(methodName);

                //通过invoke 执行方法 获取返回值
                Object object = method.invoke(videoInfo);
                //如果object不为空 且是字符串 且不为空 或者 object 不为null 且不是字符串类型
                if (object != null && object instanceof String && !StringTools.isEmpty(object.toString())
                        || object != null && !(object instanceof String)){
                    //把需要更新的字段和值放到map里面
                    dataMap.put(field.getName(), object);
                }
            }
            if (dataMap.isEmpty()) {
                return;
            }
            //更新 es
            // 1. 构建更新请求（链式DSL风格）
            UpdateResponse<Void> response = elasticsearchClient.update(u -> u
                            .index(appConfig.getEsIndexVideoName())
                            .id(videoInfo.getVideoId())
                            .doc(dataMap),  // 直接传入Map
                    Void.class      // 不关心返回的文档内容
            );
            // 2. 可选：检查操作结果
            if (response.result() == Result.Updated) {
                log.info("文档更新成功, ID: {}", videoInfo.getVideoId());
            }

        } catch (Exception e) {
            log.error("es更新视频失败", e);
            throw new BusinessException("es更新视频失败", e);
        }
    }

    //更新数量
    public void updateDocCount(String videoId ,String fileIdName ,Integer count) {

        try {
            Script script = Script.of(s -> s
                    .inline(i -> i
                            .lang("painless")
                            .source("ctx._source." + fileIdName + " += params.count")
                            .params("count", JsonData.of(count))  // 参数需包装为JsonData
                    )
            );
            UpdateResponse<Void> response = elasticsearchClient.update(u -> u
                            .index(appConfig.getEsIndexVideoName())
                            .id(videoId)
                            .script(script),
                    Void.class
            );
        } catch (Exception e) {
            log.error("es更新视频数量失败", e);
            throw new BusinessException("es更新视频数量失败", e);
        }
    }

    //删除es文档
    public void deleteDoc(String videoId) {
        try {
            DeleteResponse response = elasticsearchClient.delete(d -> d
                    .index(appConfig.getEsIndexVideoName())
                    .id(videoId)
            );
        } catch (Exception e) {
            log.error("es删除视频失败", e);
            throw new BusinessException("es删除视频失败", e);
        }
    }

    /**
     * 视频搜索功能
     * 要作用是通过关键词（keyword）在视频名称（videoName）和标签（tags）字段中匹配视频数据
     * 并返回分页结果。同时关联查询用户信息，最终返回一个包含视频列表和分页信息的封装对象
     * @param highlight 是否开启高亮
     * @param keyword 搜索关键字
     * @param orderType 排序类型
     * @param pageNo 页码
     * @param pageSize 每页大小
     * @return VideoInfo
     */
    public PaginationResultVO<VideoInfo> search(Boolean  highlight,String keyword,Integer orderType , Integer pageNo ,Integer pageSize) {
        SearchOrderTypeEnum searchOrderTypeEnum = SearchOrderTypeEnum.getByType(orderType);

       try {
           // 构建查询
           /*
            * 构建多字段匹配查询
            * 相当于原来的 QueryBuilders.multiMatchQuery(keyword, "videoName", "tags")
            */
           Query query = Query.of(q -> q
                   .multiMatch(m -> m
                           .query(keyword)
                           .fields("videoName", "tags")
                   )
           );

           // 创建搜索请求构建器，指定要查询的索引
           SearchRequest.Builder searchRequestBuilder = new SearchRequest.Builder()
                   .index(appConfig.getEsIndexVideoName()) // 从配置获取索引名
                   .query(query); // 设置查询条件

           // 设置高亮
           if (highlight) {
               Highlight.Builder highlightBuilder = new Highlight.Builder()
                       .fields("videoName", // 设置要高亮的字段
                               HighlightField.of(hf -> hf
                                       .preTags("<span class='highlight'>")
                                       .postTags("</span>")
                               )
                       );
               // 将高亮配置添加到搜索请求构建器中
               searchRequestBuilder.highlight(highlightBuilder.build());
           }

           // 设置排序
           /*
            * 排序设置部分
            * 1. 默认按 _score 升序排序
            * 2. 如果有额外排序条件，按指定字段降序排序
            */
           searchRequestBuilder.sort(s -> s.field(f -> f.field("_score").order(SortOrder.Asc)));
           if (orderType != null) {
               searchRequestBuilder.sort(s -> s.field(f -> f.field(searchOrderTypeEnum.getFileId()).order(SortOrder.Desc)));
           }

           // 设置分页
           /*
            * 分页设置部分
            * 1. 处理页码默认值（默认为1）
            * 2. 处理每页大小默认值（默认为PageSize.SIZE20的值）
            * 3. 计算 from 参数（跳过的文档数）
            */
           pageNo = pageNo == null ? 1 : pageNo;
           pageSize = pageSize == null ? PageSize.SIZE20.getSize() : pageSize;
           searchRequestBuilder
                   .from((pageNo - 1) * pageSize) // 计算起始位置
                   .size(pageSize); // 设置每页大小
           // 构建最终的搜索请求对象
           SearchRequest searchRequest = searchRequestBuilder.build();

           /*
            * 执行搜索
            * 1. 使用 elasticsearchClient 执行搜索
            * 2. 指定返回结果的类型
            */
           SearchResponse<VideoInfo> searchResponse = elasticsearchClient.search(searchRequest, VideoInfo.class);

           //获取命中结果
           List<Hit<VideoInfo>> hits = searchResponse.hits().hits();
           // 总命中数
           int totalCount = (int)searchResponse.hits().total().value();

           List<VideoInfo> videoInfoList = new ArrayList<>();
           List<String> userIdList = new ArrayList<>();

           // 遍历命中结果
            for (Hit<VideoInfo> hit : hits) {
                // 获取文档源数据
                // 1. 自动转换：hit.source() 直接返回 VideoInfo 对象（无需手动 JSON 解析）
                VideoInfo videoInfo = hit.source();
                // 2. 处理高亮字段（如果存在）
                if (videoInfo != null && hit.highlight().get("videoName") != null) {
                    // 取第一个高亮片段（通常只有一个）
                    videoInfo.setVideoName( hit.highlight().get("videoName").get(0));
                }
                videoInfoList.add(videoInfo);
                userIdList.add(videoInfo.getUserId());
            }

            for (String userId : userIdList) {
                log.info("es查询用户的id:{}",userId);
            }
           UserInfoQuery userInfoQuery = new UserInfoQuery();
           userInfoQuery.setUserIdList(userIdList);
           List<UserInfo> userInfoList = userInfoMapper.selectList(userInfoQuery);

           //转换成map集合
           // 键：UserInfo对象的userId字段
           // 值：UserInfo对象本身
           // 合并策略：key冲突时保留后者
           Map<String, UserInfo> userInfoMap = userInfoList.stream().collect(Collectors.toMap(item -> item.getUserId(), Function.identity(), (data1, data2) -> data2));

           videoInfoList.forEach(item -> {
               UserInfo userInfo = userInfoMap.get(item.getUserId());
               item.setNickName(userInfo == null ? "" : userInfo.getNickName());
           });

           SimplePage page = new SimplePage(pageNo, totalCount, pageSize);
           PaginationResultVO<VideoInfo> resultVO = new PaginationResultVO<>(totalCount, page.getPageSize(), page.getPageNo(), page.getPageTotal(), videoInfoList);
           return resultVO;
       } catch (Exception e) {
           log.error("es查询视频失败", e);
       }
        return null;
    }
}
