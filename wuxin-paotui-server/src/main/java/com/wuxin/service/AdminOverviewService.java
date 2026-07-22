package com.wuxin.service;

import com.wuxin.vo.admin.AdminConsoleVO;

public interface AdminOverviewService {
    AdminConsoleVO.Dashboard dashboard();
    AdminConsoleVO.Finance finance();
}
