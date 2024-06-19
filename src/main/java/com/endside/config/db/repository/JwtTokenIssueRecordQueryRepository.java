package com.endside.config.db.repository;

import com.endside.user.model.QJwtRecord;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.endside.user.model.JwtRecord;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class JwtTokenIssueRecordQueryRepository {

    private final JPAQueryFactory jpaQueryFactory;

    public List<JwtRecord> findAllValidTokensByUser(long userId, LocalDateTime date) {
        return jpaQueryFactory
                .select(QJwtRecord.jwtRecord)
                .from(QJwtRecord.jwtRecord)
                .where(QJwtRecord.jwtRecord.userId.eq(userId),
                        QJwtRecord.jwtRecord.expireDatetime.goe(date))
                .fetch();
    }
    public List<JwtRecord> findAllValidTokenByRefreshToken(long refreshTokenId, LocalDateTime date) {
        return jpaQueryFactory
                .select(QJwtRecord.jwtRecord)
                .from(QJwtRecord.jwtRecord)
                .where(QJwtRecord.jwtRecord.refreshTokenId.eq(refreshTokenId),
                        QJwtRecord.jwtRecord.expireDatetime.goe(date))
                .fetch();
    }
}
