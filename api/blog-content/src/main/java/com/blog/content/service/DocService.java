package com.blog.content.service;


import com.blog.core.domain.auth.vo.UserVo;
import com.blog.core.domain.content.doc.entity.DocCatalog;
import com.blog.core.domain.content.doc.entity.DocContent;
import com.blog.core.domain.content.doc.vo.DocCatalogVo;

import java.util.List;

public interface DocService {

    Integer insertDocCatalog(DocCatalog docCatalog);

    Integer deleteDocCatalog(Integer id);

    Integer updateDocContent(DocContent docContent);

    Integer updateDocCatalog(DocCatalog docCatalog);

    List<DocCatalogVo> selectDocCatalogTree(DocCatalogVo docCatalogVo);

    DocContent selectDocContentById(Integer catalogId);

    DocCatalog selectDocCatalogById(Integer catalogId);

    List<UserVo> selectDocUserList();

}
