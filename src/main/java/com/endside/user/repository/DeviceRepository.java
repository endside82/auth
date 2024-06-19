package com.endside.user.repository;

import com.endside.user.model.Device;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface DeviceRepository extends JpaRepository<Device, Long> {
    void deleteByUserId(long userId);
}
