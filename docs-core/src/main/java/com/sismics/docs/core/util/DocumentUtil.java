package com.sismics.docs.core.util;

import com.sismics.docs.core.constant.AclType;
import com.sismics.docs.core.constant.PermType;
import com.sismics.docs.core.dao.jpa.AclDao;
import com.sismics.docs.core.dao.jpa.DocumentDao;
import com.sismics.docs.core.model.jpa.Acl;
import com.sismics.docs.core.model.jpa.Document;

/**
 * Document utilities.
 *
 * @author bgamard
 */
public class DocumentUtil {
    public static Document createDocument(Document document, String userId) {
        DocumentDao documentDao = new DocumentDao();
        String documentId = documentDao.create(document, userId);

        // Create read ACL
        AclDao aclDao = new AclDao();
        Acl acl = new Acl();
        acl.setPerm(PermType.READ);
        acl.setType(AclType.USER);
        acl.setSourceId(documentId);
        acl.setTargetId(userId);
        aclDao.create(acl, userId);

        // Create write ACL
        acl = new Acl();
        acl.setPerm(PermType.WRITE);
        acl.setType(AclType.USER);
        acl.setSourceId(documentId);
        acl.setTargetId(userId);
        aclDao.create(acl, userId);

        return document;
    }
}