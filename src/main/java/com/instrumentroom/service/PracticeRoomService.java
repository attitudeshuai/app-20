package com.instrumentroom.service;

import com.instrumentroom.dto.common.PageResponse;
import com.instrumentroom.dto.room.CreateRoomRequest;
import com.instrumentroom.dto.room.RoomResponse;
import com.instrumentroom.dto.room.UpdateRoomRequest;
import com.instrumentroom.dto.room.UpdateRoomStatusRequest;
import com.instrumentroom.entity.PracticeRoom;
import com.instrumentroom.entity.RoomStatus;
import com.instrumentroom.exception.BusinessException;
import com.instrumentroom.exception.ResourceNotFoundException;
import com.instrumentroom.repository.PracticeRoomRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 练习室管理服务
 */
@Service
public class PracticeRoomService {

    private final PracticeRoomRepository roomRepository;
    private final AuthService authService;

    public PracticeRoomService(PracticeRoomRepository roomRepository, AuthService authService) {
        this.roomRepository = roomRepository;
        this.authService = authService;
    }

    /**
     * 创建练习室（仅管理员）
     */
    @Transactional
    public RoomResponse createRoom(CreateRoomRequest request) {
        checkAdminPermission();

        if (request.getOpenTime() != null && request.getCloseTime() != null
                && !request.getOpenTime().isBefore(request.getCloseTime())) {
            throw new BusinessException("开放时间必须早于关闭时间");
        }

        PracticeRoom room = PracticeRoom.builder()
                .name(request.getName())
                .location(request.getLocation())
                .capacity(request.getCapacity())
                .equipment(request.getEquipment())
                .hourlyPrice(request.getHourlyPrice())
                .openTime(request.getOpenTime())
                .closeTime(request.getCloseTime())
                .status(request.getStatus() != null ? request.getStatus() : RoomStatus.OPEN)
                .build();

        room = roomRepository.save(room);
        return RoomResponse.fromEntity(room);
    }

    /**
     * 获取练习室列表（支持分页、搜索、筛选）
     */
    public PageResponse<RoomResponse> getRooms(
            String name,
            String location,
            RoomStatus status,
            int page,
            int size,
            String sortBy,
            String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<PracticeRoom> roomPage = roomRepository.searchRooms(name, location, status, pageable);

        return PageResponse.from(roomPage, RoomResponse::fromEntity);
    }

    /**
     * 获取练习室详情
     */
    public RoomResponse getRoomById(Long id) {
        PracticeRoom room = getRoomEntityById(id);
        return RoomResponse.fromEntity(room);
    }

    /**
     * 根据ID获取练习室实体
     */
    public PracticeRoom getRoomEntityById(Long id) {
        return roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("练习室", "id", id));
    }

    /**
     * 更新练习室（仅管理员）
     */
    @Transactional
    public RoomResponse updateRoom(Long id, UpdateRoomRequest request) {
        checkAdminPermission();

        PracticeRoom room = getRoomEntityById(id);

        if (request.getName() != null) {
            room.setName(request.getName());
        }
        if (request.getLocation() != null) {
            room.setLocation(request.getLocation());
        }
        if (request.getCapacity() != null) {
            room.setCapacity(request.getCapacity());
        }
        if (request.getEquipment() != null) {
            room.setEquipment(request.getEquipment());
        }
        if (request.getHourlyPrice() != null) {
            room.setHourlyPrice(request.getHourlyPrice());
        }
        if (request.getOpenTime() != null) {
            if (request.getCloseTime() != null && !request.getOpenTime().isBefore(request.getCloseTime())) {
                throw new BusinessException("开放时间必须早于关闭时间");
            }
            if (room.getCloseTime() != null && !request.getOpenTime().isBefore(room.getCloseTime())) {
                throw new BusinessException("开放时间必须早于关闭时间");
            }
            room.setOpenTime(request.getOpenTime());
        }
        if (request.getCloseTime() != null) {
            if (!room.getOpenTime().isBefore(request.getCloseTime())) {
                throw new BusinessException("开放时间必须早于关闭时间");
            }
            room.setCloseTime(request.getCloseTime());
        }
        if (request.getStatus() != null) {
            room.setStatus(request.getStatus());
        }

        room = roomRepository.save(room);
        return RoomResponse.fromEntity(room);
    }

    /**
     * 更新练习室状态（仅管理员）
     */
    @Transactional
    public RoomResponse updateRoomStatus(Long id, UpdateRoomStatusRequest request) {
        checkAdminPermission();

        PracticeRoom room = getRoomEntityById(id);
        room.setStatus(request.getStatus());
        room = roomRepository.save(room);
        return RoomResponse.fromEntity(room);
    }

    /**
     * 删除练习室（仅管理员）
     */
    @Transactional
    public void deleteRoom(Long id) {
        checkAdminPermission();

        if (!roomRepository.existsById(id)) {
            throw new ResourceNotFoundException("练习室", "id", id);
        }
        roomRepository.deleteById(id);
    }

    /**
     * 检查管理员权限
     */
    private void checkAdminPermission() {
        if (!authService.isAdmin()) {
            throw new BusinessException(403, "需要管理员权限");
        }
    }
}
