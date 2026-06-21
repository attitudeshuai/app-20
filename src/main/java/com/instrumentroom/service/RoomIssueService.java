package com.instrumentroom.service;

import com.instrumentroom.dto.common.PageResponse;
import com.instrumentroom.dto.issue.CreateIssueRequest;
import com.instrumentroom.dto.issue.IssueResponse;
import com.instrumentroom.dto.issue.UpdateIssueRequest;
import com.instrumentroom.dto.issue.UpdateIssueStatusRequest;
import com.instrumentroom.entity.IssueStatus;
import com.instrumentroom.entity.PracticeRoom;
import com.instrumentroom.entity.RoomIssue;
import com.instrumentroom.entity.User;
import com.instrumentroom.exception.BusinessException;
import com.instrumentroom.exception.ResourceNotFoundException;
import com.instrumentroom.repository.RoomIssueRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 房间问题反馈服务
 */
@Service
public class RoomIssueService {

    private final RoomIssueRepository issueRepository;
    private final PracticeRoomService roomService;
    private final AuthService authService;

    public RoomIssueService(
            RoomIssueRepository issueRepository,
            PracticeRoomService roomService,
            AuthService authService) {
        this.issueRepository = issueRepository;
        this.roomService = roomService;
        this.authService = authService;
    }

    /**
     * 创建反馈
     */
    @Transactional
    public IssueResponse createIssue(CreateIssueRequest request) {
        User currentUser = authService.getCurrentUserEntity();
        PracticeRoom room = roomService.getRoomEntityById(request.getRoomId());

        RoomIssue issue = RoomIssue.builder()
                .room(room)
                .reporter(currentUser)
                .issueType(request.getIssueType())
                .description(request.getDescription())
                .status(IssueStatus.OPEN)
                .build();

        issue = issueRepository.save(issue);
        return IssueResponse.fromEntity(issue, true);
    }

    /**
     * 获取反馈列表（支持分页、搜索、筛选）
     */
    public PageResponse<IssueResponse> getIssues(
            Long roomId,
            Long reporterId,
            IssueStatus status,
            String issueType,
            int page,
            int size,
            String sortBy,
            String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<RoomIssue> issuePage = issueRepository.searchIssues(roomId, reporterId, status, issueType, pageable);

        return PageResponse.from(issuePage, i -> IssueResponse.fromEntity(i, false));
    }

    /**
     * 获取反馈详情
     */
    public IssueResponse getIssueById(Long id) {
        RoomIssue issue = getIssueEntityById(id);
        checkViewPermission(issue);
        return IssueResponse.fromEntity(issue, true);
    }

    /**
     * 根据ID获取反馈实体
     */
    public RoomIssue getIssueEntityById(Long id) {
        return issueRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("反馈记录", "id", id));
    }

    /**
     * 更新反馈
     */
    @Transactional
    public IssueResponse updateIssue(Long id, UpdateIssueRequest request) {
        RoomIssue issue = getIssueEntityById(id);
        checkModifyPermission(issue);

        if (request.getIssueType() != null) {
            issue.setIssueType(request.getIssueType());
        }
        if (request.getDescription() != null) {
            issue.setDescription(request.getDescription());
        }
        if (request.getStatus() != null) {
            if (!authService.isAdmin()) {
                throw new BusinessException(403, "只有管理员可以更改反馈状态");
            }
            issue.setStatus(request.getStatus());
        }

        issue = issueRepository.save(issue);
        return IssueResponse.fromEntity(issue, true);
    }

    /**
     * 更新反馈状态（仅管理员）
     */
    @Transactional
    public IssueResponse updateIssueStatus(Long id, UpdateIssueStatusRequest request) {
        if (!authService.isAdmin()) {
            throw new BusinessException(403, "只有管理员可以更改反馈状态");
        }

        RoomIssue issue = getIssueEntityById(id);
        issue.setStatus(request.getStatus());
        issue = issueRepository.save(issue);
        return IssueResponse.fromEntity(issue, true);
    }

    /**
     * 删除反馈
     */
    @Transactional
    public void deleteIssue(Long id) {
        RoomIssue issue = getIssueEntityById(id);
        checkDeletePermission(issue);
        issueRepository.delete(issue);
    }

    /**
     * 检查查看权限（反馈创建者或管理员）
     */
    private void checkViewPermission(RoomIssue issue) {
        User currentUser = authService.getCurrentUserEntity();
        if (!currentUser.getId().equals(issue.getReporter().getId()) && !authService.isAdmin()) {
            throw new BusinessException(403, "无权查看此反馈");
        }
    }

    /**
     * 检查修改权限（反馈创建者或管理员）
     */
    private void checkModifyPermission(RoomIssue issue) {
        User currentUser = authService.getCurrentUserEntity();
        if (!currentUser.getId().equals(issue.getReporter().getId()) && !authService.isAdmin()) {
            throw new BusinessException(403, "无权修改此反馈");
        }
    }

    /**
     * 检查删除权限（仅管理员）
     */
    private void checkDeletePermission(RoomIssue issue) {
        User currentUser = authService.getCurrentUserEntity();
        if (!currentUser.getId().equals(issue.getReporter().getId()) && !authService.isAdmin()) {
            throw new BusinessException(403, "无权删除此反馈");
        }
    }
}
