package com.endside.user.repository;

import com.endside.user.model.Device;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DeviceRepository extends JpaRepository<Device, Long> {
    void deleteByUserId(long userId);
    Optional<Device> findByUniqueId(String uniqueId);
}
